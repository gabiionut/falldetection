package com.pig.falldetection;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyListAdapter extends ArrayAdapter<ListItem> {

    private final Activity context;
    private final ArrayList<ListItem> listItems;

    public MyListAdapter(Activity context, ArrayList<ListItem> listItems) {
        super(context, R.layout.phone_list, listItems);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.listItems= listItems;

    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.phone_list, null,true);

        TextView titleText = rowView.findViewById(R.id.label);
        TextView subtitleText =  rowView.findViewById(R.id.mtrl_list_item_secondary_text);
        ImageButton deleteButton = rowView.findViewById(R.id.delete_btn);

        deleteButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Sterge contactul")
                    .setMessage("Sunteti sigur ca doriti sa stergeti acest contact?")
                    .setPositiveButton("Da", (dialog, which) -> {
                        State.instance.removeItem(position);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("Anuleaza", null)
                    .show();
        });

        titleText.setText(listItems.get(position).name);
        subtitleText.setText(listItems.get(position).phone);

        return rowView;

    };
}