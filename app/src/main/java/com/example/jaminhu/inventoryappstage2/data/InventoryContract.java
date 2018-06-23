package com.example.jaminhu.inventoryappstage2.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.jaminhu.inventoryappstage2";
    //So this needs to match the android manifests provider authority? What does this really mean?d
    //And why don't I, for example, need to put a ".data" after this

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String INVENTORY_PATH = "items";
    /*So is this the name of the table? if so, why don't we just use TABLE_NAME from below?
    Also, in the InventoryDbHelper, we made a database called "inventoryDatabase"... so does the CONTENT_URI not care about that?
    In other words, when something asks for the Provider with a Content Uri address, do they not need to know the name of the database
    that the table they're looking for is in?
    */
    public static class InventoryEntry implements BaseColumns {


        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + INVENTORY_PATH;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + INVENTORY_PATH;
/*
ok so still not quite sure what the point of MIME in all of this is...
 */

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, INVENTORY_PATH);
        public static final String TABLE_NAME = "items";

        public static final String _ID = BaseColumns._ID;
        /*
        Ok so I've been wondering, why do we need to implement BaseColumns... Then I saw this BaseColumns._ID call
        So to is the only reason to implement BaseColumns to autoincrement the ID column?
         */
        public static final String NAME_COLUMN = "name";
        public static final String PRICE_COLUMN = "price";
        public static final String QUANTITY_COLUMN = "quantity";
        public static final String SUPPLIER_COLUMN = "supplier";
        public static final String SUPPLIER_CONTACT_COLUMN = "supplier_contact";
    }
}
