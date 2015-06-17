package com.sudo_code.ndrenderer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class MainMenu extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void hypercubeClicked(View view) {
        Intent myIntent = new Intent(MainMenu.this, MainActivity.class);
        myIntent.putExtra("objectType", "hypercube");
        MainMenu.this.startActivity(myIntent);
    }

    public void hypertorusClicked(View view) {
        Intent myIntent = new Intent(MainMenu.this, MainActivity.class);
        myIntent.putExtra("objectType", "hypertorus");
        MainMenu.this.startActivity(myIntent);
    }

    public void complexGraphClicked(View view) {
        Intent myIntent = new Intent(MainMenu.this, MainActivity.class);
        myIntent.putExtra("objectType", "complexGraph");
        MainMenu.this.startActivity(myIntent);
    }

    public void helpClicked(View view) {
        Intent myIntent = new Intent(MainMenu.this, HelpScreen.class);
        MainMenu.this.startActivity(myIntent);
    }

    public void hypercubeHelpClicked(View view) {
        Intent myIntent = new Intent(MainMenu.this, HypercubeHelp.class);
        MainMenu.this.startActivity(myIntent);
    }

    public void hypertorusHelpClicked(View view) {
        Intent myIntent = new Intent(MainMenu.this, HypertorusHelp.class);
        MainMenu.this.startActivity(myIntent);
    }

    public void complexParabolaHelpClicked(View view) {
        Intent myIntent = new Intent(MainMenu.this, ComplexGraphHelp.class);
        MainMenu.this.startActivity(myIntent);
    }

    public void settingsClicked(View view) {
        Intent myIntent = new Intent(MainMenu.this, Settings.class);
        MainMenu.this.startActivity(myIntent);
    }
}
