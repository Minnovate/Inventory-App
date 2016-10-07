package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryapp.data.InvContract.InvEntry; // to call InvEntry directly

/**
 * Created by gamelord on 10/4/16.
 */
// Database helper to manage database creation and version management
public class InvDbHelper extends SQLiteOpenHelper{
    public static final String LOG_TAG = InvDbHelper.class.getSimpleName();
    // Name of the database file
    private static final String DATABASE_NAME = "inventory.db";
    // Database version
    private static final int DATABASE_VERSION = 1;
    // Construct a new instance of InvDbHelper
    public InvDbHelper(Context context) {super(context,DATABASE_NAME,null,DATABASE_VERSION);}
    @Override
    public void onCreate(SQLiteDatabase db){
        // Create a String that contains the SQL statement to create the inventory table
        String SQL_CREATE_INV_TABLE = "CREATE TABLE" + InvEntry.TABLE_NAME+" ("
                + InvEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InvEntry.COLUMN_INV_NAME + " TEXT NOT NULL, "
                + InvEntry.COLUMN_INV_PRICE + " INTEGER NOT NULL DEFAULT 0,"
                + InvEntry.COLUMN_INV_QUANTITY + " INTEGER NOT NULL DEFAULT 0);";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INV_TABLE);
    }
    // this is called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
     // put this one to avoid error event there's nothing to be done here
    }
}
