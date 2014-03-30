package com.lawenlerk.jotty;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.lawenlerk.jotty.database.TransactionsTable;
import com.lawenlerk.jotty.provider.EntriesProvider;

import java.text.ParseException;


public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GROUP_CURSOR_LOADER = -99; // Arbitrarily picked integer to avoid clash with group positions
    private View view;
    private ExpandableListView elvDays;
    private ExpandableListCursorAdapter mAdapter;

    public static HistoryFragment newInstance() {
        HistoryFragment historyFragment = new HistoryFragment();
        return historyFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(HistoryFragment.class.getName(), "onCreateView()");

        view = inflater.inflate(R.layout.fragment_history, container, false);

        assert view != null;
        elvDays = (ExpandableListView) view.findViewById(R.id.overviewFragment_elvDays);

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

        mAdapter = new ExpandableListCursorAdapter(getActivity(), this, null, R.layout.day_expandedgroup, R.layout.day_collapsedgroup, groupFrom, groupTo, R.layout.day_child, R.layout.day_lastchild, childFrom, childTo);


        elvDays.setAdapter(mAdapter);
        elvDays.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("HistoryFragment", Long.toString(id));
                Intent intent = new Intent(getActivity(), TransactionActivity.class);
                Uri transactionUri = Uri.parse(EntriesProvider.TRANSACTIONS_URI + "/" + id);
                intent.putExtra(EntriesProvider.CONTENT_ITEM_TYPE, transactionUri);
                startActivity(intent);
                return false;
            }
        });

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;
        String selection;
        String[] selectionArgs;
        String sortOrder;

        switch (id) {
            case GROUP_CURSOR_LOADER:
                // TODO perform all analysis in separate analysis unit instead of using SQLite
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
                // Group position was passed in as loader id, load child
                projection = new String[]{
                        TransactionsTable.ID + " AS _id",
                        TransactionsTable.CATEGORY,
                        TransactionsTable.DESCRIPTION,
                        TransactionsTable.AMOUNT,
                        TransactionsTable.TYPE
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
        Log.d(HistoryFragment.class.getName(), "onLoadFinished()");
        switch (loader.getId()) {
            case GROUP_CURSOR_LOADER:
                // Handle processing/translation of cursor data here
                // e.g. converting date string and currency into user format
                Log.d(HistoryFragment.class.getName(), "GROUP_CURSOR_LOADER");
                mAdapter.swapGroupCursor(data);
                if (mAdapter.getGroupCursor().getCount() > 0) {
                    elvDays.expandGroup(0);
                }
                break;
            default:
                // Handle processing/translation of cursor data here
                // e.g. converting date string and currency into user format
                Log.d(HistoryFragment.class.getName(), "child cursor");
                mAdapter.swapChildrenCursor(loader.getId(), data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case GROUP_CURSOR_LOADER:
                mAdapter.swapGroupCursor(null);
                break;
            default:
                mAdapter.swapChildrenCursor(loader.getId(), null);
                break;
        }
    }

    public class ExpandableListCursorAdapter extends BaseExpandableListAdapter {
        private LayoutInflater inflater;
        private Activity activity;
        private HistoryFragment historyFragment;
        private Cursor mGroupCursor;
        private SparseArray<Cursor> mCursorSparseArray = new SparseArray<Cursor>();
        private int mExpandedGroupLayout;
        private int mCollapsedGroupLayout;
        private String[] mGroupFromNames;
        private int[] mGroupFrom;
        private int[] mGroupTo;
        private int mChildLayout;
        private int mLastChildLayout;
        private String[] mChildFromNames;
        private int[] mChildFrom;
        private int[] mChildTo;
        private boolean mHasExpandedGroupLayout;
        private boolean mHasLastChildLayout;

        public ExpandableListCursorAdapter(Activity activity, HistoryFragment historyFragment, Cursor groupCursor, int expandedGroupLayout, int collapsedGroupLayout, String[] groupFromNames, int[] groupTo, int childLayout, int lastChildLayout, String[] childFromNames, int[] childTo) {
            this.historyFragment = historyFragment;
            this.mGroupCursor = groupCursor;
            this.mExpandedGroupLayout = expandedGroupLayout;
            this.mGroupFromNames = groupFromNames;
            this.mGroupFrom = findColumns(groupCursor, groupFromNames);
            this.mGroupTo = groupTo;
            this.mChildLayout = childLayout;
            this.mLastChildLayout = lastChildLayout;
            this.mChildFromNames = childFromNames;
            this.mChildTo = childTo;
            this.mCollapsedGroupLayout = collapsedGroupLayout;

            // Indicate if the additional layouts should be used
            mHasExpandedGroupLayout = true;
            mHasLastChildLayout = true;

            setActivity(activity);
        }

        public void setActivity(Activity activity) {
            this.activity = activity;
            this.inflater = this.activity.getLayoutInflater();
        }

        private int[] findColumns(Cursor cursor, String[] fromNames) {
            if (cursor != null) {
                int[] groupFrom = new int[fromNames.length];
                for (int i = 0; i < fromNames.length; i++) {
                    groupFrom[i] = cursor.getColumnIndexOrThrow(fromNames[i]);
                }
                return groupFrom;
            } else {
                return null;
            }

        }

        public Cursor getGroupCursor() {
            LoaderManager loaderManager = historyFragment.getLoaderManager();
            if (loaderManager.getLoader(GROUP_CURSOR_LOADER) == null) {
                // First time running will reach here
                loaderManager.initLoader(GROUP_CURSOR_LOADER, null, historyFragment);
                return null;
            } else if (loaderManager.getLoader(GROUP_CURSOR_LOADER) != null && mGroupCursor == null) {
                // If config changes happen or the cursor becomes null while the loader is still alive, we will reach here
                // The initLoader here will reach onLoadFinished immediately
                loaderManager.initLoader(GROUP_CURSOR_LOADER, null, historyFragment);
                return mGroupCursor;
            } else {
                // This is when mGroupCursor has already finished loading but we reach here on a notifyDataSetChanged
                return mGroupCursor;
            }
        }

        public void swapGroupCursor(Cursor groupCursor) {
            mGroupCursor = groupCursor;
            mGroupFrom = findColumns(groupCursor, mGroupFromNames);
            mAdapter.notifyDataSetChanged();
        }

        public void swapChildrenCursor(int key, Cursor childrenCursor) {
            mCursorSparseArray.put(key, childrenCursor);
            mChildFrom = findColumns(childrenCursor, mChildFromNames);
            mAdapter.notifyDataSetChanged();
        }

        public Cursor getChildrenCursor(int groupPosition) {
            // Given a day group, we return all the transactions within that day
            Cursor groupCursor = getGroup(groupPosition);

            // First, find out what the date of the current position in groupCursor is
            assert groupCursor != null;
            String dateString = groupCursor.getString(groupCursor.getColumnIndex(TransactionsTable.DATE));

            // Next, put the dateString into the args to pass to the CursorLoader
            Bundle args = new Bundle();
            args.putString(TransactionsTable.DATE, dateString);

            // Convert the date to an integer to use as a string
            int key = 0;
            try {
                key = Utilities.dateToInt(Utilities.parseDate(dateString, getResources().getString(R.string.database_date_format)));
                Log.d(HistoryFragment.class.getName(), "key = " + key);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Check if the childrenCursor has already previously been loaded. If yes, return that cursor, else start cursor loader and return null first
            Cursor childrenCursor = mCursorSparseArray.get(key);
            LoaderManager loaderManager = historyFragment.getLoaderManager();
            if (loaderManager.getLoader(key) == null) {
                loaderManager.initLoader(key, args, historyFragment);
                return null;
            } else if (loaderManager.getLoader(key) != null && childrenCursor == null) {
                loaderManager.initLoader(key, args, historyFragment);
                return mCursorSparseArray.get(key);
            } else {
                return childrenCursor;
            }
        }

        @Override
        public int getGroupCount() {
            Cursor groupCursor = getGroupCursor();
            if (groupCursor != null) {
                return groupCursor.getCount();
            }
            return 0;
        }

        @Override
        public int getChildrenCount(int key) {
            Cursor childrenCursor = getChildrenCursor(key);
            if (childrenCursor != null) {
                return childrenCursor.getCount();
            }
            return 0;
        }

        @Override
        public Cursor getGroup(int groupPosition) {
            Cursor cursor = getGroupCursor();
            cursor.moveToPosition(groupPosition);
            return cursor;
        }

        @Override
        public Cursor getChild(int groupPosition, int childPosition) {
            Cursor groupCursor = getGroup(groupPosition);
            assert groupCursor != null;
            String dateString = groupCursor.getString(groupCursor.getColumnIndex(TransactionsTable.DATE));
            int key = 0;
            try {
                key = Utilities.dateToInt(Utilities.parseDate(dateString, getResources().getString(R.string.database_date_format)));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            Cursor cursor = mCursorSparseArray.get(key);
            cursor.moveToPosition(childPosition);
            return cursor;
        }

        @Override
        public long getGroupId(int groupPosition) {
            Cursor groupCursor = getGroup(groupPosition);
            assert groupCursor != null;
            return groupCursor.getLong(groupCursor.getColumnIndexOrThrow("_id"));
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            Cursor childCursor = getChild(groupPosition, childPosition);
            assert childCursor != null;
            return childCursor.getLong(childCursor.getColumnIndexOrThrow("_id"));
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            // Don't reuse views as we want to change the layout as group expands and closes
            if (isExpanded && mHasExpandedGroupLayout) {
                convertView = inflater.inflate(mExpandedGroupLayout, null);
            } else {
                convertView = inflater.inflate(mCollapsedGroupLayout, null);
            }
            // Get the current group cursor with cursor set to groupPosition
            Cursor groupCursor = getGroup(groupPosition);

            // Alter the format of the data for display
            assert groupCursor != null;
            SparseArray<String> data = new SparseArray<String>(groupCursor.getColumnCount());
            int dateIndex = groupCursor.getColumnIndex(TransactionsTable.DATE);
            int totalIndex = groupCursor.getColumnIndex(EntriesProvider.DAY_TOTAL);

            String dateString = groupCursor.getString(dateIndex);
            double total = groupCursor.getDouble(totalIndex);

            String inputDateFormat = getResources().getString(R.string.database_date_format);
            String outputDateFormat = getResources().getString(R.string.overview_date_format);
            try {
                dateString = Utilities.formatDate(Utilities.parseDate(dateString, inputDateFormat), outputDateFormat);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String totalString = Utilities.formatCurrency(total);


            data.put(dateIndex, dateString);
            data.put(totalIndex, totalString);

            TextView textView;
            for (int i = 0; i < mGroupFrom.length; i++) {
                assert convertView != null;
                textView = (TextView) convertView.findViewById(mGroupTo[i]);
                textView.setText(data.get(mGroupFrom[i]));
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            // Don't reuse rows to avoid problems arising from dynamic data lastChild
            if (isLastChild && mHasLastChildLayout) {
                convertView = inflater.inflate(mLastChildLayout, null);
            } else {
                convertView = inflater.inflate(mChildLayout, null);
            }

            // Get the current child cursor for the groupPosition with cursor set to childPosition

            Cursor childCursor = getChild(groupPosition, childPosition);

            // Copy all data in childCursor into the sparseArray
            assert childCursor != null;
            SparseArray<String> data = new SparseArray<String>(childCursor.getColumnCount());

            for (int i = 0; i < childCursor.getColumnCount(); i++) {
                data.put(i, childCursor.getString(i));
            }

            // Alter the format of the data for display
            int amountIndex = childCursor.getColumnIndex(TransactionsTable.AMOUNT);

            double amount = childCursor.getDouble(amountIndex);

            String amountString = Utilities.formatCurrency(amount);

            data.put(amountIndex, amountString);

            TextView textView;
            for (int i = 0; i < mChildFrom.length; i++) {
                assert convertView != null;
                textView = (TextView) convertView.findViewById(mChildTo[i]);
                textView.setText(data.get(mChildFrom[i]));
            }

            // Apply color to amount
            TextView tvAmount = (TextView) convertView.findViewById(R.id.dayChild_tvAmount);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int key, int childPosition) {
            return true;
        }
    }
}
