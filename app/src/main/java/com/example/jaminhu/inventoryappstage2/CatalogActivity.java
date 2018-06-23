package com.example.jaminhu.inventoryappstage2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jaminhu.inventoryappstage2.data.InventoryContract.InventoryEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;

    private InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        ListView listView = (ListView) findViewById(R.id.list_view);

        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        /*
        Alright so if I understand correctly, normally, the cursorprovider would go in here^ (into the "null",
        but now it's null because the cursor querying is something we want to happen in the background,
        which is of course the job of a loader, therefore requiring us to implement the LoaderCallback interface?
         */
        listView.setAdapter(mCursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                intent.setData(ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id));
                /* Wait ok so now we're relying on the fact that luckily, the id of the listViewItem we are given
                can be equated to the id of the item in the table?
                */
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, this);
        // What would this second parameter "args:" ^ be used for normally?
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.delete_all:
                getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
                return true;
            case R.id.dummy_data:

                ContentValues values = new ContentValues();

                values.put(InventoryEntry.NAME_COLUMN, "Test");
                values.put(InventoryEntry.PRICE_COLUMN, 1000);
                values.put(InventoryEntry.QUANTITY_COLUMN, 1000);
                values.put(InventoryEntry.SUPPLIER_COLUMN, "BnL");
                values.put(InventoryEntry.SUPPLIER_CONTACT_COLUMN, "BnL.com");

                Uri newRowUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

                Log.v("CatalogActivity", "New row URI: " + newRowUri);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.NAME_COLUMN,
                InventoryEntry.PRICE_COLUMN,
                InventoryEntry.QUANTITY_COLUMN};

        return new CursorLoader(this,
                //Wait, how is CursorLoader and Loader<Cursor> the same thing?
                InventoryEntry.CONTENT_URI,
                //So here ^ it wants the address of the database
                projection,
                null,
                null,
                null);
        /*
        So wait, this pretty much asks you "Hey, I'm gonna go to the grocerie store to fetch ingredients
        for your soup, what's the address of the shop and the stuff you wanna get?" then it returns a cursor object
        to be used elsewhere behind the scenes (in a Loader), the contents of which eventually are fed into the
        onLoadFinished() method below?
        In other words, this takes in what you want to get and then handles the "querying a database and getting a cursor back"
        in the background?
        What about the .query() and the .getReadableDatabase() methods?
         */


    } /* Also, wait so after this onCreateLoader is called, it returns a CursorLoader which then somehow
    calls the query() method from the InventoryProvider?
    */

    /*
    Also, do I understand correctly that only the querying is done with a loader because that might take time,
    but the "insert", "delete", and all that are still accessed directly from the Provider?
     */

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        //What's the difference between swapCursor() and changeCursor()?
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
