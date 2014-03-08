package com.lawenlerk.jotcash.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lawenlerk.jotcash.Transaction;

/**
 * Created by EnLerk on 3/8/14.
 */
public class CustomCategoriesTable {
    public static final String TABLE_NAME = "custom_categories";

    public static final String ITEM_NAME = "custom_category";

    public static final String ID = "_id";
    public static final String TYPE = "type";
    public static final String CATEGORY = "category";

    // SQL statement to create transactions table
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " +
            TABLE_NAME + " (" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TYPE + " TEXT NOT NULL, " +
            CATEGORY + " TEXT NOT NULL);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_TABLE);
        Log.w(CustomCategoriesTable.class.getName(), SQL_CREATE_TABLE);

        String[] customExpenseCategories = new String[]{
                "Food and Drinks",
                "Rent",
                "Entertainment",
                "Travel",
                "Medical",
                "Education",
                "Transport",
                "Friends & Lover",
                "Family",
                "Shopping",
                "Loan",
                "Investment",
                "Other"
        };

        String[] customIncomeCategories = new String[]{
                "Salary",
                "Award",
                "Other"
        };

        for (String customExpenseCategory : customExpenseCategories) {
            ContentValues values = new ContentValues();
            values.put(CustomCategoriesTable.CATEGORY, customExpenseCategory);
            values.put(CustomCategoriesTable.TYPE, Transaction.EXPENSE);
            database.insert(CustomCategoriesTable.TABLE_NAME, null, values);
        }

        for (String customIncomeCategory : customIncomeCategories) {
            ContentValues values = new ContentValues();
            values.put(CustomCategoriesTable.CATEGORY, customIncomeCategory);
            values.put(CustomCategoriesTable.TYPE, Transaction.INCOME);
            database.insert(CustomCategoriesTable.TABLE_NAME, null, values);
        }


    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(TransactionsTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    public static class CustomCategory {
        final String category;
        final String type;

        public CustomCategory(String category, String type) {
            this.category = category;
            this.type = type;
        }
    }
}
