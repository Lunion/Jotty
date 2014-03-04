package com.lawenlerk.jotcash;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.lawenlerk.jotcash.database.TransactionsTable;
import com.lawenlerk.jotcash.provider.EntriesProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by En Lerk on 2/6/14.
 */
public class RecordFragment extends Fragment
        implements CalendarDatePickerDialog.OnDateSetListener,
        NumberPickerDialogFragment.NumberPickerDialogHandler,
        LoaderManager.LoaderCallbacks<Cursor> {
    Transaction transaction = new Transaction();

    View view;
    Button btAmount;
    RadioButton rbExpense;
    RadioButton rbIncome;
    Button btDatePicker;
    ListView lvCategoryPicker;
    EditText etCategorySearch;
    ImageButton ibCategoryAdd;

    SimpleCursorAdapter expenseAdapter;
    SimpleCursorAdapter incomeAdapter;

    // Constants for loader use
    private static final int EXPENSE = 1;
    private static final int INCOME = 2;

    private Uri transactionUri = null;


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
        if (transactionUri == null) {
            // This is not an existing transaction
            launchNumberPicker();
        }
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
            rbExpense = (RadioButton) view.findViewById(R.id.rbExpense);
            rbExpense.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((RadioButton) view).isChecked()) {
                        setType(Transaction.EXPENSE);
                    }
                }
            });

            rbIncome = (RadioButton) view.findViewById(R.id.rbIncome);
            rbIncome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((RadioButton) view).isChecked()) {
                        setType(Transaction.INCOME);
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


            // Set up category list
            lvCategoryPicker = (ListView) view.findViewById(R.id.lvCategoryPicker);
            loadCategories();

            lvCategoryPicker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Log.d("RecordFragment", Long.toString(id));
                    Cursor cursor;
                    if (transaction.category.equals(Transaction.EXPENSE)) {
                        cursor = expenseAdapter.getCursor();
                    } else {
                        cursor = incomeAdapter.getCursor();
                    }
                    cursor.moveToPosition(position);
                    transaction.category = cursor.getString(cursor.getColumnIndex(TransactionsTable.CATEGORY));
                    saveTransaction(transaction);
                }
            });

            // Set up category editText
            etCategorySearch = (EditText) view.findViewById(R.id.etCategorySearch);

            // Set up category image button
            ibCategoryAdd = (ImageButton) view.findViewById(R.id.ibCategoryAdd);
            ibCategoryAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (etCategorySearch.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), getString(R.string.category_prompt), Toast.LENGTH_SHORT).show();
                    } else {
                        transaction.category = etCategorySearch.getText().toString();
                        saveTransaction(transaction);
                    }
                }
            });

            Bundle extras = getActivity().getIntent().getExtras();
            // Check from savedInstanceState
            if (savedInstanceState != null) {
                transactionUri = (Uri) savedInstanceState.getParcelable(EntriesProvider.CONTENT_ITEM_TYPE);
/*                loadTransaction(transactionUri);*/
            } else if (extras != null) {
                transactionUri = extras.getParcelable(EntriesProvider.CONTENT_ITEM_TYPE);
                loadTransaction(transactionUri);
            } else {
                // Initialise values
                setAmount(0.0);
                setType(Transaction.EXPENSE);
                setDate(Calendar.getInstance());
            }
        }

        return view;
    }

    private void saveTransaction(Transaction transaction) {
        if (transactionUri == null) {
            insertTransaction(transaction);
        } else {
            updateTransaction(transaction);
        }
        getActivity().finish();
    }

    private void updateTransaction(Transaction transaction) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TransactionsTable.AMOUNT, transaction.amount);
        Log.i("RecordFragment", "Put " + transaction.amount);

        contentValues.put(TransactionsTable.TYPE, transaction.type);
        Log.i("RecordFragment", "Put " + transaction.type);

        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        contentValues.put(TransactionsTable.DATE, dateFormat.format(transaction.date.getTime()));
        Log.i("RecordFragment", "Put " + dateFormat.format(transaction.date.getTime()));

        contentValues.put(TransactionsTable.DESCRIPTION, transaction.description);
        Log.i("RecordFragment", "Put " + transaction.description);

        contentValues.put(TransactionsTable.CATEGORY, transaction.category);
        Log.i("RecordFragment", "Put " + transaction.category);

        getActivity().getContentResolver().update(transactionUri, contentValues, null, null);

    }

    private void loadTransaction(Uri transactionUri) {
        String[] projection = {
                TransactionsTable.AMOUNT,
                TransactionsTable.TYPE,
                TransactionsTable.DATE,
                TransactionsTable.DESCRIPTION,
                TransactionsTable.CATEGORY
        };
        Log.d("RecordFragment", "Loading: " + transactionUri.toString());
        Cursor cursor = getActivity().getContentResolver().query(transactionUri, projection, null, null, null);
        Log.d("RecordFragment", Integer.toString(cursor.getCount()));
        cursor.moveToFirst();
        setAmount(cursor.getDouble(cursor.getColumnIndex(TransactionsTable.AMOUNT)));
        setType(cursor.getString(cursor.getColumnIndex(TransactionsTable.TYPE)));

        String dateString = cursor.getString(cursor.getColumnIndex(TransactionsTable.DATE));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        try {
            setDate(simpleDateFormat.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void loadCategories() {
        String[] fromColumns = {TransactionsTable.CATEGORY};
        int[] toViews = {android.R.id.text1};
        expenseAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
        incomeAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
        getLoaderManager().initLoader(EXPENSE, null, this);
        getLoaderManager().initLoader(INCOME, null, this);
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

    public void setDate(Date date) {
        transaction.date.setTime(date);
        updateDate();
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

    public void setType(String type) {
        transaction.type = type;
        if (type.equals(Transaction.EXPENSE)) {
            lvCategoryPicker.setAdapter(expenseAdapter);
            if (!rbExpense.isChecked()) {
                rbExpense.setChecked(true);
            }
        } else {
            lvCategoryPicker.setAdapter(incomeAdapter);
            if (!rbIncome.isChecked()) {
                rbIncome.setChecked(true);
            }
        }
    }

    private void updateDatePicker() {
        // Format date into string
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));
        String dateString = simpleDateFormat.format(transaction.date.getTime());
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

    private void insertTransaction(Transaction transaction) {
        // Things to generate: timeCreated
        // Things to save: Amount, TransactionType, Date, Description, Category

        ContentValues contentValues = new ContentValues();

        transaction.timeCreated = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat(getString(R.string.database_date_time_format));
        contentValues.put(TransactionsTable.TIME_CREATED, dateFormat.format(transaction.timeCreated.getTime()));
        Log.i("RecordFragment", "Put " + dateFormat.format(transaction.timeCreated.getTime()));

        contentValues.put(TransactionsTable.AMOUNT, transaction.amount);
        Log.i("RecordFragment", "Put " + transaction.amount);

        contentValues.put(TransactionsTable.TYPE, transaction.type);
        Log.i("RecordFragment", "Put " + transaction.type);

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
        String[] projection = {
                TransactionsTable.ID + " AS _id",
                TransactionsTable.CATEGORY, // for convenience
        };
        String selection;
        String[] selectionArgs;
        String sortOrder = "MAX(" + TransactionsTable.TIME_CREATED + ") DESC";

        switch (id) {
            case EXPENSE:
                selection = TransactionsTable.TYPE + "=?";
                selectionArgs = new String[]{Transaction.EXPENSE};
                return new CursorLoader(getActivity(), EntriesProvider.CATEGORIES_URI, projection, selection, selectionArgs, sortOrder);
            case INCOME:
                selection = TransactionsTable.TYPE + "=?";
                selectionArgs = new String[]{Transaction.INCOME};
                return new CursorLoader(getActivity(), EntriesProvider.CATEGORIES_URI, projection, selection, selectionArgs, sortOrder);
            default:
                throw new IllegalArgumentException("Invalid id for Loader: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case EXPENSE:
                expenseAdapter.swapCursor(data);
                break;
            case INCOME:
                incomeAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case EXPENSE:
                expenseAdapter.swapCursor(null);
                break;
            case INCOME:
                incomeAdapter.swapCursor(null);
                break;
        }
    }
}
