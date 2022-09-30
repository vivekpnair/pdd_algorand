package com.example.laundry01.backEndSimulator;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RESTApiSimulator {

    private static RESTApiSimulator uniqueInstance;


    private JsonArray users = new JsonArray();
    private JsonArray clothTypes = new JsonArray();
    private JsonArray orders = new JsonArray();
    private Boolean dataLoaded = false;
    private int orderIDCount = 1;

    private RESTApiSimulator() { // sample comment...again !
        // a private constructor to support the singleton model
    }

    public static RESTApiSimulator getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new RESTApiSimulator();
            uniqueInstance.addTestClothTypes(); // only load cloth types. rest all in runtime thru app
        }
        return uniqueInstance;
    }

    public void addClothType(JsonObject aClothType) {
        try {
            clothTypes.add(aClothType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonArray getAllClothTypes() {
        return clothTypes;
    }

    public JsonObject authUser(String userName, String pwd) {
        JsonObject authRes = new JsonObject();
        authRes.addProperty("login_success", false); // init as false
        try {
            for (int i = 0; i < users.size(); i++) {
                JsonObject anUser = (JsonObject) users.get(i);
                if (anUser.get("name").getAsString().equals(userName) &&
                        (anUser.get("password").getAsString().equals(pwd))) {
                    authRes.remove("login_success");
                    authRes.addProperty("login_success", true); //set to true since we got auth sucess
                    // now put the rest of user info
                    authRes.add("user_info",anUser);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authRes;
    }

    public JsonArray getMyOrders(String userName) {
       // Log.i("getMyOrders",orders.toString());

        JsonArray retOrders = new JsonArray();
        try {
            // search for all orders of user.
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).getAsJsonObject().get("name").getAsString().equals(userName)) {
                    retOrders.add(orders.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return retOrders;
        }
    }

    public JsonArray getMyInstOrders(String instName) {
        // this is an admin feature.
        Log.i("getMyInstOrders",orders.toString());
        JsonArray retOrders = new JsonArray();
        try {
            // search for all orders of user.
            for (int i = 0; i < orders.size(); i++) {
                   if (orders.get(i).getAsJsonObject().get("inst").getAsString().equals(instName)) {
                    retOrders.add(orders.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return retOrders;
        }
    }

    public JsonObject getOrderDetails(int orderID) {
        JsonObject retOrderDetails = new JsonObject();
        try {
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).getAsJsonObject().get("order_id").getAsInt() == orderID) {
                    retOrderDetails = orders.get(i).getAsJsonObject();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return retOrderDetails;
        }
    }

    public Boolean addOrder(JsonObject anOrder) {
        try {
            orders.add(anOrder);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public JsonObject updateOrder(int orderID, JsonObject updatedOrder) {
        JsonObject targetOrder = new JsonObject();
        try {
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).getAsJsonObject().get("order_id").getAsInt() == orderID) {
                    targetOrder = orders.get(i).getAsJsonObject();
                    orders.remove(i);
                    orders.add(updatedOrder);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return targetOrder;
        }
    }


    public JsonObject createOrderObj(String username, String inst, ArrayList<HashMap<String, String>> itemsList, int totalCost)
    {
        JsonObject retOrder = new JsonObject();
        try {
            retOrder.addProperty("name", username);
            retOrder.addProperty("inst", inst);

            Gson gson = new Gson();
            JsonArray selItems = gson.toJsonTree(itemsList).getAsJsonArray();
            // now items array
            JsonArray items = new JsonArray();
            for (int i = 0; i < selItems.size(); i++) {
                JsonObject anItem = new JsonObject();
                anItem.addProperty("name", selItems.get(i).getAsJsonObject().get("name").getAsString());
                anItem.addProperty("count", selItems.get(i).getAsJsonObject().get("count").getAsString());
                items.add(anItem);
            }
            retOrder.add("items", items);
            retOrder.addProperty("order_id", orderIDCount++);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            retOrder.addProperty("order_date", formatter.format(new Date())); // put order date and today
            retOrder.addProperty("order_status", "New"); // all starts with new
            retOrder.addProperty("total_cost", totalCost); //
            retOrder.addProperty("paid", false); //

            addOrder(retOrder);

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return retOrder;
    }

    public Boolean registerUser(String username, String password, String mobile,String inst, Boolean isAdmin)
    {
        try{
            JsonObject userObj = fillData(username,password,inst,isAdmin,mobile);
            users.add(userObj);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private JsonObject fillData(String userName,String password, String instName, Boolean isAdmin, String mobile) {
        JsonObject retUser = new JsonObject();
        try {
            retUser.addProperty("name", userName);
            retUser.addProperty("password", password);
            retUser.addProperty("inst", instName);
            retUser.addProperty("is_admin", isAdmin);
            retUser.addProperty("mobile", mobile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return retUser;
        }
    }

    //////// STARTING TEST DATA LOADING RELATED FUNCTIONS /////////
    private void addTestClothTypes() {
        // now add some test cloth types
        try {
            JsonObject clothType1 = new JsonObject();
            JsonObject clothType2 = new JsonObject();
            JsonObject clothType3 = new JsonObject();

            clothType1.addProperty("name", "Shirt");
            clothType1.addProperty("price", "10");
            clothTypes.add(clothType1);

            clothType2.addProperty("name", "Pants");
            clothType2.addProperty("price", "12");
            clothTypes.add(clothType2);

            clothType3.addProperty("name", "Jeans");
            clothType3.addProperty("price", "15");
            clothTypes.add(clothType3);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//////// ENDING TEST DATA LOADING RELATED FUNCTIONS /////////

