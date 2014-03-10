package com.lawenlerk.jotcash;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by enlerklaw on 3/7/14.
 */
public class CategoryCursorAdapter extends SimpleCursorAdapter {
    String[] mOriginalFrom;
    int[] mFrom;

    public CategoryCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        mOriginalFrom = from;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        super.bindView(view, context, cursor);
        final ViewBinder binder = super.getViewBinder();
        final int count = mTo.length;
        final int[] from = mFrom;
        final int[] to = mTo;

        for (int i = 0; i < count; i++) {
            final View v = view.findViewById(to[i]);
            if (v != null) {
                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, cursor, from[i]);
                }

                if (!bound) {
                    Log.d(CategoryCursorAdapter.class.getSimpleName(), "i=" + i);
                    Log.d(CategoryCursorAdapter.class.getSimpleName(), "from[i]=" + from[i]);
                    if (cursor == null)
                        Log.d(CategoryCursorAdapter.class.getSimpleName(), "Cursor is null");
                    String text = cursor.getString(from[i]);
                    if (text == null) {
                        text = "";
                    }

                    if (v instanceof TextView) {
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        setViewImage((ImageView) v, text);
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleCursorAdapter");
                    }
                }
            }
        }
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        findColumns(mOriginalFrom);
        Cursor res = super.swapCursor(c);
        return res;
    }

    /**
     * Create a map from an array of strings to an array of column-id integers in mCursor.
     * If mCursor is null, the array will be discarded.
     *
     * @param from the Strings naming the columns of interest
     */
    private void findColumns(String[] from) {
        if (mCursor != null) {
            int i;
            int count = from.length;
            if (mFrom == null || mFrom.length != count) {
                mFrom = new int[count];
            }
            for (i = 0; i < count; i++) {
                mFrom[i] = mCursor.getColumnIndexOrThrow(from[i]);
            }
        } else {
            mFrom = null;
        }
    }
}
