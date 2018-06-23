package com.example.jaminhu.inventoryappstage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.jaminhu.inventoryappstage2.data.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View emptyListItem = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        // what is are these "false" doing here---------------------------------------^^^^^^------------^^^^
        return emptyListItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameView = view.findViewById(R.id.name_view);
                /*Why does this "findViewById() need a "view." preceding it when in other places it doesn't
                I kind of get that ok this is taking in the view that will be populated but why can't it work without the "view."
                And also, why does it work without the "view." in other scenarios such as in the CatalogActivity's "onCreate"?
                 */
        nameView.setText(cursor.getString(cursor.getColumnIndex("name")));

        TextView priceView = view.findViewById(R.id.price_view);
        String price = cursor.getString(cursor.getColumnIndex("price"));
        String priceViewText = "Price: " + price + " €";
        priceView.setText(priceViewText);

        TextView quantityView = view.findViewById(R.id.quantity_view);
        String quantity = cursor.getString(cursor.getColumnIndex("quantity"));
        String quantityViewText = "In stock: " + quantity + "pcs";
        quantityView.setText(quantityViewText);

        final int quantityViewInt = Integer.parseInt(quantity);

        final int itemId = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));

        Button buttonView = view.findViewById(R.id.sale_button);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantityViewInt > 0){
                    int newQuantity =  quantityViewInt - 1;
                    ContentValues newQuantityValue = new ContentValues();
                    newQuantityValue.put(InventoryEntry.QUANTITY_COLUMN, newQuantity);
                    //ok now here the value put in is an integer... unlike other places where I think they were mostly strings.

                    Uri currentUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemId);
                    int rowsUpdated = context.getContentResolver().update(currentUri, newQuantityValue, null, null);
                    /*Ok so how should I know to put "context" before the getContentResolver()? Is it because this current class I'm in
                    isn't actually an AppCompat Activity of any sorts and the "context" actually returns the activity which contains the
                    getContentResolver() method needed?
                    */


                }

            }
        });
    }
}
