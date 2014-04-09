package com.lawenlerk.jotty.holographlibrary;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by enlerklaw on 09/04/2014 in project Jotty.
 */
public class LineGraphView extends LineGraph {
    public LineGraphView(Context context) {
        super(context);
    }

    public LineGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(LineGraphCursorAdapter adapter) {

    }
}
