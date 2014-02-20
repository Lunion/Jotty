package com.lawenlerk.jotcash;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by En Lerk on 2/6/14.
 */
public class RecordFragment extends Fragment {
    public Calendar date;
    OnDatePickerButtonClickedListener mCallback;
    public interface OnDatePickerButtonClickedListener {
        public void onDatePickerClicked();
    }

    View view;

    Button btDatePicker;
    RadioButton rbYesterday;
    RadioButton rbToday;
    RadioButton rbCustom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnDatePickerButtonClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDatePickerButtonClickedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.record_fragment, container, false);

        if (view != null) {
            btDatePicker = (Button) view.findViewById(R.id.btDatePicker);
            btDatePicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onDatePickerClicked();
                }
            });

            rbYesterday = (RadioButton) view.findViewById(R.id.rbYesterday);
            rbYesterday.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDateRadioButtonClicked(view);
                }
            });
            rbToday = (RadioButton) view.findViewById(R.id.rbToday);
            rbToday.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDateRadioButtonClicked(view);
                }
            });
            rbCustom = (RadioButton) view.findViewById(R.id.rbCustom);
            rbCustom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onDateRadioButtonClicked(view);
                }
            });

            setDate(Calendar.getInstance());
        }

        return view;
    }

    public Calendar getDate() {
        return date;
    }

    private void onDateRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.rbYesterday:
                if (checked) {
                    Calendar yesterday = Calendar.getInstance();
                    yesterday.add(Calendar.DAY_OF_MONTH, -1);
                    setDate(yesterday);
                }
                break;
            case R.id.rbToday:
                if (checked) {
                    Calendar today = Calendar.getInstance();
                    setDate(today);
                }
                break;
            case R.id.rbCustom:
                if (checked) {
                    mCallback.onDatePickerClicked();
                }
                break;
        }
    }

    public void setDate(Calendar date) {
        this.date = date;
        updateDate();
    }

    public void setDate(int year, int month, int dayOfMonth) {
        date.set(year, month, dayOfMonth);
        updateDate();
    }

    private void updateDate() {
        updateDatePicker();
        updateDateRadioGroup();
    }

    private void updateDatePicker() {
        // Format date into string
        SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.date_format));
        String dateString = dateFormatter.format(date.getTime());
        btDatePicker.setText(dateString);

    }

    private void updateDateRadioGroup() {
        Log.d("Debug", "updateDateRadioGroup()");
        Calendar today = Calendar.getInstance();

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        if (sameDay(date, yesterday)) {
            rbYesterday.setChecked(true);
        } else if (sameDay(date, today)) {
            rbToday.setChecked(true);
        } else {
            rbCustom.setChecked(true);
        }
    }

    private boolean sameDay(Calendar a, Calendar b) {
        if (a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)) {
            return true;
        } else {
            return false;
        }
    }

}
