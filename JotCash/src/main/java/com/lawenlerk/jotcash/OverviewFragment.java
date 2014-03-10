// TODO Update groupCursors after data has changed
// TODO expand first group automatically
package com.lawenlerk.jotcash;


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
    ExpandableListCursorAdapter mAdapter;


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

        mAdapter = new ExpandableListCursorAdapter(getActivity(), null, R.layout.day_expandedgroup, R.layout.day_collapsedgroup, groupFrom, groupTo, R.layout.day_child, R.layout.day_lastchild, childFrom, childTo);

        elvDays.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
/*        elvDays.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                Log.d(OverviewFragment.class.getSimpleName(), "getLoaderManager().destroyLoader(" + i + ")");
                getLoaderManager().destroyLoader(i);
            }
        });*/
        elvDays.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("OverviewFragment", Long.toString(id));
                Intent intent = new Intent(getActivity(), RecordActivity.class);
                Uri transactionUri = Uri.parse(EntriesProvider.TRANSACTIONS_URI + "/" + id);
                intent.putExtra(EntriesProvider.CONTENT_ITEM_TYPE, transactionUri);
                startActivity(intent);
                return false;
            }
        });

        // Query for days and sum of amounts
        getLoaderManager().initLoader(DAYS_LOADER, null, this);

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
                mAdapter.notifyDataSetChanged();
//                elvDays.expandGroup(0);
                break;
            default:
                mAdapter.setChildrenCursor(loader.getId(), data);
                mAdapter.notifyDataSetChanged();
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
                break;
        }
    }

    private void startLoader(int id, Bundle args) {
        getLoaderManager().initLoader(id, args, this);
    }

    public class ExpandableListCursorAdapter extends BaseExpandableListAdapter {
        public LayoutInflater inflater;
        public Activity activity;
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

        public ExpandableListCursorAdapter(Activity activity, Cursor groupCursor, int expandedGroupLayout, int collapsedGroupLayout, String[] groupFromNames, int[] groupTo, int childLayout, int lastChildLayout, String[] childFromNames, int[] childTo) {
            this.activity = activity;
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


            inflater = activity.getLayoutInflater();
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
            return mGroupCursor;
        }

        public void setGroupCursor(Cursor groupCursor) {
            mGroupCursor = groupCursor;
            mGroupFrom = findColumns(groupCursor, mGroupFromNames);
            mAdapter.notifyDataSetChanged();
        }

        public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {
            mCursorSparseArray.append(groupPosition, childrenCursor);
            mChildFrom = findColumns(childrenCursor, mChildFromNames);
            mAdapter.notifyDataSetChanged();
        }

        public Cursor getChildrenCursor(int groupPosition) {
            // Given a day group, we return all the transactions within that day
            Cursor groupCursor = getGroup(groupPosition);

            // First, find out what the date of the current position in groupCursor is
            String dateString = groupCursor.getString(groupCursor.getColumnIndex(TransactionsTable.DATE));

            // Next, put the dateString into the args to pass to the CursorLoader
            Bundle args = new Bundle();
            args.putString(TransactionsTable.DATE, dateString);


            // Check if the childrenCursor has already previously been loaded. If yes, return that cursor, else start cursor loader and return null first
            Cursor childrenCursor = mCursorSparseArray.get(groupPosition);
            if (childrenCursor != null) {
                return childrenCursor;
            } else {
                // Call the function to start the loader
                int id = groupCursor.getPosition();
                startLoader(id, args);
                return null;
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
        public int getChildrenCount(int groupPosition) {
            Cursor childrenCursor = getChildrenCursor(groupPosition);
            if (childrenCursor != null) {
                return childrenCursor.getCount();
            }
            return 0;
        }

        @Override
        public Cursor getGroup(int groupPosition) {
            Cursor cursor = mGroupCursor;
            cursor.moveToPosition(groupPosition);
            return cursor;
        }

        @Override
        public Cursor getChild(int groupPosition, int childPosition) {
            Cursor cursor = mCursorSparseArray.get(groupPosition);
            cursor.moveToPosition(childPosition);
            return cursor;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return mGroupCursor.getLong(mGroupCursor.getColumnIndexOrThrow("_id"));
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            Cursor cursor = getChild(groupPosition, childPosition);
            return cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
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
            TextView textView;
            for (int i = 0; i < mGroupFrom.length; i++) {
                textView = (TextView) convertView.findViewById(mGroupTo[i]);
                textView.setText(groupCursor.getString(mGroupFrom[i]));
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
            TextView textView;
            for (int i = 0; i < mChildFrom.length; i++) {
                textView = (TextView) convertView.findViewById(mChildTo[i]);
                textView.setText(childCursor.getString(mChildFrom[i]));
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    /*public class ExpandableListCursorAdapter extends SimpleCursorTreeAdapter {
        public boolean doNotDestroyCursor = false;

        public ExpandableListCursorAdapter(Context activity, OverviewFragment overviewFragment, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
            super(activity, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        }

        public ExpandableListCursorAdapter(Context activity, OverviewFragment overviewFragment, int collapsedGroupLayout, int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout, int lastChildLayout, String[] childFrom, int[] childTo, Cursor cursor) {
            super(activity, cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, lastChildLayout, childFrom, childTo);
        }

        public ExpandableListCursorAdapter(Context activity, OverviewFragment overviewFragment, int collapsedGroupLayout, int expandedGroupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo, Cursor cursor) {
            super(activity, cursor, collapsedGroupLayout, expandedGroupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        }

        public ExpandableListCursorAdapter(Context activity, OverviewFragment overviewFragment, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo, Cursor cursor) {
            super(activity, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        }

        @Override
        public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {
            super.setChildrenCursor(groupPosition, childrenCursor);
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
//            if (loader != null && !loader.isReset()) {
//                // Restart the loader
//                restartLoader(id, args);
//            } else {
                startLoader(id, args);
//            }
            return null;
        }
    }*/

}
