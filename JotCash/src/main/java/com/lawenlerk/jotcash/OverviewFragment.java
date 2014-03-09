package com.lawenlerk.jotcash;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;

import com.lawenlerk.jotcash.database.TransactionsTable;
import com.lawenlerk.jotcash.provider.EntriesProvider;

/**
 * Created by enlerklaw on 2/24/14.
 */
public class OverviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String[] PROJECTION = new String[]{
            TransactionsTable.ID,
            TransactionsTable.AMOUNT
    };
    private static final String SELECTION = null;
    private static final String[] SELECTIONARGS = null;
    private static final String SORTORDER = "date(" + TransactionsTable.DATE + ") DESC" + ", " + "datetime(" + TransactionsTable.TIME_CREATED + ") DESC";
    private static final int DAYS_LOADER = -99; // Arbitrarily picked integer to avoid clash with group positions
    View view;
    //    ListView lvTransactions;
    ExpandableListView elvDays;
    DayExpandableListAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.overview_fragment, container, false);

//        lvTransactions = (ListView) view.findViewById(R.id.lvTransactions);
        elvDays = (ExpandableListView) view.findViewById(R.id.overviewFragment_elvDays);

        String[] fromColumns = {TransactionsTable.AMOUNT};
        int[] toViews = {android.R.id.text1};

        // TODO use ListViewAnimations with ExpandableListView to group days


        String[] groupFrom = {
                TransactionsTable.DATE,
                EntriesProvider.DAY_TOTAL
        };
        int[] groupTo = {
                R.id.dayGroup_tvDate,
                R.id.dayGroup_tvDayTotal
        };

        String[] childFrom = {
                TransactionsTable.CATEGORY,
                TransactionsTable.DESCRIPTION,
                TransactionsTable.AMOUNT
        };
        int[] childTo = {
                R.id.dayChild_tvCategory,
                R.id.dayChild_tvDescription,
                R.id.dayChild_tvAmount
        };

        mAdapter = new DayExpandableListAdapter(getActivity(), this, R.layout.day_collapsedgroup, R.layout.day_expandedgroup, groupFrom, groupTo, R.layout.day_child, R.layout.day_lastchild, childFrom, childTo, null);

        elvDays.setAdapter(mAdapter);

        // Query for days and sum of amounts
        getLoaderManager().initLoader(DAYS_LOADER, null, this);

/*        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
        lvTransactions.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        lvTransactions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                Log.d("OverviewFragment", Long.toString(id));
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                Uri transactionUri = Uri.parse(EntriesProvider.TRANSACTIONS_URI + "/" + id);
                intent.putExtra(EntriesProvider.CONTENT_ITEM_TYPE, transactionUri);
                startActivity(intent);
            }
        });*/


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection;
        String selection;
        String[] selectionArgs;
        String sortOrder;

        switch (id) {
            case DAYS_LOADER:
                projection = new String[]{
                        TransactionsTable.DATE + " AS _id",
                        TransactionsTable.DATE,
                        "SUM(" + TransactionsTable.AMOUNT + ") AS " + EntriesProvider.DAY_TOTAL   // Make sure ContentProvider returns a column with this name
                };
                selection = TransactionsTable.TYPE + "=?";
                selectionArgs = new String[]{
                        Transaction.EXPENSE
                };
                sortOrder = TransactionsTable.DATE + " DESC";

                return new CursorLoader(getActivity(), EntriesProvider.DAYS_URI, projection, selection, selectionArgs, sortOrder);

            default:
                // Group position was passed in as loader id
                projection = new String[]{
                        TransactionsTable.ID + " AS _id",
                        TransactionsTable.CATEGORY,
                        TransactionsTable.DESCRIPTION,
                        TransactionsTable.AMOUNT
                };

                // Get the date to query from the args
                selection = TransactionsTable.DATE + "=?";
                selectionArgs = new String[]{
                        args.getString(TransactionsTable.DATE)
                };
                sortOrder = "date(" + TransactionsTable.DATE + ") DESC" + ", " + "datetime(" + TransactionsTable.TIME_CREATED + ") DESC";

                return new CursorLoader(getActivity(), EntriesProvider.TRANSACTIONS_URI, projection, selection, selectionArgs, sortOrder);

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case DAYS_LOADER:
                mAdapter.setGroupCursor(data);
                break;
            default:
                // Group position was passed in as loader id
                // If mAdapter already has a cursor attached to this group position, don't set again to prevent destroying previous cursor and spawn an infinite loop
                mAdapter.setChildrenCursor(loader.getId(), data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case DAYS_LOADER:
                mAdapter.setGroupCursor(null);
                break;
            default:
                mAdapter.setChildrenCursor(loader.getId(), null);
        }
    }

    private void startLoader(int id, Bundle args) {
        getLoaderManager().initLoader(id, args, this);
    }

    private void restartLoader(int id, Bundle args) {
        getLoaderManager().restartLoader(id, args, this);
    }

    public class DayExpandableListAdapter extends SimpleCursorTreeAdapter {
        public DayExpandableListAdapter(Context context, OverviewFragment overviewFragment, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        }

        public DayExpandableListAdapter(Context context, OverviewFragment overviewFragment, int collapsedGroupLayout, int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout, int lastChildLayout, String[] childFrom, int[] childTo, Cursor cursor) {
            super(context, cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, lastChildLayout, childFrom, childTo);
        }

        public DayExpandableListAdapter(Context context, OverviewFragment overviewFragment, int collapsedGroupLayout, int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo, Cursor cursor) {
            super(context, cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        }

        public DayExpandableListAdapter(Context context, OverviewFragment overviewFragment, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo, Cursor cursor) {
            super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            // Given a day group, we return all the transactions within that day

            // First, find out what the date of the current position in groupCursor is
            String dateString = groupCursor.getString(groupCursor.getColumnIndex(TransactionsTable.DATE));

            // Next, put the dateString into the args to pass to the CursorLoader
            Bundle args = new Bundle();
            args.putString(TransactionsTable.DATE, dateString);

            // Call the function to start the loader

            int id = groupCursor.getPosition();
            Loader loader = getActivity().getSupportLoaderManager().getLoader(id);
            if (loader != null && !loader.isReset()) {
                // Restart the loader
                restartLoader(id, args);
            } else {
                startLoader(id, args);
            }
            return null;
        }
    }

}
