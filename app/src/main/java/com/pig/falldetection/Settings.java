package com.pig.falldetection;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class Settings extends AppCompatActivity {
    ListView list;

    ArrayList<ListItem> listItems = new ArrayList<ListItem>();


    FloatingActionButton fab;

    private String name = "";
    private String phone = "";
    String[] COUNTRIES = new String[] {"5 min", "10 min", "15 min", "20 min"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        setActionBarColor();
        setDropdownTypeNull();
        fab = findViewById(R.id.fab);

        MyListAdapter adapter = getMyListAdapter();

        setDropdownAdapter();

        fab.setOnClickListener((View v) -> {
            LinearLayout phoneInputLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.phone_input,  null);
            TextInputEditText phoneInput = phoneInputLayout.findViewById(R.id.phone_input_field);
            TextInputEditText nameInput = phoneInputLayout.findViewById(R.id.name_input_field);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Adauga o noua persoana de contact");

            builder.setView(phoneInputLayout);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    name = nameInput.getText().toString();
                    phone = phoneInput.getText().toString();
                    listItems.add(new ListItem(name, phone));
                    Log.d(phone, name);
                    adapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        });
    }

    private MyListAdapter getMyListAdapter() {
        listItems.add(new ListItem("Johnny Depp", "0253273812"));
        listItems.add(new ListItem("Johnny Beep", "5656455525"));
        MyListAdapter adapter = new MyListAdapter(this, listItems);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        return adapter;
    }

    private void setDropdownAdapter() {
        ArrayAdapter<String> dropdownAdapter =
                new ArrayAdapter<>(
                        getApplicationContext(),
                        R.layout.dropdown_menu_popup_item,
                        COUNTRIES);

        AutoCompleteTextView editTextFilledExposedDropdown =
                findViewById(R.id.filled_exposed_dropdown);
        editTextFilledExposedDropdown.setAdapter(dropdownAdapter);
    }

    private void setActionBarColor() {
        ActionBar actionBar;
        actionBar = getSupportActionBar();

        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#004ba0"));

        actionBar.setBackgroundDrawable(colorDrawable);
    }

    public void setDropdownTypeNull()
    {
        AutoCompleteTextView dropdown = findViewById(R.id.filled_exposed_dropdown);
        dropdown.setInputType(InputType.TYPE_NULL);
    }

}
