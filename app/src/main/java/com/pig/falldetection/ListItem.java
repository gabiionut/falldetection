package com.pig.falldetection;

public class ListItem {
    String name;
    String phone;

    ListItem(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return name;
    }
}
