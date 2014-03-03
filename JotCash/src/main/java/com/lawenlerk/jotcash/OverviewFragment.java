package com.lawenlerk.jotcash;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lawenlerk.jotcash.database.TransactionsTable;
import com.lawenlerk.jotcash.provider.EntriesProvider;

/**
 * Created by enlerklaw on 2/24/14.
 */
public class OverviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    View view;
    ListView lvTransactions;
    SimpleCursorAdapter mAdapter;

    private static final String[] PROJECTION = new String[]{
            TransactionsTable.ID,
            TransactionsTable.AMOUNT
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.overview_fragment, container, false);

        lvTransactions = (ListView) view.findViewById(R.id.lvTransactions);

        String[] fromColumns = {TransactionsTable.AMOUNT};
        int[] toViews = {android.R.id.text1};

        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
        lvTransactions.setAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), EntriesProvider.TRANSACTIONS_URI, PROJECTION, null, null, null);
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
