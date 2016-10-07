package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InvContract.InvEntry;

/**
 * Created by gamelord on 10/4/16.
 */

public class InvCursorAdapter extends CursorAdapter {
    public InvCursorAdapter(Context context, Cursor c) { super(context, c,0);}
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }
    // build a method to bind inventory data to a list
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);

        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_PRICE);

        // Read the item attributes from the Cursor for the current item
        String invName = cursor.getString(nameColumnIndex);
        String invPrice = cursor.getString(priceColumnIndex);
        String invQuantity = cursor.getString(quantityColumnIndex);

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(invName);
        quantityTextView.setText(invQuantity);
        priceTextView.setText(invPrice);
    }
}
