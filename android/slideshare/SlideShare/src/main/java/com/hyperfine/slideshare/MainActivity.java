package com.hyperfine.slideshare;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import static com.hyperfine.slideshare.Config.D;
import static com.hyperfine.slideshare.Config.E;

public class MainActivity extends Activity {

    public final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(D)Log.d(TAG, "MainActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(D)Log.d(TAG, "MainActivity.onCreateOptionsMenu");

        super.onCreateOptionsMenu(menu);

        // BUGBUG
        MenuItem csa = menu.add("Create slides");
        csa.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, CreateSlidesActivity.class);
                MainActivity.this.startActivity(intent);
                return true;
            }
        });

        // BUGBUG
        MenuItem psa = menu.add("Play slides");
        psa.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, PlaySlidesActivity.class);
                MainActivity.this.startActivity(intent);
                return true;
            }
        });

        // BUGBUG - test menu item
        MenuItem trp = menu.add("TestRecordPlay");
        trp.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, TestRecordPlayActivity.class);
                MainActivity.this.startActivity(intent);
                return true;
            }
        });

        // BUGBUG - test menu item
        MenuItem tip = menu.add("TestImagePicker");
        tip.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, TestImagePickerActivity.class);
                MainActivity.this.startActivity(intent);
                return true;
            }
        });

        return true;
    }
}
