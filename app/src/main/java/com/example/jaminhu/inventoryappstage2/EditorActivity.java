package com.example.jaminhu.inventoryappstage2;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jaminhu.inventoryappstage2.data.InventoryContract.InventoryEntry;

import org.w3c.dom.Text;

import java.util.Locale;

import static android.view.View.GONE;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = 1;

    private Uri mContentUri;

    private EditText mNameView;
    private EditText mPriceView;
    private EditText mQuantityView;
    private EditText mSupplierView;
    private EditText mSupplierContactView;
    private Button mSubtractButton;
    private Button mAddButton;
    private Button mContactButton;

    private boolean mContentChanged;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mContentUri = getIntent().getData();

        mNameView = (EditText) findViewById(R.id.edit_item_name);
        mPriceView = (EditText) findViewById(R.id.edit_item_price);
        mQuantityView = (EditText) findViewById(R.id.edit_item_quantity);
        mSupplierView = (EditText) findViewById(R.id.edit_item_supplier);
        mSupplierContactView = (EditText) findViewById(R.id.edit_item_supplier_contact);
        mSubtractButton = (Button) findViewById(R.id.subtract_button);
        mAddButton = (Button) findViewById(R.id.add_button);
        mContactButton = (Button) findViewById(R.id.contact_button);

        mSubtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(mQuantityView.getText().toString().trim());
                if (currentQuantity > 0) {
                    int newQuantity = currentQuantity - 1;
                    mQuantityView.setText(String.valueOf(newQuantity));
                }
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(mQuantityView.getText().toString().trim());
                int newQuantity = currentQuantity + 1;
                mQuantityView.setText(String.valueOf(newQuantity));
            }
        });

        if (mContentUri == null){
            setTitle(getString(R.string.editor_activity_title_new_item));
            mQuantityView.setText("0");
            invalidateOptionsMenu();
            //Wait, so what does this actually do/call? Does this eventually call the onPrepareOptionsMenu?
        } else {
            getLoaderManager().initLoader(LOADER_ID, null, this);
            setTitle(getString(R.string.editor_activity_title_edit_item));
        }

        mNameView.setOnTouchListener(mTouchListener);
        mPriceView.setOnTouchListener(mTouchListener);
        mQuantityView.setOnTouchListener(mTouchListener);
        mSupplierView.setOnTouchListener(mTouchListener);
        mSupplierContactView.setOnTouchListener(mTouchListener);
        mSubtractButton.setOnTouchListener(mTouchListener);
        mAddButton.setOnTouchListener(mTouchListener);

        mContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String supplierContactString = mSupplierContactView.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + supplierContactString));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        //Is this the method that's eventually called after calling the invalidateOptionsMenu()?
        if (mContentUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                saveItem();
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:

                if (!mContentChanged){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
        //Why can't we just type here^ "return true"?
    }

    public void saveItem(){

        String nameString = mNameView.getText().toString().trim();

        String priceString = mPriceView.getText().toString().trim();

        String supplierString = mSupplierView.getText().toString().trim();

        String supplierContactString = mSupplierContactView.getText().toString();

        String supplierPhoneString;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supplierPhoneString = PhoneNumberUtils.formatNumber(supplierContactString, Locale.getDefault().getCountry());
        } else {
            supplierPhoneString = PhoneNumberUtils.formatNumber(supplierContactString);
        }

        if (TextUtils.isEmpty(nameString) ||
                TextUtils.isEmpty(priceString)||
                TextUtils.isEmpty(supplierString)||
                TextUtils.isEmpty(supplierContactString)){
            Toast.makeText(this, "All fields should be populated",
                    Toast.LENGTH_LONG).show();
            return;
        }

        int priceInt = Integer.parseInt(priceString);
        // Hmm... what if I pass in the price into the "values" as a string... will it still be
        //stored in the SQLite database as an Integer?

        int quantity = 0;
        String quantityString = mQuantityView.getText().toString().trim();
        if (!TextUtils.isEmpty(quantityString)){
            quantity = Integer.parseInt(quantityString);
        }

        ContentValues values = new ContentValues();

        values.put(InventoryEntry.NAME_COLUMN, nameString);
        values.put(InventoryEntry.PRICE_COLUMN, priceInt);
        values.put(InventoryEntry.QUANTITY_COLUMN, quantity);
        values.put(InventoryEntry.SUPPLIER_COLUMN, supplierString);
        values.put(InventoryEntry.SUPPLIER_CONTACT_COLUMN, supplierPhoneString);

        if (mContentUri == null) {

            Uri newRowUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newRowUri == null){
                Toast.makeText(this, "Error adding item",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Successfully added item",
                        Toast.LENGTH_LONG).show();
            }

        } else {
            int rowsUpdated = getContentResolver().update(mContentUri, values, null, null);
            //So technically speaking here, the option of giving this^¨and this ----^ is redundant because
            // the "values" variable is handling what things to update? or why?

            if (rowsUpdated == 0){
                Toast.makeText(this, "Error updating item",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Successfully updated item",
                        Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }

    public void deleteItem(){

        int rowsDeleted = getContentResolver().delete(mContentUri, null, null);

        if (rowsDeleted == 0){
            Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                    Toast.LENGTH_LONG).show();
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.NAME_COLUMN,
                InventoryEntry.PRICE_COLUMN,
                InventoryEntry.QUANTITY_COLUMN,
                InventoryEntry.SUPPLIER_COLUMN,
                InventoryEntry.SUPPLIER_CONTACT_COLUMN};

        return new CursorLoader(this,
                mContentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || data.getCount() < 1){
            return;
        }

        data.moveToFirst();

        mNameView.setText(data.getString(data.getColumnIndex(InventoryEntry.NAME_COLUMN)));
        mPriceView.setText(data.getString(data.getColumnIndex(InventoryEntry.PRICE_COLUMN)));
        mQuantityView.setText(data.getString(data.getColumnIndex(InventoryEntry.QUANTITY_COLUMN)));
        mSupplierView.setText(data.getString(data.getColumnIndex(InventoryEntry.SUPPLIER_COLUMN)));
        mSupplierContactView.setText(data.getString(data.getColumnIndex(InventoryEntry.SUPPLIER_CONTACT_COLUMN)));

    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mContentChanged = true;
            return false;
        }
    };

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onBackPressed() {

        if (!mContentChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
