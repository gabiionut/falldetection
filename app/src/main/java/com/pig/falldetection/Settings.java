package com.pig.falldetection;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class Settings extends AppCompatActivity {
    ListView list;

    ArrayList<ListItem> listItems = new ArrayList<>();

    FloatingActionButton fab;

    private String name = "";
    private String phone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        setActionBarColor();
        listItems = State.instance.getState();
        fab = findViewById(R.id.fab);

        MyListAdapter adapter = getMyListAdapter();

        fab.setOnClickListener((View v) -> {
            LinearLayout phoneInputLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.phone_input,  null);
            TextInputEditText phoneInput = phoneInputLayout.findViewById(R.id.phone_input_field);
            TextInputEditText nameInput = phoneInputLayout.findViewById(R.id.name_input_field);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add a new contact person");

            builder.setView(phoneInputLayout);

            builder.setPositiveButton("OK", (dialog, which) -> {
                name = nameInput.getText().toString();
                phone = phoneInput.getText().toString();
                State.instance.addItem(new ListItem(name, phone));
                listItems = State.instance.getState();
                Log.d(phone, name);
                adapter.notifyDataSetChanged();
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    private MyListAdapter getMyListAdapter() {
        MyListAdapter adapter = new MyListAdapter(this, listItems);
        list = findViewById(R.id.list);
        list.setAdapter(adapter);
        return adapter;
    }

    private void setActionBarColor() {
        ActionBar actionBar;
        actionBar = getSupportActionBar();

        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#004ba0"));

        actionBar.setBackgroundDrawable(colorDrawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.time_menu, menu);
        return true;
    }

    public void onTimeIconClick(MenuItem mi) {
        LinearLayout phoneInputLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.slider,  null);
        Slider slider = phoneInputLayout.findViewById(R.id.slider);

        slider.addOnChangeListener((slider1, value, fromUser) -> {
            Log.d("Slider", "value");
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose duration until the message is sent");

        builder.setView(phoneInputLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
