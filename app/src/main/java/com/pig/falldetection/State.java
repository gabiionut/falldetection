package com.pig.falldetection;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class State{
    public static final State instance = new State();
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());

    private ArrayList<ListItem> listItems;
    Type listOfItems = new TypeToken<List<ListItem>>() {}.getType();
    String LIST_ITEMS = "LIST_ITEMS";

    private boolean isDetectionActive;
    String IS_DETECTION_ACTIVE = "IS_DETECTION_ACTIVE";

    private float duration;
    String DURATION = "DURATION";

    private State() {
        listItems = new Gson().fromJson(preferences.getString(LIST_ITEMS, "[]"), listOfItems);
        isDetectionActive = preferences.getBoolean(IS_DETECTION_ACTIVE, true);
        duration = preferences.getFloat(DURATION, 5);
    }
    public void addItem(ListItem  item) {
        this.listItems.add(item);
        String strList = new Gson().toJson(listItems, listOfItems);
        preferences.edit().putString(LIST_ITEMS, strList).apply();
    }
    public ArrayList<ListItem>  getState() {
        return listItems;
    }
    public void removeItem(int index) {
        this.listItems.remove(index);
        String strList = new Gson().toJson(listItems, listOfItems);
        preferences.edit().putString(LIST_ITEMS, strList).apply();
    }

    public void toggleDetectionStatus() {
        this.isDetectionActive = !this.isDetectionActive;
        preferences.edit().putBoolean(IS_DETECTION_ACTIVE, isDetectionActive).apply();
    }

    public boolean getDetectionStatus() {
        return isDetectionActive;
    }

    public void setDuration(float newDuration) {
        duration = newDuration;
        preferences.edit().putFloat(DURATION, duration).apply();
    }

    public float getDuration() {
        return duration;
    }
}
