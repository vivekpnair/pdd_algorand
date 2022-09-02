package com.example.laundry01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.laundry01.backEndSimulator.RESTApiSimulator;
import com.example.laundry01.utils.User;
import com.google.gson.JsonObject;

public class LoginActivity extends AppCompatActivity {

    private EditText userName;
    private EditText password;
    private RESTApiSimulator mRestAPI;
    private TextView mMsgLoginTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mRestAPI = RESTApiSimulator.getInstance();
        mMsgLoginTV = findViewById(R.id.msgLoginTV);
    }

    public void loginCick(View view) {
        userName = findViewById(R.id.edtTxtUname);
        password = findViewById(R.id.edtTxtPwd);
        JsonObject authRes = mRestAPI.authUser(userName.getText().toString(),password.getText().toString());
        if(authRes.get("login_success").getAsBoolean() == true){
            mMsgLoginTV.setTextColor(Color.parseColor("#4BB543"));
            mMsgLoginTV.setText(userName.getText()+ " successfully logged in!");
            User loggedOnUser = User.getInstance(); // this user instance is a singleton and will be available in all activtites with the user info
            loggedOnUser.setUserInfo(authRes,true);
            // load my main activity which will list my orders
            Intent ordersActivityIntent = new Intent(LoginActivity.this, OrdersActivity.class);
            startActivity(ordersActivityIntent);
        }
        else {
            mMsgLoginTV.setTextColor(Color.parseColor("#FF9494"));
            mMsgLoginTV.setText(userName.getText()+ " failed to login!");
        }
    }

    public void onRegUserClick(View view) {
        // load register user activity
        Intent regUserActivityIntent = new Intent(LoginActivity.this, RegisterUserActivity.class);
        startActivity(regUserActivityIntent);
    }
}