package com.zjy.police.trafficassist.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zjy.police.trafficassist.R;
import com.zjy.police.trafficassist.listener.LoginStatusChangedListener;
import com.zjy.police.trafficassist.model.User;
import com.zjy.police.trafficassist.utils.LoginCheck;

public class LoginActivity extends AppCompatActivity {

    private User user;
    private EditText new_username;
    private EditText new_password;
    private Button loginBtn;
    private Button signUpBtn;
    private TextView forget_password;

    private static LoginStatusChangedListener loginStatusChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new_username = (EditText) findViewById(R.id.username);
        new_password = (EditText) findViewById(R.id.password);
        forget_password = (TextView) findViewById(R.id.forget_password);

        loginBtn = (Button) findViewById(R.id.user_sign_in);
        signUpBtn = (Button) findViewById(R.id.user_sign_up);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(LoginActivity.this, SignupActivity.class), 0);
            }
        });
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void attemptLogin(){

        // Reset errors.
        new_username.setError(null);
        new_password.setError(null);
        /**
         * 初始化User对象
         */
        user = new User(new_username.getText().toString(), new_password.getText().toString());
        //MapActivity.USER = USER;

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the USER entered one.
        if (!TextUtils.isEmpty(user.getPassword()) && !isPasswordValid(user.getPassword())) {
            new_password.setError(getString(R.string.error_invalid_password));
            focusView = new_password;
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
            final ProgressDialog mPDialog = new ProgressDialog(LoginActivity.this);
            mPDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mPDialog.setMessage(getResources().getString(R.string.now_user_login));
            mPDialog.setCancelable(true);
            mPDialog.show();

            LoginCheck loginCheck = new LoginCheck(this, user, mPDialog);
            loginCheck.setOnLoginStatusChanged(loginStatusChangedListener);
            loginCheck.login();
        }
    }

    public static void setOnLoginStatusChanged(LoginStatusChangedListener listener) {
        loginStatusChangedListener = listener;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            if (requestCode == 0) {
                if (data != null) {
                    new_username.setText(data.getStringExtra("username"));
                    new_password.setText(data.getStringExtra("password"));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
