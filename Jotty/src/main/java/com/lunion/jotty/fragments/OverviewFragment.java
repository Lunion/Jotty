package com.lunion.jotty.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.lunion.jotty.R;
import com.lunion.jotty.Transaction;
import com.lunion.jotty.Utilities;
import com.lunion.jotty.database.TransactionsTable;
import com.lunion.jotty.provider.EntriesProvider;

import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OverviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match.
    // This can possibly be used for different layouts
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int WEEK_LENGTH = 7;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LineGraphView lineGraphView;
    private GraphViewSeries graphViewSeries;
    private GraphView.GraphViewData[] graphViewDatas;

    private OnFragmentInteractionListener mListener;

    public OverviewFragment() {
        // Required empty public constructor
    }

    public static OverviewFragment newInstance() {
        OverviewFragment fragment = new OverviewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        assert view != null;

        lineGraphView = new LineGraphView(getActivity(), "Past Month");
        lineGraphView.setDrawDataPoints(true);
        lineGraphView.setDataPointsRadius(15f);

        // Initialise the graph view with 7 (1 week) data points of y=0
        graphViewDatas = new GraphView.GraphViewData[WEEK_LENGTH];
        for (int i = 0; i < WEEK_LENGTH; i++) {
            graphViewDatas[i] = new GraphView.GraphViewData(-WEEK_LENGTH + i + 1, 0);
        }
        graphViewSeries = new GraphViewSeries(graphViewDatas);
        lineGraphView.addSeries(graphViewSeries);
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(0, null, this);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.gvDaily);
        linearLayout.addView(lineGraphView);

        // Inflate the layout for this fragment
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection;
        String selection;
        String[] selectionArgs;
        String sortOrder;

        projection = new String[]{
                TransactionsTable.DATE + " AS _id",
                TransactionsTable.DATE,
                "SUM(" + TransactionsTable.AMOUNT + ") AS " + EntriesProvider.DAY_TOTAL
        };
        selection = TransactionsTable.TYPE + "=?";
        selectionArgs = new String[]{
                Transaction.EXPENSE
        };
        sortOrder = TransactionsTable.DATE + " DESC";

        return new CursorLoader(getActivity(), EntriesProvider.DAYS_URI, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        populateGraphView(cursor);
    }

    private void populateGraphView(Cursor cursor) {
        // We have 2 "cursors", one is along the x-axis of the graph (right to left),
        // the other is our actual cursor pointing at data in descending order of date.
        // We will iterate through the x-axis from right to left, if the corresponding date is
        // equivalent or less than the data that our data cursor points to, we will move the
        // data cursor by 1 step.

        if (cursor != null) {
            // Find out the date today
            Date date = new Date();

            for (int i=0; i < WEEK_LENGTH; i++) {



                // Check and see if today's date is the same
            }

/*            while (cursor.moveToNext() && cursor.getPosition() < WEEK_LENGTH) {
                int i = cursor.getPosition();
                graphViewDatas[WEEK_LENGTH - 1 - i] = new GraphView.GraphViewData(-i, cursor.getDouble(cursor.getColumnIndex(EntriesProvider.DAY_TOTAL)));
            }*/
        }
        graphViewSeries.resetData(graphViewDatas);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
}
