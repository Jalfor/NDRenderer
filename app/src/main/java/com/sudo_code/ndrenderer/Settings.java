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

        Spinner spinner = (Spinner) findViewById(R.id.color_spinner);
        spinner.setOnItemSelectedListener(this);

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

        if (parent.getItemAtPosition(pos).toString().equals("Red")) {
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
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}