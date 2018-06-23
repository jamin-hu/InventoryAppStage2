package com.example.jaminhu.inventoryappstage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.jaminhu.inventoryappstage2.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /*Ok now I'm confused... this constructor is not in the lightbulb suggested list... not even in Android documentation,
    So if I were to have done this myself without looking at the code from the Pets App, how would I have known
    to make this constructor instead of the suggested:
            public InventoryDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
    */

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_INVENTORY_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryEntry.NAME_COLUMN + " TEXT NOT NULL, " +
                InventoryEntry.PRICE_COLUMN + " INTEGER NOT NULL, " +
                InventoryEntry.QUANTITY_COLUMN + " INTEGER, " +
                InventoryEntry.SUPPLIER_COLUMN + " TEXT, " +
                InventoryEntry.SUPPLIER_CONTACT_COLUMN + " INTEGER" +
                ");";

        db.execSQL(CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
