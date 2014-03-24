package com.lawenlerk.jotty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class OldMainActivity extends ActionBarActivity {
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
                launchRecordActivity();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchRecordActivity() {
        Intent intent = new Intent(this, RecordActivity.class);
        startActivity(intent);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Log.d(OldMainActivity.class.getName(), "onCreate()");

/*        if (savedInstanceState == null) {
            HistoryFragment historyFragment = new HistoryFragment();
            historyFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, historyFragment, "historyFragment").commit();
        } else {
            HistoryFragment historyFragment = (HistoryFragment) getSupportFragmentManager().findFragmentByTag("historyFragment");
        }*/

        FragmentManager fragmentManager = getSupportFragmentManager();

        HistoryFragment historyFragment = (HistoryFragment) fragmentManager.findFragmentByTag("historyFragment");

        if (historyFragment == null) {
            // Creating new fragment
            Log.d(OldMainActivity.class.getName(), "Creating new fragment");
            historyFragment = new HistoryFragment();
            fragmentManager.beginTransaction().add(R.id.fragment_container, historyFragment, "historyFragment").commit();
        }
    }
}
