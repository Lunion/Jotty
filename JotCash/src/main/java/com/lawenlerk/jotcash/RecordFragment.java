package com.lawenlerk.jotcash;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.lawenlerk.jotcash.database.TransactionsTable;
import com.lawenlerk.jotcash.provider.EntriesProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by En Lerk on 2/6/14.
 */
public class RecordFragment extends Fragment
        implements CalendarDatePickerDialog.OnDateSetListener,
        NumberPickerDialogFragment.NumberPickerDialogHandler, LoaderManager.LoaderCallbacks<Cursor> {
    Transaction transaction = new Transaction();

    View view;
    Button btAmount;
    Switch swExpenseIncome;
    Button btDatePicker;
    ListView lvCategoryPicker;
    EditText etCategorySearch;
    ImageButton ibCategoryAdd;

    SimpleCursorAdapter mAdapter;
    private static final String[] PROJECTION = {
            "DISTINCT " + TransactionsTable.CATEGORY + " AS _id",
            TransactionsTable.CATEGORY, // for convenience
    };
    private static final String SELECTION = null;
    private static final String[] SELECTIONARGS = null;
    private static final String SORTORDER = "date(" + TransactionsTable.TIME_CREATED + ") DESC";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        launchNumberPicker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.record_fragment, container, false);

        if (view != null) {
            btAmount = (Button) view.findViewById(R.id.etAmount);
            btAmount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchNumberPicker();

                }
            });

            // Set up swExpenseIncome
            swExpenseIncome = (Switch) view.findViewById(R.id.swExpenseIncome);
            swExpenseIncome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isIncome) {
                    if (isIncome) {
                        transaction.transactionType = Transaction.INCOME;

                    } else {
                        transaction.transactionType = Transaction.EXPENSE;

                    }
                }
            });

            // Set up date picker
            btDatePicker = (Button) view.findViewById(R.id.btDatePicker);
            btDatePicker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fragmentManager = getChildFragmentManager();

                    CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog.newInstance(
                            RecordFragment.this,
                            transaction.date.get(Calendar.YEAR),
                            transaction.date.get(Calendar.MONTH),
                            transaction.date.get(Calendar.DAY_OF_MONTH));
                    calendarDatePickerDialog.show(fragmentManager, "calendarDatePickerDialog");

                }
            });
            // Initialise date value to today


            // Set up category list
            lvCategoryPicker = (ListView) view.findViewById(R.id.lvCategoryPicker);
            String[] fromColumns = {TransactionsTable.CATEGORY};
            int[] toViews = {android.R.id.text1};
            mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
            lvCategoryPicker.setAdapter(mAdapter);
            getLoaderManager().initLoader(0, null, this);


            // Set up category editText
            etCategorySearch = (EditText) view.findViewById(R.id.etCategorySearch);


            // Set up category image button
            ibCategoryAdd = (ImageButton) view.findViewById(R.id.ibCategoryAdd);
            ibCategoryAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (etCategorySearch.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Please enter a category", Toast.LENGTH_SHORT).show();
                    } else {
                        transaction.category = etCategorySearch.getText().toString();
                        saveTransaction(transaction);
                        getActivity().finish();
                    }
                }
            });


            // Initialise values
            setAmount(0.0);
            transaction.transactionType = Transaction.EXPENSE;  // default value is expense
            setDate(Calendar.getInstance());
        }

        return view;
    }

    private void launchNumberPicker() {
        NumberPickerBuilder numberPickerBuilder = new NumberPickerBuilder();
        numberPickerBuilder.setFragmentManager(getChildFragmentManager());
        numberPickerBuilder.setStyleResId(R.style.BetterPickersDialogFragment_Light);
        numberPickerBuilder.setTargetFragment(this);
        numberPickerBuilder.setPlusMinusVisibility(View.INVISIBLE);
        numberPickerBuilder.setLabelText("SGD");    // TODO let user select currency string from settings
        numberPickerBuilder.show();
    }

    public void setDate(Calendar date) {
        transaction.date = date;
        updateDate();
    }

    public void setDate(int year, int month, int dayOfMonth) {
        transaction.date.set(year, month, dayOfMonth);
        updateDate();
    }

    private void updateDate() {
        updateDatePicker();
    }

    public void setAmount(double amount) {
        transaction.amount = amount;
        btAmount.setText(toCurrency(amount, 2));
    }

    private void updateDatePicker() {
        // Format date into string
        SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.date_format));
        String dateString = dateFormatter.format(transaction.date.getTime());
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

    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        transaction.amount = Math.round(fullNumber * 100.0) / 100.0;

        setAmount(transaction.amount);
    }

    private String toCurrency(Double amount, int decimals) {
        return padCurrency(padZeroes(amount, decimals));
    }

    private String padZeroes(Double amount, int decimals) {
        String amountString = Double.toString(amount);
        for (int i = amountString.length() - (amountString.indexOf('.') + 1); i < decimals; i++) {
            amountString = amountString + '0';
        }
        return amountString;
    }

    private String padCurrency(String amountString) {
        String currencyString = "SGD"; // TODO extract this into settings
        return amountString + " " + currencyString;
    }

    private void saveTransaction(Transaction transaction) {
        // Things to generate: timeCreated
        // Things to save: Amount, TransactionType, Date, Description, Category

        ContentValues contentValues = new ContentValues();

        transaction.timeCreated = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat(getString(R.string.database_date_time_format));
        contentValues.put(TransactionsTable.TIME_CREATED, dateFormat.format(transaction.timeCreated.getTime()));
        Log.i("RecordFragment", "Put " + dateFormat.format(transaction.timeCreated.getTime()));

        contentValues.put(TransactionsTable.AMOUNT, transaction.amount);
        Log.i("RecordFragment", "Put " + transaction.amount);

        if (transaction.transactionType == Transaction.EXPENSE) {
            contentValues.put(TransactionsTable.TYPE, "EXPENSE");
            Log.i("RecordFragment", "Put " + "EXPENSE");
        } else {
            contentValues.put(TransactionsTable.TYPE, "INCOME");
            Log.i("RecordFragment", "Put " + "INCOME");
        }

        dateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        contentValues.put(TransactionsTable.DATE, dateFormat.format(transaction.date.getTime()));
        Log.i("RecordFragment", "Put " + dateFormat.format(transaction.date.getTime()));

        contentValues.put(TransactionsTable.DESCRIPTION, transaction.description);
        Log.i("RecordFragment", "Put " + transaction.description);

        contentValues.put(TransactionsTable.CATEGORY, transaction.category);
        Log.i("RecordFragment", "Put " + transaction.category);

        getActivity().getContentResolver().insert(EntriesProvider.TRANSACTIONS_URI, contentValues);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), EntriesProvider.TRANSACTIONS_URI, PROJECTION, SELECTION, SELECTIONARGS, SORTORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
