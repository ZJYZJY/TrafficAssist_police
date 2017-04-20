package com.zjy.police.trafficassist.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zjy.police.trafficassist.R;
import com.zjy.police.trafficassist.model.User;
import com.zjy.police.trafficassist.UserStatus;
import com.zjy.police.trafficassist.WebService;
import com.zjy.police.trafficassist.utils.HttpUtil;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.zjy.police.trafficassist.utils.HttpUtil.SUCCESS;

public class SignupActivity extends AppCompatActivity {

    private User user;

    private CoordinatorLayout container;
    private EditText new_username;
    private EditText new_passname;
    private Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        container = (CoordinatorLayout) findViewById(R.id.reg_container);
        new_username = (EditText) findViewById(R.id.username);
        new_passname = (EditText) findViewById(R.id.password);
        signUp = (Button) findViewById(R.id.sign_up_button);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager m = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(m.isActive()){
                    m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                attemptSignUp();
            }
        });
    }

    private void attemptSignUp(){

        // Reset errors.
        new_username.setError(null);
        new_passname.setError(null);
        /**
         * 初始化User对象
         */
        user = new User(new_username.getText().toString(), new_passname.getText().toString());
        //MapActivity.USER = USER;

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the USER entered one.
        if (!TextUtils.isEmpty(user.getPassword()) && !isPasswordValid(user.getPassword())) {
            new_passname.setError(getString(R.string.error_invalid_password));
            focusView = new_passname;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(user.getUsername())) {
            new_username.setError(getString(R.string.error_field_required));
            focusView = new_username;
            cancel = true;
        } else if (!isUsernameValid(user.getUsername())) {
            new_username.setError(getString(R.string.error_invalid_username));
            focusView = new_username;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // perform the USER login attempt.
            final ProgressDialog mPDialog = new ProgressDialog(SignupActivity.this);
            mPDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mPDialog.setMessage(getResources().getString(R.string.now_user_register));
            mPDialog.setCancelable(true);
            mPDialog.show();

            HttpUtil.create().signUp(user).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    mPDialog.dismiss();
                    try {
                        String res = response.body().string();
                        if(HttpUtil.stateCode(res) == SUCCESS){
                            Toast.makeText(SignupActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.putExtra("username", new_username.getText().toString());
                            intent.putExtra("password", new_passname.getText().toString());
                            setResult(0, intent);
                            finish();
                        }else {
                            Toast.makeText(SignupActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(SignupActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean isUsernameValid(String username) {
        return username.length() == 11;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 6;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}