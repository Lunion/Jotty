package com.lawenlerk.jotcash.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
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

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + TransactionsTable.TABLE_NAME;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + TransactionsTable.ITEM_NAME;

    @Override
    public boolean onCreate() {
        entriesDatabaseHelper = new EntriesDatabaseHelper(
                getContext(),   // the application context
                DBNAME,         // the name of the database
                null,           // uses the default SQLite cursor
                3               // the version number
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
                queryBuilder.appendWhere(TransactionsTable.ID + "=" + uri.getLastPathSegment());
                break;
            case CATEGORIES:
                queryBuilder.setTables(TransactionsTable.TABLE_NAME);
                groupBy = TransactionsTable.CATEGORY + ", " + TransactionsTable.TYPE;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Log.v(EntriesProvider.class.getName(), "Starting query");
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, groupBy, null, sortOrder);
        Log.v(EntriesProvider.class.getName(), "Queried from the database");
        Log.d("EntriesProvider", Integer.toString(cursor.getCount()));
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
                    rowsUpdated = database.update(TransactionsTable.TABLE_NAME, contentValues, TransactionsTable.ID + "=" + id, null);
                } else {
                    rowsUpdated = database.update(TransactionsTable.TABLE_NAME, contentValues, TransactionsTable.ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Log.v("EntriesProvider", "Updated database");
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}

