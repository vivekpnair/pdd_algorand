package com.example.laundry01;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.example.laundry01.backEndSimulator.RESTApiSimulator;
import com.example.laundry01.utils.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

// this page will list all orders...

public class OrdersActivity extends MenuForAllActivity {

    private Toolbar toolbar;

    private BaseAdapter mListViewAdapter;
    private ListView mListView;
    private User mLoggedOnUser;

    private RESTApiSimulator mRestAPI;

    ArrayList<HashMap<String, String>> mOrderList;

    @Override
    protected void onStart() {
        super.onStart();
        mLoggedOnUser = User.getInstance();
        if(mLoggedOnUser.getLoggedOn() == false)
        {
            Intent loginIntent = new Intent(OrdersActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
        else
        {
            Log.i("Orders now",mRestAPI.getMyOrders(mLoggedOnUser.getUserName()).toString());
            loadMyOrders(); // reload so that new orders created will also showup
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.myAppBar);
        setSupportActionBar(toolbar);
        toolbar.showOverflowMenu();

        mRestAPI = RESTApiSimulator.getInstance();

        mOrderList = new ArrayList<>();
        mListView = findViewById(R.id.listview);

        mListViewAdapter = new SimpleAdapter(
                OrdersActivity.this, mOrderList, R.layout.order_row_layout, new String[]{"order_id", "name","order_date","order_status"},
                new int[]{R.id.orderIDTV, R.id.orderNameTV, R.id.ordDateTV,R.id.ordStatusTV});
        mListView.setAdapter(mListViewAdapter);

        loadMyOrders();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override //ur overriding item click so that u can execute ur logic
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i("INFO", String.valueOf(i));

                TextView nameObjView = (TextView) view.findViewById(R.id.orderNameTV);
                TextView orderIDObjView = (TextView) view.findViewById(R.id.orderIDTV);

                Intent orderDetailsIntent = new Intent(OrdersActivity.this, OrderDetailsActivity.class);

                String name = nameObjView.getText().toString();
                String orderID = orderIDObjView.getText().toString();
                orderDetailsIntent.putExtra("name", name);
                orderDetailsIntent.putExtra("order_id", orderID);

                startActivity(orderDetailsIntent);
            }
        });
    }

public void loadMyOrders()
{
    try{
        mLoggedOnUser = User.getInstance();
        if(mLoggedOnUser.getLoggedOn() == true) {
            mOrderList.clear();// clear the list and reload

            // two paths here...if the user is not an admin get his/her orders...
            // if the user is admin then get all orders for the institution he/she administers
            JsonArray myOrders;
            if(mLoggedOnUser.isAdmin())
            {
                myOrders = mRestAPI.getMyInstOrders(mLoggedOnUser.getUserInst());
            }
            else {
                myOrders = mRestAPI.getMyOrders(mLoggedOnUser.getUserName());
            }
            for (JsonElement anOrderEl : myOrders) {
                JsonObject anOrderObj = anOrderEl.getAsJsonObject();
                Log.i("Order details ", anOrderObj.toString());

                String uname = anOrderObj.get("name").getAsString();
                String order_id = anOrderObj.get("order_id").getAsString();
                String order_status = anOrderObj.get("order_status").getAsString();

                HashMap<String, String> anOrder = new HashMap<>();
                anOrder.put("name", uname);
                anOrder.put("order_id", order_id);
                anOrder.put("order_status", order_status);
                anOrder.put("order_date", anOrderObj.get("order_date").getAsString());
                mOrderList.add(anOrder);
            }
            mListViewAdapter.notifyDataSetChanged();

        }
    }
    catch (Exception e)
    {
        e.printStackTrace();
    }
}
}

