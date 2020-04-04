package com.pig.falldetection;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        listItems.add(new ListItem("Johnny Depp", "0253273812"));
        listItems.add(new ListItem("Johnny Beep", "5656455525"));
        fab = findViewById(R.id.fab);

        MyListAdapter adapter = new MyListAdapter(this, listItems);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

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

}
