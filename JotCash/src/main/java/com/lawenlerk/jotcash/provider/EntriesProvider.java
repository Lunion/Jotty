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

    public static final Uri TRANSACTIONS_URI = Uri.parse("content://" + AUTHORITY + "/" + TransactionsTable.TABLE_NAME);
    public static final Uri CATEGORIES_URI = Uri.parse("content://" + AUTHORITY + "/" + CategoriesTable.TABLE_NAME);

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
        SQLiteDatabase database = entriesDatabaseHelper.getReadableDatabase();

        switch (sUriMatcher.match(uri)) {
            case TRANSACTIONS:
                queryBuilder.setTables(TransactionsTable.TABLE_NAME + " JOIN " + CategoriesTable.TABLE_NAME + " ON (" + TransactionsTable.TABLE_NAME + "." + TransactionsTable.CATEGORY_ID + " = " + CategoriesTable.TABLE_NAME + "." + CategoriesTable.ID + ")");
                break;
            case TRANSACTION_ID:
                queryBuilder.setTables(TransactionsTable.TABLE_NAME + " JOIN " + CategoriesTable.TABLE_NAME + " ON (" + TransactionsTable.TABLE_NAME + "." + TransactionsTable.CATEGORY_ID + " = " + CategoriesTable.TABLE_NAME + "." + CategoriesTable.ID + ")");
                selection = selection + " " + TransactionsTable.ID + " = " + uri.getLastPathSegment();
                break;
            case CATEGORIES:
                queryBuilder.setTables(CategoriesTable.TABLE_NAME);
                break;
            case CATEGORY_ID:
                queryBuilder.setTables(CategoriesTable.TABLE_NAME);
                selection = selection + " " + CategoriesTable.ID + " = " + uri.getLastPathSegment();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
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
                // Check if category already exists in categories table
                String[] projection = {
                        CategoriesTable.ID,
                        CategoriesTable.CATEGORY,
                        CategoriesTable.COUNT
                };
                String selection = CategoriesTable.CATEGORY + " = ?";
                String[] selectionArgs = {contentValues.getAsString(CategoriesTable.CATEGORY)};
                Cursor cursor = query(CATEGORIES_URI, projection, selection, selectionArgs, null);

                // Prepare ContentValues for insertion or update in categories table later
                ContentValues categoryContentValues = new ContentValues();
                categoryContentValues.put(CategoriesTable.CATEGORY, contentValues.getAsString(CategoriesTable.CATEGORY));
                categoryContentValues.put(CategoriesTable.LAST_USED, contentValues.getAsString(TransactionsTable.TIME_CREATED));

                String categoryId;

                if (cursor.getCount() < 1) {
                    // Category does not exist
                    // Initialise count to 1
                    Log.d("EntriesProvider", "category does not exist");
                    categoryContentValues.put(CategoriesTable.COUNT, 1);

                    // Actually do the insert
                    Uri categoryUri = insert(CATEGORIES_URI, categoryContentValues);
                    categoryId = categoryUri.getLastPathSegment();

                } else {
                    // Category already exists
                    // Use existing cursor to update category frequency and last used time
                    Log.d("EntriesProvider", "category already exists");
                    cursor.moveToFirst();
                    int categoryIdIndex = cursor.getColumnIndex(CategoriesTable.ID);
                    int categoryCountIndex = cursor.getColumnIndex(CategoriesTable.COUNT);
                    categoryId = cursor.getString(categoryIdIndex);
                    int categoryCount = cursor.getInt(categoryCountIndex);

                    // Increment count
                    categoryContentValues.put(CategoriesTable.COUNT, categoryCount + 1);
                    Uri categoryUri = Uri.parse(CATEGORIES_URI + "/" + categoryId);

                    // Actually do the update
                    update(categoryUri, categoryContentValues, null, null);

                }

                // Append into original set of contentValues with returned categoryID as well and insert into database
                // Also, remove the category field in contentValues as this exists in categories table
                contentValues.remove(CategoriesTable.CATEGORY);
                contentValues.put(TransactionsTable.CATEGORY_ID, categoryId);
                long transactionId = database.insert(TransactionsTable.TABLE_NAME, null, contentValues);
                Log.v("EntriesProvider", "Inserted transaction into database");

                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(TRANSACTIONS_URI + "/" + transactionId);

            case CATEGORIES:
                // Check if ContentValues have right amount of values

                // Insert into categories table
                long categoryIdLong = database.insert(CategoriesTable.TABLE_NAME, null, contentValues);

                Log.v("EntriesProvider", "Inserted category into database");
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(CATEGORIES_URI + "/" + categoryIdLong);
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
                deleteUnusedCategories();
                break;
            case TRANSACTION_ID:
                id = uri.getLastPathSegment();

                // Take not of the category to decrement
                String[] transactionProjection = {
                        TransactionsTable.CATEGORY_ID,
                        CategoriesTable.COUNT
                };
                Cursor transactionCursor = query(Uri.parse(TRANSACTIONS_URI + "/" + id), transactionProjection, null, null, null);
                transactionCursor.moveToFirst();
                int categoryIdIndex = transactionCursor.getColumnIndex(TransactionsTable.CATEGORY_ID);
                String categoryId = transactionCursor.getString(categoryIdIndex);

                // Take not of the current count for the category. This can be obtained here because of the joined query
                int countIndex = transactionCursor.getColumnIndex(CategoriesTable.COUNT);
                int count = transactionCursor.getInt(countIndex);

                // Update category by id
                ContentValues categoryCV = new ContentValues();
                categoryCV.put(CategoriesTable.COUNT, count-1);
                update(Uri.parse(CATEGORIES_URI + "/" + categoryId), categoryCV, null, null);

                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = database.delete(TransactionsTable.TABLE_NAME, TransactionsTable.ID + "=" + id, null);
                } else {
                    rowsDeleted = database.delete(TransactionsTable.TABLE_NAME, TransactionsTable.ID + "=" + id + " AND " + selection, selectionArgs);
                }
                deleteUnusedCategories();
                break;
            case CATEGORIES:
                rowsDeleted = database.delete(CategoriesTable.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = database.delete(CategoriesTable.TABLE_NAME, CategoriesTable.ID + "=" + id, null);
                } else {
                    rowsDeleted = database.delete(CategoriesTable.TABLE_NAME, CategoriesTable.ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        Log.v("EntriesProvider", "Deleted transaction/category from database");
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    private int deleteUnusedCategories() {
        int rowsDeleted = 0;

        rowsDeleted = delete(CATEGORIES_URI, CategoriesTable.COUNT + "=" + Integer.toString(0), null);
        Log.v("EntriesProvider", "Deleted unused categories from database");
        getContext().getContentResolver().notifyChange(CATEGORIES_URI, null);

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
                    rowsUpdated = database.update(TransactionsTable.TABLE_NAME, contentValues, TransactionsTable.CATEGORY_ID + "=" + id, null);
                } else {
                    rowsUpdated = database.update(TransactionsTable.TABLE_NAME, contentValues, TransactionsTable.CATEGORY_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            case CATEGORIES:
                rowsUpdated = database.update(CategoriesTable.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case CATEGORY_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = database.update(CategoriesTable.TABLE_NAME, contentValues, CategoriesTable.ID + "=" + id, null);
                } else {
                    rowsUpdated = database.update(CategoriesTable.TABLE_NAME, contentValues, CategoriesTable.ID + "=" + id + " AND " + selection, selectionArgs);
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
