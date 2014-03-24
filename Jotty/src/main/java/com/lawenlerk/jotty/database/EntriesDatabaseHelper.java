package com.lawenlerk.jotty.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by enlerklaw on 2/25/14.
 */
public class EntriesDatabaseHelper extends SQLiteOpenHelper {

    public EntriesDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        TransactionsTable.onCreate(database);
        CustomCategoriesTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        TransactionsTable.onUpgrade(database, oldVersion, newVersion);
        CustomCategoriesTable.onUpgrade(database, oldVersion, newVersion);
    }
}
