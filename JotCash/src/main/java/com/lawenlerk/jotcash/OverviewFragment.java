package com.lawenlerk.jotcash;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import com.lawenlerk.jotcash.database.TransactionsTable;

/**
 * Created by enlerklaw on 2/24/14.
 */
public class OverviewFragment extends Fragment {
    View view;
    ListView lvTransactions;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.overview_fragment, container, false);
        ProgressBar progressBar = new ProgressBar(getActivity());
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        lvTransactions = (ListView) view.findViewById(R.id.lvTransactions);
        lvTransactions.setEmptyView(progressBar);

/*        ViewGroup root = (ViewGroup) view.findViewById(android.R.id.content);
        root.addView(progressBar);*/

        String[] fromColumns = {TransactionsTable.AMOUNT};
        int[] toViews = {android.R.id.text1};

        android.support.v4.widget.SimpleCursorAdapter mAdapter = new android.support.v4.widget.SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
        lvTransactions.setAdapter(mAdapter);


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
