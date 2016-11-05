package com.zjy.police.trafficassist;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.overlay.DrivingRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.zjy.police.trafficassist.adapter.AccidentPicAdapter;
import com.zjy.police.trafficassist.util.SensorEventHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements LocationSource, AMapLocationListener,
        AMap.OnMarkerClickListener, AMap.InfoWindowAdapter,
        GeocodeSearch.OnGeocodeSearchListener, AMap.OnMapClickListener,
        RouteSearch.OnRouteSearchListener, View.OnClickListener {

    private MapView mapView;
    private AMap aMap;
    // 处理定位更新
    private OnLocationChangedListener mListener;
    // 定位
    private AMapLocationClient mlocationClient;
    private LatLng location;
    private GeocodeSearch geocoderSearch;
    private RouteSearch mrouteSearch;
    private DrivingRouteOverlay routeOverlay;
    private Marker currentMarker;
    private Marker mLocMarker;

    private boolean mFirstFix = false;
    private SensorEventHelper mSensorHelper;
    private Circle mCircle;
    private BottomSheetDialog dialog;

    private static final int STROKE_COLOR   = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR     = Color.argb(10, 0, 0, 180);
    private static final int CALL_PHONE     = 1;

    private Map<String, String> ReturnInfo = new HashMap<>();
    private ArrayList<String> accidentTags = new ArrayList<>();
    private ArrayList<Bitmap> bitmapArr = new ArrayList<>();
    private String addressName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //地图初始化
        mapView = (MapView) findViewById(R.id.map);
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
        Initial();

        aMap.setOnMarkerClickListener(this);
        aMap.setOnMapClickListener(this);
//        aMap.setInfoWindowAdapter(this);
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);

        mrouteSearch = new RouteSearch(this);
        mrouteSearch.setRouteSearchListener(this);
    }

    private void Initial() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            setUpMap();
        }
        mSensorHelper = new SensorEventHelper(this);
        mSensorHelper.registerSensorListener();
    }

    //定位功能
    private void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮可见
        aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位
        aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private void checkPermission() {
        // 请求拨打电话权限
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CALL_PHONE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        CALL_PHONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CALL_PHONE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("permission", "PERMISSION_GRANTED: Thx, I will give u a better service!");
                } else {
                    Log.i("permission", "PERMISSION_DENIED: Sorry, without this permission I can't do next work for u");
                    Toast.makeText(MainActivity.this, "拨打电话失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
        aMap.animateCamera(update, 1000, callback);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
//        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
        currentMarker = marker;
        if(marker != mLocMarker)
            showBSDialog();
        changeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(marker.getPosition(), 15, 0, 0)), null);
        return false;
    }

    private void showBSDialog() {
        dialog = new BottomSheetDialog(this);
//        Fresco.initialize(this);
        final View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.bottomsheet_dialog_layout, null);
        view.findViewById(R.id.btn_carowner_info).setOnClickListener(this);
        view.findViewById(R.id.btn_carowner_call).setOnClickListener(this);
        view.findViewById(R.id.fab_startnavi).setOnClickListener(this);
        // 获取事故标签
        new AsyncTask<Void, Void, ArrayList<String>>() {
            @Override
            protected ArrayList<String> doInBackground(Void... params) {
                return WebService.getAccidentTags(ReturnInfo.get("username"));
            }

            @Override
            protected void onPostExecute(final ArrayList<String> accTags) {
                super.onPostExecute(accTags);
                accidentTags = accTags;
                TextView tv = (TextView) view.findViewById(R.id.acc_tags);
                if(accidentTags.size() != 0) {
                    tv.setText(accidentTags.get(0));
                    for(int i = 1; i < accidentTags.size(); i++) {
                        tv.append("     " + accidentTags.get(i));
                    }
                }else {
                    tv.setText("");
                }
            }
        }.execute();
        // 获取事故图片
        final RecyclerView accPicList = (RecyclerView) view.findViewById(R.id.list_acc_pic);
        final LinearLayoutManager LayoutManager = new LinearLayoutManager(this);
        LayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        accPicList.setLayoutManager(LayoutManager);             //设置LinearLayoutManager
        accPicList.setItemAnimator(new DefaultItemAnimator());  //设置ItemAnimator
        accPicList.setHasFixedSize(true);                       //设置固定大小
        accPicList.addItemDecoration(new PicDecoration(this, PicDecoration.HORIZONTAL_LIST));
        accPicList.addOnItemTouchListener(new RecyclerViewClickListener(this, accPicList,
                new RecyclerViewClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, "哈哈哈", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
        new AsyncTask<Void, Void, ArrayList<Bitmap>>() {
            @Override
            protected ArrayList<Bitmap> doInBackground(Void... params) {
                return WebService.getAccidentPics(ReturnInfo.get("username"));
            }

            @Override
            protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
                super.onPostExecute(bitmaps);
                bitmapArr = bitmaps;
                AccidentPicAdapter accidentPicAdapter = new AccidentPicAdapter(MainActivity.this, bitmapArr);
                accPicList.setAdapter(accidentPicAdapter);
                accidentPicAdapter.notifyDataSetChanged();
            }
        }.execute();
        dialog.setContentView(view);
        dialog.show();
    }

    /**
     * 监听自定义infowindow窗口的infowindow事件回调
     */
    @Override
    public View getInfoWindow(Marker marker) {
        if(marker.getSnippet().equals(ReturnInfo.get("username"))) {
            View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
            RegeocodeQuery query = new RegeocodeQuery(
                    new LatLonPoint(marker.getPosition().latitude, marker.getPosition().longitude),
                    100, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
            render(marker, infoWindow);
            return infoWindow;
        }
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public void render(final Marker marker, View view) {
        TextView info_user = (TextView) view.findViewById(R.id.info_window_user);
        TextView info_localtion = (TextView) view.findViewById(R.id.info_window_localtion);
        Button info_call = (Button) view.findViewById(R.id.info_window_call);
        Button info_navi = (Button) view.findViewById(R.id.info_window_navi);
        if (info_user != null && info_localtion != null) {
            info_user.setText(ReturnInfo.get("username"));
            if (addressName != null) {
                info_localtion.setText(addressName);
            } else {
                Log.d("info_window", "text_not_ok");
            }
            info_user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, AccidentDetail.class));
                }
            });
        }
        if (info_call != null) {
            info_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder comfirmDialog =
                            new AlertDialog.Builder(MainActivity.this);
                    comfirmDialog.setTitle("提示").setMessage("你确定要打电话给报警人吗?");
                    comfirmDialog.setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkPermission();
                                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ReturnInfo.get("username"))));
                                }
                            }).setNegativeButton("取消", null);
                    comfirmDialog.show();// 显示
                }
            });
        }
        if (info_navi != null) {
            info_navi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 第三个参数表示途经点（最多支持16个），第四个参数表示避让区域（最多支持32个），第五个参数表示避让道路
//                    RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(
//                            new RouteSearch.FromAndTo(
//                                    new LatLonPoint(location.latitude, location.longitude),
//                                    new LatLonPoint(Double.parseDouble(ReturnInfo.get("latitude")),
//                                            Double.parseDouble(ReturnInfo.get("longitude")))),
//                            RouteSearch.RidingDefault, null, null, "");
//                    mrouteSearch.calculateDriveRouteAsyn(query);
//                    marker.hideInfoWindow();
//                    marker.remove();
                    Intent i = new Intent(MainActivity.this, NaviView.class);
                    i.putExtra("mlocation", location);
                    i.putExtra("ap_latitude", ReturnInfo.get("latitude"));
                    i.putExtra("ap_longitude", ReturnInfo.get("longitude"));
                    startActivity(i);
                }
            });
        }
    }

    private void getAccLoc() {
        new AsyncTask<Void, Void, Map<String, String>>() {

            @Override
            protected Map<String, String> doInBackground(Void... params) {
//                ReturnInfo = WebService.getAccidentLoc(UserStatus.user.getLocation().longitude,
//                        UserStatus.user.getLocation().latitude);
//                if(ReturnInfo != null)
//                    Log.d("location", ReturnInfo.get("latitude") + "   " + ReturnInfo.get("longitude"));
//                else
//                    Toast.makeText(MainActivity.this, "无法连接到服务器", Toast.LENGTH_SHORT).show();
                ReturnInfo.put("username", "15158266502");
                ReturnInfo.put("longitude", "120.3452320");
                ReturnInfo.put("latitude", "30.3258400");
                return ReturnInfo;
                //{"username":"15158266502","longitude":"120.3452320","latitude":"30.3258400"}
            }

            @Override
            protected void onPostExecute(final Map<String, String> accidentPoint) {
                super.onPostExecute(accidentPoint);
                if(accidentPoint != null) {
                    Marker marker = aMap.addMarker(new MarkerOptions().
                            position(new LatLng(Double.parseDouble(accidentPoint.get("latitude")), Double.parseDouble(accidentPoint.get("longitude")))).
                            //title("事故点").
                            //snippet(accidentPoint.get("username")).
                            icon(BitmapDescriptorFactory.fromBitmap(
                                    BitmapFactory.decodeResource(getResources(),
                                            R.mipmap.location_marker))));
                }
            }
        }.execute();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (currentMarker != null) {
            currentMarker.hideInfoWindow();
        }
    }

    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addMarker(LatLng latlng) {
        if (mLocMarker != null) {
            return;
        }
        Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.navi_map_gps_locked);
        BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);

//		BitmapDescriptor des = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
        MarkerOptions options = new MarkerOptions();
        options.icon(des);
        options.anchor(0.5f, 0.5f);
        options.position(latlng);
        mLocMarker = aMap.addMarker(options);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                location = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                UserStatus.user.setLocation(location);
                if (!mFirstFix) {
                    mFirstFix = true;
                    addCircle(location, amapLocation.getAccuracy());//添加定位精度圆
                    addMarker(location);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                } else {
                    mCircle.setCenter(location);
                    mCircle.setRadius(amapLocation.getAccuracy());
                    mLocMarker.setPosition(location);
                }
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getLocationDetail();//获取定位信息描述
                if(routeOverlay == null)
                    getAccLoc();
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
                Toast.makeText(getApplicationContext(), errText, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            //退出时杀死进程
            mLocationOption.setKillProcess(true);
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置定位间隔,单位毫秒,默认为2000ms
            mLocationOption.setInterval(2000);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    // 逆地理编码回调
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == 1000) {
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                    && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                addressName = regeocodeResult.getRegeocodeAddress().getFormatAddress() + "附近";
                Log.d("info_window", "i am here  " + addressName);
            } else {

            }
        }
    }

    // 地理编码回调
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    //MapView生命周期
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(currentMarker != null) {
            currentMarker.hideInfoWindow();
        }
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        mFirstFix = false;
        mapView.onPause();
//        deactivate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
        mapView.onDestroy();
    }

    // 以下是路线规划需实现的方法
    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        //解析result获取算路结果
        DrivePath drivePath = driveRouteResult.getPaths().get(0);
        routeOverlay = new DrivingRouteOverlay(this, aMap, drivePath,
                driveRouteResult.getStartPos(), driveRouteResult.getTargetPos());
        routeOverlay.addToMap();
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {}
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {}
    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_carowner_info:
                startActivity(new Intent(MainActivity.this, AccidentDetail.class));
                break;
            case R.id.btn_carowner_call:
                final AlertDialog.Builder comfirmDialog =
                        new AlertDialog.Builder(MainActivity.this);
                comfirmDialog.setTitle("提示").setMessage("你确定要打电话给报警人吗?");
                comfirmDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                checkPermission();
                                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ReturnInfo.get("username"))));
                            }
                        }).setNegativeButton("取消", null);
                comfirmDialog.show();// 显示
                break;
            case R.id.fab_startnavi:
                dialog.dismiss();
                Intent i = new Intent(MainActivity.this, NaviView.class);
                i.putExtra("mlocation", location);
                i.putExtra("ap_latitude", ReturnInfo.get("latitude"));
                i.putExtra("ap_longitude", ReturnInfo.get("longitude"));
                startActivity(i);
                break;
        }
    }
}
