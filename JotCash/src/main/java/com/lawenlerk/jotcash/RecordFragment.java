package com.lawenlerk.jotcash;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by En Lerk on 2/6/14.
 */
public class RecordFragment extends Fragment implements CalendarDatePickerDialog.OnDateSetListener {
    public Calendar date;

    View view;
    EditText etAmount;
    Button btDatePicker;
    ListView lvCategoryPicker;
    ImageButton ibCategoryAdd;

    private List<CategoryItem> data;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.record_fragment, container, false);

        if (view != null) {

            // Set up date picker
            btDatePicker = (Button) view.findViewById(R.id.btDatePicker);
            btDatePicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fragmentManager = getChildFragmentManager();

                    CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog.newInstance(
                            RecordFragment.this,
                            date.get(Calendar.YEAR),
                            date.get(Calendar.MONTH),
                            date.get(Calendar.DAY_OF_MONTH));
                    calendarDatePickerDialog.show(fragmentManager, "calendarDatePickerDialog");

                }
            });
            // Initialise date value to today
            if (date == null) {
                setDate(Calendar.getInstance());
            }

            // Set up category list


            // Set up category adder
            ibCategoryAdd = (ImageButton) view.findViewById(R.id.ibCategoryAdd);
            ibCategoryAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            // Set up category image button
            ibCategoryAdd = (ImageButton) view.findViewById(R.id.ibCategoryAdd);
            ibCategoryAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });


        }

        return view;
    }

    public Calendar getDate() {
        return date;
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
    }

    private void updateDatePicker() {
        // Format date into string
        SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.date_format));
        String dateString = dateFormatter.format(date.getTime());
        btDatePicker.setText(dateString);

    }

    private boolean sameDay(Calendar a, Calendar b) {
        // Checks if a and b occur on the same day
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int month, int dayOfMonth) {
        setDate(year, month, dayOfMonth);

    }

//    public interface OnDatePickerButtonClickedListener {
//        public void onDatePickerClicked();
//    }

}
