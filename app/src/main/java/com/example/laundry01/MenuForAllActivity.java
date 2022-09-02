package com.example.laundry01;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.laundry01.utils.User;
import com.google.gson.JsonObject;

public class MenuForAllActivity extends AppCompatActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_menu, menu);
        //return true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Intent addOrderIntent = new Intent(MenuForAllActivity.this, AddOrderActivity.class);
                startActivity(addOrderIntent);
                break;
            case R.id.listorders:
                Intent orderDetailsIntent = new Intent(MenuForAllActivity.this, OrdersActivity.class);
                startActivity(orderDetailsIntent);
                break;
            case R.id.logout:
                User loggedOnUser = User.getInstance();
                loggedOnUser.setLoggedOn(false); // set logged in to false and redirect to orders which will check for login
                loggedOnUser.setUser(new JsonObject()); // empty json
                Intent ordersIntent = new Intent(MenuForAllActivity.this, OrdersActivity.class);
                startActivity(ordersIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
