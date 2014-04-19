package com.lunion.jotty.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by enlerklaw on 2/24/14.
 */
public class TransactionsTable {
    public static final String TABLE_NAME = "transactions";

    public static final String ITEM_NAME = "transaction";

    public static final String ID = "_id";
    public static final String TIME_CREATED = "time_created";
    public static final String AMOUNT = "amount";
    public static final String TYPE = "type";
    public static final String DATE = "date";
    public static final String DESCRIPTION = "description";
    public static final String CATEGORY = "category";

    // SQL statement to create transactions table
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " +
            TABLE_NAME + " (" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TIME_CREATED + " TEXT NOT NULL, " +
            AMOUNT + " REAL NOT NULL, " +
            TYPE + " TEXT NOT NULL, " +
            DATE + " TEXT NOT NULL, " +
            DESCRIPTION + " TEXT, " +
            CATEGORY + " TEXT NOT NULL);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_TABLE);
        Log.w(TransactionsTable.class.getName(), SQL_CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(TransactionsTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

}
