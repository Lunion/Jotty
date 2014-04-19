package com.lunion.jotty.fragments;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lawenlerk.jotty.R;
import com.lunion.jotty.Transaction;
import com.lunion.jotty.Utilities;
import com.lunion.jotty.activities.TransactionActivity;
import com.lunion.jotty.database.TransactionsTable;
import com.lunion.jotty.provider.EntriesProvider;

import java.util.Date;


public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int CURSOR_LOADER_ID = -99; // Arbitrarily picked integer to avoid clash with group positions
    private View view;
    private ListView lvDays;
    private SimpleCursorAdapter mAdapter;

    public static HistoryFragment newInstance() {
        HistoryFragment historyFragment = new HistoryFragment();
        return historyFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(HistoryFragment.class.getName(), "onCreateView()");

        view = inflater.inflate(R.layout.fragment_history, container, false);

        assert view != null;
        lvDays = (ListView) view.findViewById(R.id.lvDays);


        String[] from = {
                TransactionsTable.CATEGORY,
                TransactionsTable.DESCRIPTION,
                TransactionsTable.AMOUNT
        };
        int[] to = {
                R.id.tvTransactionCategory,
                R.id.tvTransactionDescription,
                R.id.tvTransactionAmount
        };

        mAdapter = new DaySimpleCursorAdapter(getActivity(), R.layout.transaction_list_item, null, from, to, 0);

        lvDays.setAdapter(mAdapter);

        lvDays.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO setup onItemClick
                Intent intent = new Intent(getActivity(), TransactionActivity.class);
                Uri transactionUri = Uri.parse(EntriesProvider.TRANSACTIONS_URI + "/" + id);
                intent.putExtra(EntriesProvider.CONTENT_ITEM_TYPE, transactionUri);
                startActivity(intent);
            }
        });

/*        elvDays.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("HistoryFragment", Long.toString(id));
                Intent intent = new Intent(getActivity(), TransactionActivity.class);
                Uri transactionUri = Uri.parse(EntriesProvider.TRANSACTIONS_URI + "/" + id);
                intent.putExtra(EntriesProvider.CONTENT_ITEM_TYPE, transactionUri);
                startActivity(intent);
                return false;
            }
        });*/

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(CURSOR_LOADER_ID, null, this);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;
        String sortOrder;

/*        projection = new String[]{
                TransactionsTable.DATE + " AS _id",
                TransactionsTable.DATE,
        };
        sortOrder = TransactionsTable.DATE + " DESC";

        Log.d(HistoryFragment.class.getName(), "Creating group loader");
        return new CursorLoader(getActivity(), EntriesProvider.DAYS_URI, projection, null, null, sortOrder);*/

        projection = new String[]{
                TransactionsTable.ID + " AS _id",
                TransactionsTable.DATE,
                TransactionsTable.TYPE,
                TransactionsTable.CATEGORY,
                TransactionsTable.DESCRIPTION,
                TransactionsTable.AMOUNT
        };
        sortOrder = TransactionsTable.DATE + " DESC" + ", " + TransactionsTable.TIME_CREATED + " DESC";

        Log.d(HistoryFragment.class.getName(), "Creating loader");
        return new CursorLoader(getActivity(), EntriesProvider.TRANSACTIONS_URI, projection, null, null, sortOrder);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(HistoryFragment.class.getName(), "onLoadFinished()");

        // Handle processing/translation of cursor data here
        // e.g. converting date string and currency into user format
        Log.d(HistoryFragment.class.getName(), "CURSOR_LOADER_ID");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private class DaySimpleCursorAdapter extends SimpleCursorAdapter {
        public DaySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Cursor cursor = getCursor();
            cursor.moveToPosition(position);

            // Perform conversions of display formats
            if (view != null) {
                TextView tvTransactionAmount = (TextView) view.findViewById(R.id.tvTransactionAmount);
                assert tvTransactionAmount.getText() != null;
                String amountString = tvTransactionAmount.getText().toString();
                amountString = Utilities.formatCurrency(Double.parseDouble(amountString));
                tvTransactionAmount.setText(amountString);

                String transactionType = cursor.getString(cursor.getColumnIndex(TransactionsTable.TYPE));
                assert transactionType != null;
                if (transactionType.equals(Transaction.EXPENSE)) {
                    tvTransactionAmount.setTextColor(getResources().getColor(R.color.expense));
                } else {
                    tvTransactionAmount.setTextColor(getResources().getColor(R.color.income));
                }

                // Find out if we need to add a date header on top of this view
                LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.llHeader);
                linearLayout.removeAllViews();
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                // First, get the date of the current transaction
                String transactionDate = cursor.getString(cursor.getColumnIndex(TransactionsTable.DATE));
                assert transactionDate != null;
                if (position > 0) {
                    cursor.moveToPrevious();
                }
                String previousTransactionDate = cursor.getString(cursor.getColumnIndex(TransactionsTable.DATE));
                assert previousTransactionDate != null;
                cursor.moveToPosition(position); // reset the cursor position before we forget
                // If the current transaction is a first transaction or the transaction has a different date than the previous transaction then add a header
                if (position == 0 || !transactionDate.equals(previousTransactionDate)) {
                    View headerView = layoutInflater.inflate(R.layout.day_list_item, null);
                    assert headerView != null;
                    TextView tvDay = (TextView) headerView.findViewById(R.id.tvDay);
                    TextView tvDate = (TextView) headerView.findViewById(R.id.tvDate);

                    Date currentDate = Utilities.parseDate(transactionDate, getString(R.string.database_date_format));
                    String dayOfWeekString = Utilities.formatDate(currentDate, getString(R.string.day_of_week_date_format));
                    dayOfWeekString = dayOfWeekString.toUpperCase();
                    tvDay.setText(dayOfWeekString);
                    tvDate.setText(Utilities.formatDate(currentDate, getString(R.string.overview_date_format)));

                    linearLayout.addView(headerView, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }


            return view;
        }
    }
}
