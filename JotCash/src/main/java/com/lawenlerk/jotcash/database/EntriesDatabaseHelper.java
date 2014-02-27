package com.lawenlerk.jotcash.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lawenlerk.jotcash.database.TransactionsTable;

/**
 * Created by enlerklaw on 2/25/14.
 */
public class EntriesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "records.db";
    private static final int DATABASE_VERSION = 1;

    public EntriesDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        TransactionsTable.onCreate(database);
        CategoriesTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        TransactionsTable.onUpgrade(database, oldVersion, newVersion);
        CategoriesTable.onUpgrade(database, oldVersion, newVersion);
    }
}
