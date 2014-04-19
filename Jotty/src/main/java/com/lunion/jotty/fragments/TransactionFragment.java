package com.lunion.jotty.fragments;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.lawenlerk.jotty.R;
import com.linearlistview.LinearListView;
import com.lunion.jotty.Transaction;
import com.lunion.jotty.Utilities;
import com.lunion.jotty.database.TransactionsTable;
import com.lunion.jotty.provider.EntriesProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by En Lerk on 2/6/14.
 */
public class TransactionFragment extends Fragment
        implements CalendarDatePickerDialog.OnDateSetListener,
        NumberPickerDialogFragment.NumberPickerDialogHandler,
        LoaderManager.LoaderCallbacks<Cursor> {
    // Constants for loader use
    private static final int EXPENSE_LOADER = 1;
    private static final int INCOME_LOADER = 2;
    Transaction transaction = new Transaction();

    View view;
    Button btAmount;
    RadioButton rbExpense;
    RadioButton rbIncome;
    Button btDatePicker;
    EditText etDescription;
    LinearListView llvCategoryPicker;
    EditText etCategorySearch;
    ImageButton ibCategoryAdd;
    SimpleCursorAdapter expenseAdapter;
    SimpleCursorAdapter incomeAdapter;
    Boolean expenseAdapterSwapped = false;
    Boolean incomeAdapterSwapped = false;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_transaction, container, false);
        getActivity().setTitle(getString(R.string.add_transaction));

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
                    launchCalendarDatePicker();

                }
            });

            // Set up description
            etDescription = (EditText) view.findViewById(R.id.etDescription);

            // Set up category list
            llvCategoryPicker = (LinearListView) view.findViewById(R.id.llvCategoryPicker);
            loadCategories();

            llvCategoryPicker.setOnItemClickListener(new LinearListView.OnItemClickListener() {
                @Override
                public void onItemClick(LinearListView linearListView, View view, int position, long id) {
                    Log.d("TransactionFragment", Long.toString(id));
                    Cursor cursor;
                    if (transaction.type.equals(Transaction.EXPENSE)) {
                        cursor = expenseAdapter.getCursor();
                    } else {
                        cursor = incomeAdapter.getCursor();
                    }
                    Log.d("TransactionFragment", "cursor.getCount()=" + cursor.getCount());
                    cursor.moveToPosition(position);
                    transaction.category = cursor.getString(cursor.getColumnIndex(TransactionsTable.CATEGORY));
                    cursor.close();
                    saveTransaction(transaction);
                }
            });

            // Set up category etCategorySearch
            etCategorySearch = (EditText) view.findViewById(R.id.etCategorySearch);
            etCategorySearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        attemptSaveTransaction();
                    }
                    return false;
                }
            });

            // Set up category image button
            ibCategoryAdd = (ImageButton) view.findViewById(R.id.ibCategoryAdd);
            ibCategoryAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptSaveTransaction();
                }
            });


            // Check from savedInstanceState
            if (savedInstanceState == null) {
                // This is not a recreation, check if anything was passed in
                Bundle extras = getActivity().getIntent().getExtras();
                if (extras != null) {
                    // This is an existing transaction
                    transactionUri = extras.getParcelable(EntriesProvider.CONTENT_ITEM_TYPE);
                    loadTransaction(transactionUri);

                    getActivity().setTitle(getString(R.string.edit_transaction));

                }
            } else {
                // TODO extract these codes into onRestoreInstanceState
                // This is a recreation, might be new or existing transaction
                // Reload all values saved from last instance
                transactionUri = savedInstanceState.getParcelable(EntriesProvider.CONTENT_ITEM_TYPE);
                setAmount(savedInstanceState.getDouble(TransactionsTable.AMOUNT));
                setType(savedInstanceState.getString(TransactionsTable.TYPE));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
                try {
                    setDate(simpleDateFormat.parse(savedInstanceState.getString(TransactionsTable.DATE)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                setDescription(savedInstanceState.getString(TransactionsTable.DESCRIPTION));
                setCategory(savedInstanceState.getString(TransactionsTable.CATEGORY));

                // Check if there is a existing NumberPickerDialogFragment
                NumberPickerDialogFragment numberPickerDialogFragment = (NumberPickerDialogFragment) getChildFragmentManager().findFragmentByTag("numberPickerDialogFragment");
                if (numberPickerDialogFragment != null) {
                    numberPickerDialogFragment.setTargetFragment(this, 0);
                }

                // Check if there is an existing CalendarDatePickerDialog(Fragment)
                CalendarDatePickerDialog calendarDatePickerDialogFragment = (CalendarDatePickerDialog) getChildFragmentManager().findFragmentByTag("calendarDatePickerDialogFragment");
                if (calendarDatePickerDialogFragment != null) {
                    calendarDatePickerDialogFragment.setOnDateSetListener(TransactionFragment.this);
                }

            }

            // Check if this is an entirely new transaction
            if (transactionUri == null && savedInstanceState == null) {
                // If this is a new transaction and not recreation as well
                // Initialise values
                setAmount(0.0);
                setType(Transaction.EXPENSE);
                setDate(Calendar.getInstance());
                launchNumberPicker();
            }


        }


        return view;
    }

    private void attemptSaveTransaction() {
        if (etCategorySearch.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.category_prompt), Toast.LENGTH_SHORT).show();
        } else {
            transaction.category = etCategorySearch.getText().toString();
            saveTransaction(transaction);
        }
    }

    private void launchCalendarDatePicker() {
        FragmentManager fragmentManager = getChildFragmentManager();

        CalendarDatePickerDialog calendarDatePickerDialogFragment = CalendarDatePickerDialog.newInstance(
                this,
                transaction.date.get(Calendar.YEAR),
                transaction.date.get(Calendar.MONTH),
                transaction.date.get(Calendar.DAY_OF_MONTH));
        calendarDatePickerDialogFragment.show(fragmentManager, "calendarDatePickerDialogFragment");
    }

    private void saveTransaction(Transaction transaction) {
        transaction.description = etDescription.getText().toString();
        if (transactionUri == null) {
            insertTransaction(transaction);
        } else {
            updateTransaction(transaction);
        }
        getActivity().finish();
    }

    public void deleteTransaction() {
        if (transactionUri != null) {
            // Delete from database
            getActivity().getContentResolver().delete(transactionUri, null, null);
            getActivity().finish();
        } else {
            // Just discard this transaction and exit
            getActivity().finish();
        }
    }

    private void updateTransaction(Transaction transaction) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TransactionsTable.AMOUNT, transaction.amount);
        Log.i("TransactionFragment", "Put " + transaction.amount);

        contentValues.put(TransactionsTable.TYPE, transaction.type);
        Log.i("TransactionFragment", "Put " + transaction.type);

        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        contentValues.put(TransactionsTable.DATE, dateFormat.format(transaction.date.getTime()));
        Log.i("TransactionFragment", "Put " + dateFormat.format(transaction.date.getTime()));

        contentValues.put(TransactionsTable.DESCRIPTION, transaction.description);
        Log.i("TransactionFragment", "Put " + transaction.description);

        contentValues.put(TransactionsTable.CATEGORY, transaction.category);
        Log.i("TransactionFragment", "Put " + transaction.category);

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
        Log.d("TransactionFragment", "Loading: " + transactionUri.toString());
        Cursor cursor = getActivity().getContentResolver().query(transactionUri, projection, null, null, null);
        Log.d("TransactionFragment", Integer.toString(cursor.getCount()));
        cursor.moveToFirst();
        setAmount(cursor.getDouble(cursor.getColumnIndex(TransactionsTable.AMOUNT)));
        setType(cursor.getString(cursor.getColumnIndex(TransactionsTable.TYPE)));
        setDescription(cursor.getString(cursor.getColumnIndex(TransactionsTable.DESCRIPTION)));
        String dateString = cursor.getString(cursor.getColumnIndex(TransactionsTable.DATE));
        setCategory(cursor.getString(cursor.getColumnIndex(TransactionsTable.CATEGORY)));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        try {
            setDate(simpleDateFormat.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cursor.close();
    }

    private void setDescription(String description) {
        transaction.description = description;
        etDescription.setText(transaction.description);

    }

    private void setCategory(String category) {
        transaction.category = category;
        etCategorySearch.setText(transaction.category);
    }

    private void loadCategories() {
        String[] fromColumns = {TransactionsTable.CATEGORY};
        int[] toViews = {R.id.tvTransactionCategory};
        expenseAdapter = new SimpleCursorAdapter(getActivity(), R.layout.category_row, null, fromColumns, toViews, 0);
        incomeAdapter = new SimpleCursorAdapter(getActivity(), R.layout.category_row, null, fromColumns, toViews, 0);
        getLoaderManager().initLoader(EXPENSE_LOADER, null, this);
        getLoaderManager().initLoader(INCOME_LOADER, null, this);
    }

    private void launchNumberPicker() {
        FragmentManager fragmentManager = getChildFragmentManager();

        NumberPickerDialogFragment numberPickerDialogFragment = NumberPickerDialogFragment.newInstance(0, R.style.BetterPickersDialogFragment_Light, null, null, View.INVISIBLE, View.VISIBLE, Utilities.getCurrencyString());
        numberPickerDialogFragment.setTargetFragment(this, 0);
        numberPickerDialogFragment.show(fragmentManager, "numberPickerDialogFragment");


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
        updateCategoryPicker();
        if (type.equals(Transaction.EXPENSE)) {
            if (!rbExpense.isChecked()) {
                rbExpense.setChecked(true);
            }
        } else {
            if (!rbIncome.isChecked()) {
                rbIncome.setChecked(true);
            }
        }
    }

    public void updateCategoryPicker() {
        // Workaround to solve issue of nullpointerexception if setAdapter before cursor is fully swapped and parsed
        if (expenseAdapterSwapped && transaction.type.equals(Transaction.EXPENSE)) {
            llvCategoryPicker.setAdapter(expenseAdapter);
        } else if (incomeAdapterSwapped && transaction.type.equals(Transaction.INCOME)) {
            llvCategoryPicker.setAdapter(incomeAdapter);
        }
    }

    private void updateDatePicker() {
        // Format date into string
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_picker_date_format));
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(EntriesProvider.CONTENT_ITEM_TYPE, transactionUri);

        outState.putDouble(TransactionsTable.AMOUNT, transaction.amount);
        outState.putString(TransactionsTable.TYPE, transaction.type);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        outState.putString(TransactionsTable.DATE, simpleDateFormat.format(transaction.date.getTime()));

        outState.putString(TransactionsTable.DESCRIPTION, transaction.description);
        outState.putString(TransactionsTable.CATEGORY, transaction.category);


//        outState.putParcelable();
    }

    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        transaction.amount = Math.round(fullNumber * 100.0) / 100.0;

        setAmount(transaction.amount);
    }

    private String toCurrency(Double amount, int decimals) {
        return Utilities.addCurrencyString(Utilities.toDecimals(amount, decimals));
    }

    private void insertTransaction(Transaction transaction) {
        // Things to generate: timeCreated
        // Things to save: Amount, TransactionType, Date, Description, Category

        ContentValues contentValues = new ContentValues();

        transaction.timeCreated = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat(getString(R.string.database_date_time_format));
        contentValues.put(TransactionsTable.TIME_CREATED, dateFormat.format(transaction.timeCreated.getTime()));
        Log.i("TransactionFragment", "Put " + dateFormat.format(transaction.timeCreated.getTime()));

        contentValues.put(TransactionsTable.AMOUNT, transaction.amount);
        Log.i("TransactionFragment", "Put " + transaction.amount);

        contentValues.put(TransactionsTable.TYPE, transaction.type);
        Log.i("TransactionFragment", "Put " + transaction.type);

        dateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        contentValues.put(TransactionsTable.DATE, dateFormat.format(transaction.date.getTime()));
        Log.i("TransactionFragment", "Put " + dateFormat.format(transaction.date.getTime()));

        contentValues.put(TransactionsTable.DESCRIPTION, transaction.description);
        Log.i("TransactionFragment", "Put " + transaction.description);

        contentValues.put(TransactionsTable.CATEGORY, transaction.category);
        Log.i("TransactionFragment", "Put " + transaction.category);

        getActivity().getContentResolver().insert(EntriesProvider.TRANSACTIONS_URI, contentValues);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                TransactionsTable.CATEGORY + " AS _id",
                TransactionsTable.CATEGORY,
                TransactionsTable.TYPE,
                TransactionsTable.TIME_CREATED
        };
        String selection;
        String[] selectionArgs;
        String sortOrder = TransactionsTable.TIME_CREATED + " DESC";

        switch (id) {
            case EXPENSE_LOADER:
                selection = TransactionsTable.TYPE + "=?";
                selectionArgs = new String[]{Transaction.EXPENSE};
                return new CursorLoader(getActivity(), EntriesProvider.CATEGORIES_URI, projection, selection, selectionArgs, sortOrder);
            case INCOME_LOADER:
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
            case EXPENSE_LOADER:
                expenseAdapter.swapCursor(data);
                expenseAdapterSwapped = true;
                updateCategoryPicker();
                break;
            case INCOME_LOADER:
                incomeAdapter.swapCursor(data);
                incomeAdapterSwapped = true;
                updateCategoryPicker();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case EXPENSE_LOADER:
                expenseAdapter.swapCursor(null);
                break;
            case INCOME_LOADER:
                incomeAdapter.swapCursor(null);
                break;
        }
    }
}