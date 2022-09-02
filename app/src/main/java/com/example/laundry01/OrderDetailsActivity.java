package com.example.laundry01;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.laundry01.backEndSimulator.RESTApiSimulator;
import com.example.laundry01.utils.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailsActivity extends MenuForAllActivity {

    private Toolbar toolbar;
    private Spinner mSpnrStatus;
    private Button mUpdateOrderBtn;
    private Button mPayBtn;
    private TextView msgOrdDetTV;

    private JsonObject mSelectedOrder;
    private ArrayAdapter<String> mSpnrStatusAdapter;
    RESTApiSimulator mRestAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        toolbar = findViewById(R.id.myAppBar);
        setSupportActionBar(toolbar);

        mSpnrStatus = findViewById(R.id.spnrStatus);
        mPayBtn = findViewById(R.id.payBtn);
        mUpdateOrderBtn = findViewById(R.id.updateOrderBtn);
        msgOrdDetTV = findViewById(R.id.msgOrdDetTV);

        mRestAPI = RESTApiSimulator.getInstance();

        populateOrderStatusData();


        Intent myIntent = getIntent(); // gets the previously created intent
        String name = myIntent.getStringExtra("name");
        String orderID = myIntent.getStringExtra("order_id");

        mSelectedOrder = mRestAPI.getOrderDetails(Integer.parseInt(orderID));
        TextView tv = (TextView) findViewById(R.id.msg);
        tv.setText("Order ID : " + mSelectedOrder.get("order_id").getAsString() +
                "\nOrder for : " + mSelectedOrder.get("name").getAsString() +
                "\nInstitution : " + mSelectedOrder.get("inst").getAsString() +
                "\nOrder Date : " + mSelectedOrder.get("order_date").getAsString() +
                "\nTotal cost : " + mSelectedOrder.get("total_cost").getAsString());

        int spinnerPosition = mSpnrStatusAdapter.getPosition(mSelectedOrder.get("order_status").getAsString());
        mSpnrStatus.setSelection(spinnerPosition);

        if (!User.getInstance().isAdmin()) {
            // if user is not admin dont show the UPDATE button and lock the spinner
            mUpdateOrderBtn.setVisibility(View.GONE);
            mSpnrStatus.setEnabled(false);
        }

        // now populate the order item details
        JsonArray orderItems = mSelectedOrder.getAsJsonArray("items");
        String items = "";
        for (JsonElement anItemEL : orderItems) {
            JsonObject anItemOObj = anItemEL.getAsJsonObject();
            Log.i("Order details ", anItemOObj.toString());
            items += anItemOObj.get("name").getAsString() + "  :   " + anItemOObj.get("count").getAsString() + "\n";
        }
        TextView ordItemsV = findViewById(R.id.ordItemsID);
        ordItemsV.setText(items);
    }

    private void populateOrderStatusData() {
        try {
            List<String> instList = new ArrayList<String>();
            instList.add("New");
            instList.add("InProgress");
            instList.add("Completed");
            instList.add("Delivered");
            instList.add("Cancelled");
            mSpnrStatusAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item, instList);
            mSpnrStatusAdapter.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            mSpnrStatus.setAdapter(mSpnrStatusAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUpdateClick(View view) {
        // update order with if status has changed
        String currentStatus = mSelectedOrder.get("order_status").getAsString();
        String selectedStatus = mSpnrStatus.getSelectedItem().toString();

        if(currentStatus.equals(selectedStatus))
        {
            Log.i("onUpdateClick","same status ...not updating");
        }
        else if((selectedStatus.equals("InProgress")) ||
                (selectedStatus.equals("Completed")) ||
                (selectedStatus.equals("Delivered")) ||
                (selectedStatus.equals("Cancelled")))
        {
            //update status
            mSelectedOrder.remove("order_status");
            mSelectedOrder.addProperty("order_status",selectedStatus);
            mRestAPI.updateOrder(mSelectedOrder.get("order_id").getAsInt(),mSelectedOrder);
        }
        else
        {
            Log.i("onUpdateClick","ivalid case ...not updating");
        }
    }

    public void onPayClick(View view) {
        try {
            String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
            int GOOGLE_PAY_REQUEST_CODE = 123;
            Uri uri =
                    new Uri.Builder()
                            .scheme("upi")
                            .authority("pay")
                            .appendQueryParameter("pa", "nair.chithra@oksbi")
                            .appendQueryParameter("pn", "MR WASH!")
                            //    .appendQueryParameter("mc", "1234")
                            //   .appendQueryParameter("tr", "123456789")
                            .appendQueryParameter("tn", "MR Wash laundry payment")
                            .appendQueryParameter("am", "1.00")
                            .appendQueryParameter("cu", "INR")
                            //   .appendQueryParameter("url", "https://test.merchant.website")
                            .build();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
            startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            msgOrdDetTV.setTextColor(Color.parseColor("#FF9494"));
            msgOrdDetTV.setText("Payment failed!");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            int GOOGLE_PAY_REQUEST_CODE = 123;
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == GOOGLE_PAY_REQUEST_CODE) {
                // Process based on the data in response.
                Log.i("result", data.getStringExtra("Status"));
                msgOrdDetTV.setText(data.toString());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            msgOrdDetTV.setTextColor(Color.parseColor("#FF9494"));
            msgOrdDetTV.setText("Payment failed!");
        }
    }
}
