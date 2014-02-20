package com.lawenlerk.jotcash;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import java.util.Calendar;

public class MainActivity extends ActionBarActivity implements RecordFragment.OnDatePickerButtonClickedListener, DatePickerFragment.OnDatePickerDoneListener {
    RecordFragment recordFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        recordFragment = (RecordFragment) getSupportFragmentManager().findFragmentById(R.id.recordFragment);
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
