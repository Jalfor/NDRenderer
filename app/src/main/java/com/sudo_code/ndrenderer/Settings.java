package com.sudo_code.ndrenderer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;

public class Settings extends Activity implements AdapterView.OnItemSelectedListener {

    private SharedPreferences mSharedPref;
    SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        Spinner spinner1 = (Spinner) findViewById(R.id.color_spinner);
        spinner1.setOnItemSelectedListener(this);

        Spinner spinner2 = (Spinner) findViewById(R.id.dim_spinner);
        spinner2.setOnItemSelectedListener(this);

        SharedPreferences mSharedPref = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        if (parent.getItemAtPosition(pos).toString().equals("White")) {
            mEditor.putString("color", "White");
            mEditor.commit();
        }

        else if (parent.getItemAtPosition(pos).toString().equals("Red")) {
            mEditor.putString("color", "Red");
            mEditor.commit();
        }

        else if (parent.getItemAtPosition(pos).toString().equals("Green")) {
            mEditor.putString("color", "Green");
            mEditor.commit();
        }

        else if (parent.getItemAtPosition(pos).toString().equals("Blue")) {
            mEditor.putString("color", "Blue");
            mEditor.commit();
        }

        else if (parent.getItemAtPosition(pos).toString().equals("3D")) {
            mEditor.putInt("dims", 3);
            mEditor.commit();
        }

        else if (parent.getItemAtPosition(pos).toString().equals("4D")) {
            mEditor.putInt("dims", 4);
            mEditor.commit();
        }

        else if (parent.getItemAtPosition(pos).toString().equals("5D")) {
            mEditor.putInt("dims", 5);
            mEditor.commit();
        }

        else if (parent.getItemAtPosition(pos).toString().equals("6D")) {
            mEditor.putInt("dims", 6);
            mEditor.commit();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}