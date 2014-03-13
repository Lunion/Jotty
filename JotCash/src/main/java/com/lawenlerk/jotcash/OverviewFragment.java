// TODO Handle configuration changes. Currently, switching orientation Portrait -> Landscape -> Portrait results in crash. Possible disconnected activity. Debug to find out more
package com.lawenlerk.jotcash;


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


public class OverviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GROUP_CURSOR_LOADER = -99; // Arbitrarily picked integer to avoid clash with group positions
    View view;
    ExpandableListView elvDays;
    ExpandableListCursorAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.overview_fragment, container, false);

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

        mAdapter = new ExpandableListCursorAdapter(this, null, R.layout.day_expandedgroup, R.layout.day_collapsedgroup, groupFrom, groupTo, R.layout.day_child, R.layout.day_lastchild, childFrom, childTo);

        elvDays.setAdapter(mAdapter);
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
            case GROUP_CURSOR_LOADER:
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
            case GROUP_CURSOR_LOADER:
                // Handle processing/translation of cursor data here
                // e.g. converting date string and currency into user format
                mAdapter.setGroupCursor(data);
                mAdapter.requeryChildrenCursor();
                elvDays.expandGroup(0);
                break;
            default:
                // Handle processing/translation of cursor data here
                // e.g. converting date string and currency into user format
                mAdapter.setChildrenCursor(loader.getId(), data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case GROUP_CURSOR_LOADER:
                mAdapter.setGroupCursor(null);
                break;
            default:
                mAdapter.setChildrenCursor(loader.getId(), null);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(OverviewFragment.class.getSimpleName(), "mAdapter.requery()");
        mAdapter.requery();
        // Reconnect the fragment
        mAdapter.setOverviewFragment(this);
    }

    public class ExpandableListCursorAdapter extends BaseExpandableListAdapter {
        private LayoutInflater inflater;
        private OverviewFragment overviewFragment;
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

        public ExpandableListCursorAdapter(OverviewFragment overviewFragment, Cursor groupCursor, int expandedGroupLayout, int collapsedGroupLayout, String[] groupFromNames, int[] groupTo, int childLayout, int lastChildLayout, String[] childFromNames, int[] childTo) {
            this.overviewFragment = overviewFragment;
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

            setOverviewFragment(overviewFragment);
        }

        public void setOverviewFragment(OverviewFragment overviewFragment) {
            this.overviewFragment = overviewFragment;
            this.inflater = overviewFragment.getActivity().getLayoutInflater();
        }

        public void requery() {
            // Obtain all cursors again
            overviewFragment.getLoaderManager().destroyLoader(GROUP_CURSOR_LOADER);
            overviewFragment.getLoaderManager().initLoader(GROUP_CURSOR_LOADER, null, overviewFragment);

        }

        private void requeryChildrenCursor() {
            // Loop through all the children cursors then destroy and init the loaders
            for (int i = 0; i < mCursorSparseArray.size(); i++) {
                int childLoaderId = mCursorSparseArray.keyAt(i);
                Cursor childCursor = mCursorSparseArray.get(childLoaderId); // childLoaderId = key = groupPosition
                childCursor.moveToFirst();
                // Find out which group we are dealing with
                Cursor groupCursor = getGroup(childLoaderId);
                String dateString = groupCursor.getString(groupCursor.getColumnIndexOrThrow(TransactionsTable.DATE));
                Bundle args = new Bundle();
                args.putString(TransactionsTable.DATE, dateString);
                overviewFragment.getLoaderManager().destroyLoader(childLoaderId);
                overviewFragment.getLoaderManager().initLoader(childLoaderId, args, overviewFragment);
            }
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
            assert groupCursor != null;
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
                getLoaderManager().initLoader(id, args, this.overviewFragment);
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
            Cursor cursor = getGroupCursor();
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
            TextView textView;
            for (int i = 0; i < mGroupFrom.length; i++) {
                assert convertView != null;
                textView = (TextView) convertView.findViewById(mGroupTo[i]);
                assert groupCursor != null;
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
                assert convertView != null;
                textView = (TextView) convertView.findViewById(mChildTo[i]);
                assert childCursor != null;
                textView.setText(childCursor.getString(mChildFrom[i]));
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

}
