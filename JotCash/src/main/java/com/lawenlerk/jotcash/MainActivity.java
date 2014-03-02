package com.lawenlerk.jotcash;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.lawenlerk.jotcash.provider.EntriesProvider;

import java.util.Calendar;

public class MainActivity extends ActionBarActivity implements
        RecordFragment.OnDatePickerButtonClickedListener,
        DatePickerFragment.OnDatePickerDoneListener {
    RecordFragment recordFragment;
    OverviewFragment overviewFragment;

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
                ActionBar actionBar = getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled(true);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void launchRecordFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        recordFragment = new RecordFragment();
        fragmentTransaction.add(R.id.fragment_container, recordFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        overviewFragment = new OverviewFragment();
        fragmentTransaction.add(R.id.fragment_container, overviewFragment);
        fragmentTransaction.commit();

        /*
        recordFragment = new RecordFragment();
        fragmentTransaction.add(R.id.fragment_container, recordFragment);
        fragmentTransaction.commit();
        */
    }

    @Override
    public void onDatePickerClicked() {
        android.support.v4.app.DialogFragment datePickerFragment = new DatePickerFragment();

        // Put date date into argument to pass to datePickerFragment
        Bundle args = new Bundle();
        args.putInt("year", recordFragment.date.get(Calendar.YEAR));
        args.putInt("month", recordFragment.date.get(Calendar.MONTH));
        args.putInt("dayOfMonth", recordFragment.date.get(Calendar.DAY_OF_MONTH));
        datePickerFragment.setArguments(args);

        datePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDatePickerDone(int year, int month, int day) {
        recordFragment.setDate(year, month, day);
    }

}
