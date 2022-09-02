package com.example.laundry01;

import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.laundry01.backEndSimulator.RESTApiSimulator;

import java.util.ArrayList;
import java.util.List;

public class RegisterUserActivity extends MenuForAllActivity {

    private TextView mUserNameTV;
    private TextView mPasswordTV;
    private TextView mMobileTV;
    private Spinner mInstSpnr;
    private CheckBox mIsAdminCB;
    private TextView mMsgRegUserTV;

    private RESTApiSimulator mRestAPI;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        toolbar = findViewById(R.id.myAppBar);
        setSupportActionBar(toolbar);

        mUserNameTV = findViewById(R.id.userNameTV);
        mPasswordTV = findViewById(R.id.passwordTV);
        mMobileTV = findViewById(R.id.mobileTV);
        mInstSpnr = findViewById(R.id.spnrInst);
        mIsAdminCB = findViewById(R.id.isAdminCBx);
        mMsgRegUserTV = findViewById(R.id.msgRegUserTV);

        // fill institutions with sample data...in real this has to be fecthed from backend from a db
        populateInsitutionData();
        // get access to our test data API singleton
        mRestAPI = RESTApiSimulator.getInstance();
    }

    public void onSubmitClick(View view) {

        String userName = mUserNameTV.getText().toString();
        String password = mPasswordTV.getText().toString();
        String mobile = mMobileTV.getText().toString();
        String selectedInst = mInstSpnr.getSelectedItem().toString();
        Boolean isAdmin = mIsAdminCB.isChecked();

        Boolean success = mRestAPI.registerUser(userName,password,mobile,selectedInst,isAdmin);
        if(success)
        {
            mMsgRegUserTV.setTextColor(Color.parseColor("#4BB543"));
            mMsgRegUserTV.setText("Successfully registered user : "+userName);
        }
        else
        {
            mMsgRegUserTV.setTextColor(Color.parseColor("#FF9494"));
            mMsgRegUserTV.setText("Failed to register user : "+userName);
        }
    }

    private void populateInsitutionData()
    {
        try{
            List<String> instList = new ArrayList<String>();
            instList.add("NIT Suratkal");
            instList.add("NIT Calicut");
            instList.add("NIT Warrangal");
            instList.add("NIT Trichy");

            Spinner instSpinner = (Spinner) findViewById(R.id.spnrInst);
            ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, instList);
            dataAdapter2.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            instSpinner.setAdapter(dataAdapter2);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}