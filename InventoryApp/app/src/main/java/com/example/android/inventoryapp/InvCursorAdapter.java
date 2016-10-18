package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InvContract.InvEntry;

/**
 * Created by gamelord on 10/4/16.
 */

public class InvCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = InvCursorAdapter.class.getSimpleName();
    private String quantityString;
    private String soldString;


    public InvCursorAdapter(Context context, Cursor c) { super(context, c,0);}
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }
    // build a method to bind inventory data to a list
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView soldTextView = (TextView) view.findViewById(R.id.sold);

        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_NAME);
        final int quantityColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_PRICE);
        final int soldColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_SOLD);
        final int idColumnIndex = cursor.getColumnIndex(InvEntry._ID);
        final int mRowId = cursor.getInt(idColumnIndex);

        // Read the item attributes from the Cursor for the current item
        String invName = cursor.getString(nameColumnIndex);
        String invPrice = cursor.getString(priceColumnIndex);
        String invQuantity = cursor.getString(quantityColumnIndex);
        String invSold = cursor.getString(soldColumnIndex);

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(invName);
        quantityTextView.setText(invQuantity);
        priceTextView.setText(invPrice);
        soldTextView.setText(invSold);

        Button soldButton = (Button) view.findViewById(R.id.sellone);
        soldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sold = Integer.parseInt(soldTextView.getText().toString());
                int quantity = Integer.parseInt(quantityTextView.getText().toString());
                if (quantity > 0) {
                    quantity--;
                    sold++;

                    quantityString = Integer.toString(quantity);
                    soldString = Integer.toString(sold);

                    ContentValues values = new ContentValues();
                    values.put(InvEntry.COLUMN_INV_QUANTITY, quantityString);
                    values.put(InvEntry.COLUMN_INV_SOLD, soldString);

                    Log.e(LOG_TAG, "Value of mRowID: " + mRowId);

                    Uri currentProductUri = ContentUris.withAppendedId(InvEntry.CONTENT_URI,
                            mRowId);

                    Log.e(LOG_TAG, "currentProductUri: " + String.valueOf(currentProductUri));

                    int RowsUpdated = context.getContentResolver().update(currentProductUri, values, null, null);
                    quantityTextView.setText(quantityString);
                    soldTextView.setText(soldString);
                }
            }
        });
    }

}
