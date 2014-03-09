package com.lawenlerk.jotcash;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;

/**
 * Created by EnLerk on 3/9/14.
 */
public class DayCursorTreeAdapter extends CursorTreeAdapter {
    public DayCursorTreeAdapter(Cursor cursor, Context context) {
        super(cursor, context);
    }

    public DayCursorTreeAdapter(Cursor cursor, Context context, boolean autoRequery) {
        super(cursor, context, autoRequery);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor cursor) {
        return null;
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean b, ViewGroup viewGroup) {
        return null;
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean b) {

    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean b, ViewGroup viewGroup) {
        return null;
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean b) {

    }
}
