package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryapp.data.InvContract.InvEntry;

/**
 * Created by gamelord on 10/4/16.
 **/

public class InvProvider extends ContentProvider {
    // Tag for the log messages
    public static final String LOG_TAG = InvProvider.class.getSimpleName();
    // URI matcher code for the content URI of the inventory table
    private static final int INVS = 100;
    // URI matcher code for the content URI for a single inventory item in the database
    private static final int INV_ID = 101;
    /* Utilize the codes from Pets app for the UriMatcher*/
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // match for all items
        sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_INV, INVS);
        // match for single item
        sUriMatcher.addURI(InvContract.CONTENT_AUTHORITY, InvContract.PATH_INV + "/#", INV_ID);
    }

    // Create a database helper object
    private InvDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InvDbHelper(getContext());
        return true;
    }

    // Create a cursor query
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This corsor will hold the result of the query
        Cursor cursor;
        //Figure out if the URI macher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                cursor = database.query(InvEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INV_ID:
                selection = InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InvEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //set notification URI on the Cursor to know what conent URI the Cursor was created for
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // return the cursor
        return cursor;
    }

    // Create a method to insert new item
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                return insertInv(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertInv(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(InvEntry.COLUMN_INV_NAME);
        if (name == null) {
            throw new IllegalArgumentException("An item requires a name!");
        }
        // Check quantity and price is greater or equal to 0
        Integer price = values.getAsInteger(InvEntry.COLUMN_INV_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Please enter a valid price");
        }
        Integer quantity = values.getAsInteger(InvEntry.COLUMN_INV_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Please enter a valid quantity");
        }
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new item with the given values
        long id = database.insert(InvEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    // Update method
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                return updateInv(uri, contentValues, selection, selectionArgs);
            case INV_ID:
                selection = InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInv(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInv(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InvEntry.COLUMN_INV_NAME)) {
            String name = values.getAsString(InvEntry.COLUMN_INV_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Inventory item requires a name");
            }
        }
        if (values.containsKey(InvEntry.COLUMN_INV_QUANTITY)) {
            Integer quantity = values.getAsInteger(InvEntry.COLUMN_INV_QUANTITY);
            if (quantity == null && quantity < 0) {
                throw new IllegalArgumentException("Inventory item requires a valid quanity");
            }
        }
        if (values.containsKey(InvEntry.COLUMN_INV_PRICE)) {
            Integer price = values.getAsInteger(InvEntry.COLUMN_INV_PRICE);
            if (price == null && price < 0) {
                throw new IllegalArgumentException("Inventory item requires a valid price");
            }
        }
        // if there are no values to update , then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // otherwises, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InvEntry.TABLE_NAME, values, selection, selectionArgs);
        // if 1 or more rows were updated
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    // delete method
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INV_ID:
                // Delete a single row given by the ID in the URI
                selection = InvEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InvEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVS:
                return InvEntry.CONTENT_LIST_TYPE;
            case INV_ID:
                return InvEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
