package com.lawenlerk.jotcash;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
    RecordFragment recordFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_transaction:
                launchRecordFragment();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchRecordFragment() {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Log.d(MainActivity.class.getName(), "onCreate()");

/*        if (savedInstanceState == null) {
            OverviewFragment overviewFragment = new OverviewFragment();
            overviewFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, overviewFragment, "overviewFragment").commit();
        } else {
            OverviewFragment overviewFragment = (OverviewFragment) getSupportFragmentManager().findFragmentByTag("overviewFragment");
        }*/

        FragmentManager fragmentManager = getSupportFragmentManager();

        OverviewFragment overviewFragment = (OverviewFragment) fragmentManager.findFragmentByTag("overviewFragment");

        if (overviewFragment == null) {
            // Creating new fragment
            Log.d(MainActivity.class.getName(), "Creating new fragment");
            overviewFragment = new OverviewFragment();
            fragmentManager.beginTransaction().add(R.id.fragment_container, overviewFragment, "overviewFragment").commit();
        }
    }
}
