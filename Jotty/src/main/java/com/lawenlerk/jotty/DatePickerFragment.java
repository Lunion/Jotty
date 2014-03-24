package com.lawenlerk.jotty;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by En Lerk on 2/4/14.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    OnDatePickerDoneListener mCallback;

    public interface OnDatePickerDoneListener {
        public void onDatePickerDone(int year, int month, int day);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();

        int year = getArguments().getInt("year", c.get(Calendar.YEAR));
        int month = getArguments().getInt("month", c.get(Calendar.MONTH));
        int dayOfMonth = getArguments().getInt("dayOfMonth", c.get(Calendar.DAY_OF_MONTH));

        return new DatePickerDialog(getActivity(), this, year, month, dayOfMonth);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnDatePickerDoneListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDatePickerDoneListener");
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        mCallback.onDatePickerDone(year, month, day);

    }
}
