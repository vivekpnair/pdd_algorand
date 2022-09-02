package com.example.laundry01;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.laundry01.backEndSimulator.RESTApiSimulator;
import com.example.laundry01.utils.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddOrderActivity extends MenuForAllActivity { // thid

    private Toolbar toolbar;

    private User mLoggedOnUser;
    private RESTApiSimulator mRestAPI;

    private BaseAdapter mSelectedItemsAdapter;
    private ListView mSelectedItemsListView;
    private ArrayList<HashMap<String, String>> mClothItems;
    private Spinner mClothTypeSpnr;
    private Spinner mClothCountSpnr;
    private TextView mTotalCostTV;
    private TextView mMsgTV;

    @Override
    protected void onStart() {
        super.onStart();
        mLoggedOnUser = User.getInstance();
        mRestAPI = RESTApiSimulator.getInstance();

        if(mLoggedOnUser.getLoggedOn() == false)
        {
            Intent loginIntent = new Intent(AddOrderActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
        else
        {
            populateClothTypes();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);
        toolbar = findViewById(R.id.myAppBar);
        setSupportActionBar(toolbar);

        mClothTypeSpnr = findViewById(R.id.spnrClothTypeID);
        mClothCountSpnr  = findViewById(R.id.spnrClothCountID);
        mTotalCostTV =findViewById(R.id.totalCostTV);
        mMsgTV =findViewById(R.id.msgTV);


        mClothItems = new ArrayList<>();

        mSelectedItemsAdapter =new SimpleAdapter(this,mClothItems,R.layout.cloths_selected_row_layout,new String[]{"name","count"},new int[]{R.id.orderIDTV,R.id.orderNameTV}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                // get filled view from SimpleAdapter
                View itemView=super.getView(position, convertView, parent);
                // find our button there
                View itemDelBtn=itemView.findViewById(R.id.deleteItemID);
                // add an onClickListener
                itemDelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("onClick","onClick Deleted item");
                        mClothItems.remove(position);
                        mSelectedItemsAdapter.notifyDataSetChanged();
                        int totalCost = calculateTotal(mClothItems);
                        mTotalCostTV.setText(String.valueOf(totalCost));
                    }
                });
                return itemView;
            }
        };

        mSelectedItemsListView = findViewById(R.id.lvSelectedItemsID);
        mSelectedItemsListView.setAdapter(mSelectedItemsAdapter);
    }

    private int calculateTotal(ArrayList<HashMap<String, String>> mClothItems) {
        int total = 0;
        try{
            Gson gson = new Gson();
            JsonArray selItems = gson.toJsonTree(mClothItems).getAsJsonArray();
            JsonArray allClothTypes = mRestAPI.getAllClothTypes();
            // [{"name":"Shirt","price":"10"},{"name":"Pants","price":"12"},{"name":"Jeans","price":"15"}]
            Log.i("allClorhTypes",allClothTypes.toString());
            for (JsonElement anItemE : selItems)
            {
                JsonObject anItemO = anItemE.getAsJsonObject();
                String name = anItemO.get("name").getAsString();
                int count = Integer.parseInt(anItemO.get("count").getAsString());

                // find the price for item name from allClothTypes json array
                int price = 0;
                for (JsonElement aClothTypeE : allClothTypes)
                {
                    JsonObject aClothTypeO = aClothTypeE.getAsJsonObject();
                    if(aClothTypeO.get("name").getAsString().equals(name))
                    {
                        price = Integer.parseInt(aClothTypeO.get("price").getAsString());
                        break;
                    }
                }
                // now we got price
                total+= price*count;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return total;
    }

    private void populateClothTypes()
    {
        try{
            if(mLoggedOnUser.getLoggedOn() == true) {
                JsonObject loggedOnUser = mLoggedOnUser.getUser();
                JsonArray allClothTypes = mRestAPI.getAllClothTypes();

                List<String> allClothTypesL = new ArrayList<String>();
                for (JsonElement aClothTypeE : allClothTypes) {
                    JsonObject aClothTypeO = aClothTypeE.getAsJsonObject();
                    String name = aClothTypeO.get("name").getAsString();
                    allClothTypesL.add(name);
                }
                Spinner spinner1 = (Spinner) findViewById(R.id.spnrClothTypeID);
                ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>
                        (this, android.R.layout.simple_spinner_item, allClothTypesL);
                dataAdapter1.setDropDownViewResource
                        (android.R.layout.simple_spinner_dropdown_item);
                spinner1.setAdapter(dataAdapter1);

                // now the cloth count spinner
                List<Integer> numList = new ArrayList<Integer>();
                for(int i=1;i<100;i++)
                {
                    numList.add(i);
                }
                Spinner spinner2 = (Spinner) findViewById(R.id.spnrClothCountID);
                ArrayAdapter<Integer> dataAdapter2 = new ArrayAdapter<Integer>
                        (this, android.R.layout.simple_spinner_item, numList);
                dataAdapter2.setDropDownViewResource
                        (android.R.layout.simple_spinner_dropdown_item);
                spinner2.setAdapter(dataAdapter2);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onAddItemClick(View view) {

        HashMap<String, String> anItem = new HashMap<>();
        anItem.put("name", mClothTypeSpnr.getSelectedItem().toString());
        anItem.put("count", mClothCountSpnr.getSelectedItem().toString());
        mClothItems.add(anItem);
        mSelectedItemsAdapter.notifyDataSetChanged();
        int totalCost = calculateTotal(mClothItems);
        mTotalCostTV.setText(String.valueOf(totalCost));


    }

    public void onSubmitOrdClick(View view) {
        // save the order this will be added to the in memory JSON test data in JsomTestData object
        // ....in real world ...this will be serevr side REST API post call which will persist the data to a db
        // for our demo purpose we will save it to our test data object
        //createOrderObj(String username, String inst, ArrayList<OrderItem> itemsList)

        JsonObject ret = mRestAPI.createOrderObj(mLoggedOnUser.getUser().get("user_info").getAsJsonObject().get("name").getAsString(),
                                         mLoggedOnUser.getUser().get("user_info").getAsJsonObject().get("inst").getAsString(),
                                         mClothItems,Integer.parseInt(mTotalCostTV.getText().toString()));

        Log.i("createOrderObj",ret.toString());
        if(ret.isJsonNull())
        {
            mMsgTV.setText("Failed to create order !. Please check with admin");
        }
        else
        {
            mMsgTV.setText("Successfully created order. order id: " + ret.get("order_id").getAsString());
        }
    }
}