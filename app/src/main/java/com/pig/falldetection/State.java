package com.pig.falldetection;

import java.util.ArrayList;

public class State{
    public static final State instance = new State();
    private ArrayList<ListItem> listItems = new ArrayList<>();

    private State() {
        listItems.add(new ListItem("Johnny Depp", "0764793163"));
//        listItems.add(new ListItem("Johnny Beep", "0764504313"));
    }
    public void addItem(ListItem  item) {
        this.listItems.add(item);
    }
    public ArrayList<ListItem>  getState() {
        return listItems;
    }
}
