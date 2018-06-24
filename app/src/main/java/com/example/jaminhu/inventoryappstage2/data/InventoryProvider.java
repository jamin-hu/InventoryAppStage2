package com.example.jaminhu.inventoryappstage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.jaminhu.inventoryappstage2.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    private static final int INVENTORY_ID = 100;

    private static final int ITEM_ID = 101;

    private InventoryDbHelper mDbHelper;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.INVENTORY_PATH , INVENTORY_ID);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.INVENTORY_PATH + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        //Why doesnt typing "this" as context ^ work here?
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match){
            case (INVENTORY_ID):
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case (ITEM_ID):
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        /*
        this is important to do man... remember always! otherwise the other these:
            getContext().getContentResolver().notifyChange(uri, null);
        will not work
         */
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
/*
still not sure what this does exactly... and why necessary... yet...
 */
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        //wait so this-------------^ is the uri of the table? how is this different from InventoryEntry.CONTENT_URI;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_ID:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values){


        String name = values.getAsString(InventoryEntry.NAME_COLUMN);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        Integer price = values.getAsInteger(InventoryEntry.PRICE_COLUMN); //why is this variable type Integer instead of int
        if (price == null || price < 0 ) {
            throw new IllegalArgumentException("Pet requires a valid price");
        }

        Integer quantity = values.getAsInteger(InventoryEntry.QUANTITY_COLUMN);
        if (quantity != null && quantity < 0) { //hmmmmmmmm compare with if (weight == null || weight < 0 )...
            throw new IllegalArgumentException("Item requires a valid quantity");
        }

        String supplier = values.getAsString(InventoryEntry.SUPPLIER_COLUMN);
        if (supplier == null) {
            throw new IllegalArgumentException("Item requires a supplier name");
        }

        String supplierContact = values.getAsString(InventoryEntry.SUPPLIER_CONTACT_COLUMN);
        if (supplierContact == null) {
            throw new IllegalArgumentException("Item requires a phone number");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_ID:
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, InventoryEntry._ID + "=?", new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        /*
        Ok so this whole class is pretty much the class that contains methods that take in URI's and turn them into sql commands?
         */

        final int match = sUriMatcher.match(uri);
        switch (match) {
            /*
            I mean technically, does there even need to be this switch statement here in the update() as well as in the insert()?
            Because the update is only called from the EditorView when it was opened through a ListView item click, and the insert()
            is only called from the EditorView when it was opened through the FAB button thing... so it's pretty much guaranteed that the
            MIME type will always be an item type for the update() method, and table type for the insert() method?
             */
            case ITEM_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(InventoryEntry.NAME_COLUMN)) {
            String name = values.getAsString(InventoryEntry.NAME_COLUMN);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        if (values.containsKey(InventoryEntry.PRICE_COLUMN)) {
            Integer price = values.getAsInteger(InventoryEntry.PRICE_COLUMN);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Item requires valid price");
            }
        }

        if (values.containsKey(InventoryEntry.QUANTITY_COLUMN)) {
            Integer quantity = values.getAsInteger(InventoryEntry.QUANTITY_COLUMN);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Item requires valid quantity");
            }
        }

        if (values.containsKey(InventoryEntry.SUPPLIER_COLUMN)) {
            String supplier = values.getAsString(InventoryEntry.SUPPLIER_COLUMN);
            if (supplier == null) {
                throw new IllegalArgumentException("Item requires a supplier name");
            }
        }

        if (values.containsKey(InventoryEntry.SUPPLIER_CONTACT_COLUMN)) {
            String supplierContact = values.getAsString(InventoryEntry.SUPPLIER_CONTACT_COLUMN);
            if (supplierContact == null) {
                throw new IllegalArgumentException("Item requires a phone number");
            }
        }

        if(values.size() == 0){
            return 0;
        } else {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            int numOfRows = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

            if (numOfRows !=0){
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return numOfRows;
        }

    }
}
