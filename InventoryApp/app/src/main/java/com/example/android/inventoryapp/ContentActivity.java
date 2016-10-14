package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InvContract.InvEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gamelord on 10/4/16.
 */

public class ContentActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // Identifier for the item data loader
    private static final int EXISTING_INV_LOADER = 0;
    // Content URI for the existing item
    private Uri mCurrentInvUri;
    // EditText field to enter the item's name, price and quantity
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSoldText;
    private ImageView mImageView;
    private int number = 0;
    private int basequantity = 0;

    // Boolean flag that keeps track of whether the item has been edited or not
    private boolean mInvHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInvHasChanged = true;
            return false;
        }
    };

    static final int REQUEST_TAKE_PHOTO = 1;
    Boolean imageStatus = false;
    private String mImagePath;
    String imagePath;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);


        // Examine the intent that was used to launch this activity to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentInvUri = intent.getData();

        // If the intent DOES NOT contain a item content URI, then we know that we are creating a new item
        if (mCurrentInvUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
            // hire buttons and textView
            Button sellButton = (Button) findViewById(R.id.sell);
            sellButton.setVisibility(View.GONE);
            Button restockButton = (Button) findViewById(R.id.restock);
            restockButton.setVisibility(View.GONE);
            Button ordermoreButton = (Button) findViewById(R.id.ordermore);
            ordermoreButton.setVisibility(View.GONE);
            TextView inventory = (TextView) findViewById(R.id.inventory_guideline);
            inventory.setVisibility(View.GONE);
            Button imageButton = (Button) findViewById(R.id.takePhoto);
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    imageStatus = false;
                    dispatchTakePictureIntent();

                }
            });
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit an item"
            setTitle(getString(R.string.editor_activity_title_edit_item));

            // Initialize a loader to read the item data from the database and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_INV_LOADER, null, this);
            Button sellButton = (Button) findViewById(R.id.sell);
            sellButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    basequantity = 0;
                    number = -1;
                    saveInv();
                }
            });
            Button restockButton = (Button) findViewById(R.id.restock);
            restockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    basequantity = -1;
                    number = +1;
                    saveInv();
                }
            });
            Button ordermoreButton = (Button) findViewById(R.id.ordermore);
            ordermoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String nameString = mNameEditText.getText().toString().trim();
                    String priceString = mPriceEditText.getText().toString().trim();
                    String quantityString = mQuantityEditText.getText().toString().trim();
                    String orderMessage = createOrderSummary(nameString, priceString, quantityString);
                    composeEmail(nameString, "New oder request for " + nameString, orderMessage);
                }
            });
            Button imageButton = (Button) findViewById(R.id.takePhoto);
            imageButton.setVisibility(View.GONE);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_inv_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_inv_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_inv_quantity);
        mImageView = (ImageView) findViewById(R.id.product_image);
        mSoldText = (EditText) findViewById(R.id.sold_quantity);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSoldText.setOnTouchListener(mTouchListener);
    }

    /**
     * Get user input from editor and save item into database.
     **/
    private void saveInv() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String soldString = mSoldText.getText().toString().trim();
        int quantityInt = Integer.parseInt(quantityString);
        int priceInt = Integer.parseInt(priceString);
        int soldInt = Integer.parseInt(soldString);
        if (quantityInt <= basequantity || priceInt <= 0) {
            Toast.makeText(this, getString(R.string.editor_quantity_edit_failed),
                    Toast.LENGTH_SHORT).show();
            return;
        } else {
            quantityInt = quantityInt + number; //add number to the quantity number
            soldInt = soldInt - number;
            quantityString = String.valueOf(quantityInt);
            soldString = String.valueOf(soldInt);
        }

        // Check if this is supposed to be a new item
        // and check if any of the fields in the editor are blank
        if (mCurrentInvUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(soldString)) {
            Toast.makeText(this, getString(R.string.editor_empty_not_allow),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys, and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InvEntry.COLUMN_INV_NAME, nameString);
        values.put(InvEntry.COLUMN_INV_PRICE, priceString);
        values.put(InvEntry.COLUMN_INV_QUANTITY, quantityString);
        values.put(InvEntry.COLUMN_INV_SOLD, soldString);
        values.put(InvEntry.COLUMN_INV_IMAGE, mImagePath);


        // Determine if this is a new or existing pet by checking if mCurrentPetUri is null or not
        if (mCurrentInvUri == null) {
            // This is a NEW item
            Uri newUri = getContentResolver().insert(InvEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_inv_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_inv_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUr and pass in the new ContentValues.
            int rowsAffected = getContentResolver().update(mCurrentInvUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_inv_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_inv_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_content.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_content, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentInvUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                saveInv();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity which is the {@link CatalogActivity}.
                if (!mInvHasChanged) {
                    NavUtils.navigateUpFromSameTask(ContentActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(ContentActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mInvHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InvEntry._ID,
                InvEntry.COLUMN_INV_NAME,
                InvEntry.COLUMN_INV_PRICE,
                InvEntry.COLUMN_INV_QUANTITY,
                InvEntry.COLUMN_INV_SOLD,
                InvEntry.COLUMN_INV_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentInvUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_QUANTITY);
            int soldColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_SOLD);
            int imageColumnIndex = cursor.getColumnIndex(InvEntry.COLUMN_INV_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int sold = cursor.getInt(soldColumnIndex);
            String mImagePath = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSoldText.setText(Integer.toString(sold));

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mImagePath, bmOptions);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(mImagePath, bmOptions);
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSoldText.setText("");

    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteInv();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the pet in the database.
     */
    private void deleteInv() {
        // Only perform the delete if this is an existing .
        if (mCurrentInvUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentInvUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_inv_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_inv_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    // Create an order summary
    private String createOrderSummary(String nameString, String priceString, String quantityString) {
        int quantityInt = Integer.parseInt(quantityString);
        int priceInt = Integer.parseInt(priceString);
        String orderMessage = "Order request for : " + nameString;
        orderMessage = orderMessage + "\nQuantity: " + quantityInt;
        orderMessage = orderMessage + "\nPrice per unit: $" + priceInt * 0.9;
        orderMessage = orderMessage + "\nTotal: $" + priceInt * 0.9 * quantityInt;
        orderMessage = orderMessage + "\nThank you!";
        return orderMessage;

    }

    // Compose email to supplier
    public void composeEmail(String addresses, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    // Camera handler
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (imageFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.inventoryapp",
                        imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == REQUEST_TAKE_PHOTO && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                mImageView.setImageBitmap(photo);
            }

            try {
                createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(image.getAbsolutePath(), Context.MODE_PRIVATE);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save a file: path for use with ACTION_VIEW intents
        mImagePath = "file:" + image.getAbsolutePath();
        if (!imageStatus) {
            imagePath = image.getAbsolutePath();
            imageStatus = true;
        }
        return image;
    }
}
