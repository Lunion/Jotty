package com.lawenlerk.jotcash;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by enlerklaw on 2/24/14.
 */
public class TransactionsOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "records";
    private static final int DATABASE_VERSION = 1;
    private static final String TRANSACTIONS_TABLE_NAME = "transactions";

    private static final String COL_TRANSACTION_ID = "id";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_TRANSACTION_TYPE = "transaction_type";
    private static final String COL_CATEGORY = "category";


    private static final String TRANSACTIONS_TABLE_CREATE =
            "CREATE TABLE " + TRANSACTIONS_TABLE_NAME + " (" +
                    COL_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
                    COL_AMOUNT + " REAL NOT NULL, " +
                    COL_TRANSACTION_TYPE + " TEXT NOT NULL, " +
                    COL_CATEGORY + " TEXT NOT NULL" +
                    ");";

    TransactionsOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TRANSACTIONS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(TransactionsOpenHelper.class.getName(), "Upgrading database from version " +
                oldVersion + " to " + newVersion +
                ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TRANSACTIONS_TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
