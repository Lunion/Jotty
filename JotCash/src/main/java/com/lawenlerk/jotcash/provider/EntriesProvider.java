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

    private static final String AUTHORITY = "com.lawenlerk.jotcash.provider";

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, TransactionsTable.TABLE_NAME, TRANSACTIONS);
        sUriMatcher.addURI(AUTHORITY, TransactionsTable.TABLE_NAME + "/#", TRANSACTION_ID);
        sUriMatcher.addURI(AUTHORITY, "categories", CATEGORIES);
    }

    public static final Uri TRANSACTIONS_URI = Uri.parse("content://" + AUTHORITY + "/" + TransactionsTable.TABLE_NAME);
    public static final Uri CATEGORIES_URI = Uri.parse("content://" + AUTHORITY + "/" + "categories");  // For convenience to utilise groupby

    @Override
    public boolean onCreate() {
        entriesDatabaseHelper = new EntriesDatabaseHelper(
                getContext(),   // the application context
                DBNAME,         // the name of the database
                null,           // uses the default SQLite cursor
                2               // the version number
        );
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        SQLiteDatabase database = entriesDatabaseHelper.getReadableDatabase();

        String groupBy = null;
        switch (sUriMatcher.match(uri)) {
            case TRANSACTIONS:
                queryBuilder.setTables(TransactionsTable.TABLE_NAME);
                break;
            case TRANSACTION_ID:
                queryBuilder.setTables(TransactionsTable.TABLE_NAME);
                queryBuilder.appendWhere(TransactionsTable.CATEGORY + "=" + uri.getLastPathSegment());
                break;
            case CATEGORIES:
                queryBuilder.setTables(TransactionsTable.TABLE_NAME);
                groupBy = TransactionsTable.CATEGORY;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, groupBy, null, sortOrder);
        Log.d("EntriesProvider", Integer.toString(cursor.getCount()));
        Log.v("EntriesProvider", "Queried from the database");
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = entriesDatabaseHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case TRANSACTIONS:
                long transactionId = database.insert(TransactionsTable.TABLE_NAME, null, contentValues);
                Log.v("EntriesProvider", "Inserted transaction into database");

                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(TRANSACTIONS_URI + "/" + transactionId);

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;

        SQLiteDatabase database = entriesDatabaseHelper.getWritableDatabase();

        String id;

        switch (sUriMatcher.match(uri)) {
            case TRANSACTIONS:
                rowsDeleted = database.delete(TransactionsTable.TABLE_NAME, selection, selectionArgs);
                break;
            case TRANSACTION_ID:
                id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = database.delete(TransactionsTable.TABLE_NAME, TransactionsTable.ID + "=" + id, null);
                } else {
                    rowsDeleted = database.delete(TransactionsTable.TABLE_NAME, TransactionsTable.ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Log.v("EntriesProvider", "Deleted transaction from database");
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        // Do not use this in the UI to update an existing transaction, create a new transaction and delete the old one instead.
        // This is only for internal use

        int rowsUpdated = 0;
        String id;

        SQLiteDatabase database = entriesDatabaseHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case TRANSACTIONS:
                rowsUpdated = database.update(TransactionsTable.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case TRANSACTION_ID:
                id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = database.update(TransactionsTable.TABLE_NAME, contentValues, TransactionsTable.CATEGORY + "=" + id, null);
                } else {
                    rowsUpdated = database.update(TransactionsTable.TABLE_NAME, contentValues, TransactionsTable.CATEGORY + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Log.v("EntriesProvider", "Updated database");
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
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
    public String getTransactionType(Uri uri) {
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
