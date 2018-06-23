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

import com.example.jaminhu.inventoryappstage2.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    /** URI matcher code for the content URI for the pets table */
    private static final int INVENTORY = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
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

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.INVENTORY_PATH , INVENTORY);
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
            case (INVENTORY):
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
still not sure what this does exactly... and why necessary
 */
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
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
            case INVENTORY:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values){

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = values.getAsString(InventoryEntry.NAME_COLUMN);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        Integer price = values.getAsInteger(InventoryEntry.PRICE_COLUMN); //why is this variable type Integer instead of int
        if (price == null || price < 0 ) {
            throw new IllegalArgumentException("Pet requires a valid gender");
        }

        Integer quantity = values.getAsInteger(InventoryEntry.QUANTITY_COLUMN);
        if (quantity != null && quantity < 0) { //hmmmmmmmm compare with if (weight == null || weight < 0 )...
            throw new IllegalArgumentException("Pet requires a valid weight");
        }



        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
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
        Ok so this is pretty much the class that contains methods that take in URI's and turn them into sql commands?
         */

        SQLiteDatabase db =mDbHelper.getWritableDatabase();

        selection = InventoryEntry._ID + "=?";
        selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

        int rowsUpdated = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }
}
