package com.lawenlerk.jotcash.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.lawenlerk.jotcash.database.CategoriesTable;
import com.lawenlerk.jotcash.database.EntriesDatabaseHelper;
import com.lawenlerk.jotcash.database.TransactionsTable;

/**
 * Created by enlerklaw on 2/25/14.
 */
public class EntriesProvider extends ContentProvider {
    // database
    private EntriesDatabaseHelper entriesDatabaseHelper;

    private static final String DBNAME = "entries";

    // Integer codes/constants for UriMatcher
    private static final int TRANSACTIONS = 1;
    private static final int TRANSACTION_ID = 2;
    private static final int CATEGORIES = 3;
    private static final int CATEGORY_ID = 4;

    private static final String AUTHORITY = "com.lawenlerk.jotcash.provider";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, TransactionsTable.TABLE_NAME, TRANSACTIONS);
        sUriMatcher.addURI(AUTHORITY, TransactionsTable.TABLE_NAME + "/#", TRANSACTION_ID);
        sUriMatcher.addURI(AUTHORITY, CategoriesTable.TABLE_NAME, CATEGORIES);
        sUriMatcher.addURI(AUTHORITY, CategoriesTable.TABLE_NAME + "/#", CATEGORY_ID);
    }

/*    // used for the UriMatcher
    private static final int TRANSACTIONS = 10;
    private static final int TRANSACTION_ID = 20;

    private static final String AUTHORITY = "com.lawenlerk.jotcash.provider";

    private static final String BASE_PATH = "transactions";
    public static final Uri CONTENT_URI = Uri.parse("content://" +AUTHORITY + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/transactions";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/transaction";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, TRANSACTIONS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", TRANSACTION_ID);
    }*/

    @Override
    public boolean onCreate() {
        entriesDatabaseHelper = new EntriesDatabaseHelper(
                getContext(),   // the application context
                DBNAME,         // the name of the database
                null,           // uses the default SQLite cursor
                1               // the version number
        );
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case TRANSACTIONS:
                queryBuilder.setTables(TransactionsTable.TABLE_NAME + " JOIN " +CategoriesTable.TABLE_NAME + " ON ("+TransactionsTable.TABLE_NAME +"." + TransactionsTable.CATEGORY_ID + " = "+CategoriesTable.TABLE_NAME +"." + CategoriesTable.CATEGORY_ID + ")");
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "_ID ASC";
                }
                break;
            case TRANSACTION_ID:
                queryBuilder.setTables(TransactionsTable.TABLE_NAME + " JOIN " +CategoriesTable.TABLE_NAME + " ON ("+TransactionsTable.TABLE_NAME +"." + TransactionsTable.CATEGORY_ID + " = "+CategoriesTable.TABLE_NAME +"." + CategoriesTable.CATEGORY_ID + ")");
                selection = selection + "_ID = " + uri.getLastPathSegment();
                break;
            case CATEGORIES:
                queryBuilder.setTables(CategoriesTable.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = "_ID ASC";
                }
                break;
            case CATEGORY_ID:
                queryBuilder.setTables(CategoriesTable.TABLE_NAME);
                selection = selection + "_ID = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase database = entriesDatabaseHelper.getWritableDatabase();
        if (database==null) {
            throw new IllegalArgumentException("Database is null");
        }
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }


    /*    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
       *//* SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(TransactionsTable.TABLE_NAME);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case TRANSACTIONS:
                break;
            case TRANSACTION_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(TransactionsTable.ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(sqlDB, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;*//*
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
*//*        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        long id = 0;
        switch (uriType) {
            case TRANSACTIONS:
                id = sqlDB.insert(TransactionsTable.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);*//*
        database = entriesDatabaseHelper.getWritableDatabase();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
*//*        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case TRANSACTIONS:
                rowsDeleted = sqlDB.delete(TransactionsTable.TABLE_NAME, selection, selectionArgs);
                break;
            case TRANSACTION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(TransactionsTable.TABLE_NAME, TransactionsTable.ID + "=" + id, null);

                } else {
                    rowsDeleted = sqlDB.delete(TransactionsTable.TABLE_NAME, TransactionsTable.ID + "=" +id + " AND " +selection, selectionArgs);

                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;*//*
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
*//*        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case TRANSACTIONS:
                rowsUpdated = sqlDB.update(TransactionsTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRANSACTION_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(TransactionsTable.TABLE_NAME, values, TransactionsTable.ID + "=" + id, null);

                } else {
                    rowsUpdated = sqlDB.update(TransactionsTable.TABLE_NAME, values, TransactionsTable.ID + "=" + id +" and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " +uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;*//*
    }*/

}
