package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gamelord on 10/4/16.
 */
//API Contract of the Inventory App
public final class InvContract {
    // Create an empty constructor to prevent accidentially contract class instantiating
    private InvContract() {
    }

    // Utilize the "Content authority" part from Pets App
    public static final String CONTENT_AUTHORITY = "com.example.android.InventoryApp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INV = "InventoryApp";

    // Create a inner class that defines constant values for Inventory App database table
    public static final class InvEntry implements BaseColumns {
        // content URI to access the inventory data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INV);
        // the MIME type of the CONTENT_URI for a list of inventory
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INV;
        // the MIME type of the CONTENT_URI for A single inventory item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INV;

        // Name of the database table for inventory items
        public final static String TABLE_NAME = "InventoryApp";
        // Create the format of the database
        // Create UNIQUE ID number for inventory items
        public final static String _ID = BaseColumns._ID;
        // Create Name, Quantity and Price
        public final static String COLUMN_INV_NAME = "name";
        public final static String COLUMN_INV_QUANTITY = "quantity";
        public final static String COLUMN_INV_PRICE = "price";
        public final static String COLUMN_INV_SOLD = "sold";
        public final static String COLUMN_INV_IMAGE = "imagePath";
    }
}
