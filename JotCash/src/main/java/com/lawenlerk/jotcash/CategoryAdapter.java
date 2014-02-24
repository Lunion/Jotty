package com.lawenlerk.jotcash;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fortysevendeg.swipelistview.SwipeListView;

import java.util.List;

/**
 * Created by enlerklaw on 2/24/14.
 */
public class CategoryAdapter extends BaseAdapter {
    private List<CategoryItem> data;
    private Context context;

    public CategoryAdapter(Context context, List<CategoryItem> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public CategoryItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CategoryItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = li.inflate(R.layout.category_row, parent, false);
            holder = new ViewHolder();
            holder.tvCategory = (TextView) convertView.findViewById(R.id.tvCategory);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ((SwipeListView)parent).recycle(convertView, position);

        holder.tvCategory.setText(item.getName());

        return convertView;

    }

    private class ViewHolder {
        TextView tvCategory;
    }
}
