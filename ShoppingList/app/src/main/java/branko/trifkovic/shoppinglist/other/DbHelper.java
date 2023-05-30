package branko.trifkovic.shoppinglist.other;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import branko.trifkovic.shoppinglist.allListAdapter.AllShoppingListsElement;
import branko.trifkovic.shoppinglist.itemListAdapter.OneShoppingListElement;

public class DbHelper extends SQLiteOpenHelper {

    // USERS TABLE
    private final String TABLE_USERS = "USERS";
    private final String COLUMN_USERNAME = "username";
    private final String COLUMN_EMAIL = "email";
    private final String COLUMN_PASSWORD = "password";
    private final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " (" + COLUMN_USERNAME + " TEXT UNIQUE ON CONFLICT IGNORE, " + COLUMN_EMAIL + " TEXT, " + COLUMN_PASSWORD + " TEXT) ;";

    // LISTS TABLE
    private final String TABLE_LISTS = "LISTS";
    private final String COLUMN_LIST_TITLE = "list_title";
    private final String COLUMN_LIST_OWNER = "list_owner";
    private final String COLUMN_LIST_SHARED = "shared";
    private final String CREATE_TABLE_LISTS = "CREATE TABLE " + TABLE_LISTS + " (" + COLUMN_LIST_TITLE + " TEXT UNIQUE ON CONFLICT IGNORE, " + COLUMN_LIST_OWNER + " TEXT, " + COLUMN_LIST_SHARED + " INTEGER) ;";


    // ITEMS TABLE
    private final String TABLE_ITEMS = "ITEMS";
    private final String COLUMN_ITEM_ID = "id";
    private final String COLUMN_ITEM_NAME = "item_name";
    private final String COLUMN_ITEM_PARENT = "list_name";
    private final String COLUMN_ITEM_CHECKED = "checked";
    private final String CREATE_TABLE_ITEMS = "CREATE TABLE " + TABLE_ITEMS + " (" + COLUMN_ITEM_ID + " TEXT, " + COLUMN_ITEM_NAME + " TEXT, " + COLUMN_ITEM_PARENT + " TEXT , " + COLUMN_ITEM_CHECKED + " INTEGER) ;";

    public DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_USERS);
        sqLiteDatabase.execSQL(CREATE_TABLE_LISTS);
        sqLiteDatabase.execSQL(CREATE_TABLE_ITEMS);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    // table USERS methods
    public long registerUser(User user) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues val = new ContentValues();
        val.put(COLUMN_USERNAME, user.getmUsername());
        val.put(COLUMN_EMAIL, user.getmEmail());
        val.put(COLUMN_PASSWORD, user.getmPassword());

        long rv = db.insert(TABLE_USERS, null, val);
        close();

        return rv;
    }

    public boolean readUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        boolean rv;

        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USERNAME + " =?", new String[] {username}, null, null, null);

        if (cursor.getCount() <= 0) {
            rv = false;
        } else {

            cursor.moveToFirst();

            User user = createUser(cursor);

            if (user.getmUsername().equals(username) && user.getmPassword().equals(password))
                rv = true;
            else
                rv = false;
        }

        close();
        return rv;
    }

    private User createUser(Cursor cursor) {
        String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
        String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));

        return new User(username, email, password);
    }


    // table LISTS methods
    public AllShoppingListsElement[] readSharedLists(String owner) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_LISTS, null, COLUMN_LIST_SHARED + "=? AND " + COLUMN_LIST_OWNER + "!=?", new String[] {Integer.toString(1), owner}, null, null, null);

        if(cursor.getCount() < 0)
            return null;

        AllShoppingListsElement[] list = new AllShoppingListsElement[cursor.getCount()];

        int i = 0;
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIST_TITLE));
            String shared = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIST_SHARED)) != 0) ? "true" : "false";

            list[i++] = new AllShoppingListsElement(title, shared, owner);
        }

        close();

        return list;
    }

    public AllShoppingListsElement[] readAllLists(String owner) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_LISTS, null, COLUMN_LIST_OWNER + "=?", new String[] {owner}, null, null, null);

        if(cursor.getCount() < 0)
            return null;

        AllShoppingListsElement[] list = new AllShoppingListsElement[cursor.getCount()];

        int i = 0;
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIST_TITLE));
            String shared = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIST_SHARED)) != 0) ? "true" : "false";

            list[i++] = new AllShoppingListsElement(title, shared, owner);
        }

        close();

        return list;
    }

    public AllShoppingListsElement[] readMyLists(String owner) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_LISTS, null, COLUMN_LIST_OWNER + "=?", new String[] {owner}, null, null, null);

        if(cursor.getCount() < 0)
            return null;

        AllShoppingListsElement[] list = new AllShoppingListsElement[cursor.getCount()];

        int i = 0;
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LIST_TITLE));
            String shared = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LIST_SHARED)) != 0) ? "true" : "false";

            list[i++] = new AllShoppingListsElement(title, shared, owner);
        }

        close();

        return list;
    }

    public long addList(String title, int shared, String owner) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues val = new ContentValues();
        val.put(COLUMN_LIST_TITLE, title);
        val.put(COLUMN_LIST_OWNER, owner);
        val.put(COLUMN_LIST_SHARED, shared);

        long rv = db.insert(TABLE_LISTS, null, val);
        close();

        return rv;
    }

    public void deleteList(String listTitle) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LISTS, COLUMN_LIST_TITLE + " =?", new String[] {listTitle});
        db.close();
    }

    public void deleteListByUsername(String username) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LISTS, COLUMN_LIST_OWNER + " =?", new String[] {username});
        db.close();
    }


    // table ITEMS methods
    public OneShoppingListElement[] readItems(String listName) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_ITEMS, null, COLUMN_ITEM_PARENT + "=?", new String[] {listName}, null, null, null);

        if(cursor.getCount() < 0)
            return null;

        OneShoppingListElement[] list = new OneShoppingListElement[cursor.getCount()];

        int i = 0;
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME));
            boolean checked = (cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_CHECKED)) != 0) ? true : false;
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));

            list[i++] = new OneShoppingListElement(title, checked, id);
        }

        close();

        return list;
    }

    public void addItem(OneShoppingListElement el, String listName) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues val = new ContentValues();
        val.put(COLUMN_ITEM_NAME, el.getTask());
        val.put(COLUMN_ITEM_CHECKED, el.ismDoneTask() ? 1 : 0);
        val.put(COLUMN_ITEM_PARENT, listName);
        val.put(COLUMN_ITEM_ID, el.getId());

        db.insert(TABLE_ITEMS, null, val);

        close();
    }

    public void deleteItem(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ITEMS, COLUMN_ITEM_ID + " =?", new String[] {id});
        db.close();
    }

    public void updateItemChecked(String id, boolean checked) {
        SQLiteDatabase db = getWritableDatabase();
        int val = checked ? 1 : 0;

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ITEM_CHECKED, val);

        db.update(TABLE_ITEMS, cv, COLUMN_ITEM_ID + "= ?", new String[] {id});

        close();
    }

    public int getMaxId() {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.query(TABLE_ITEMS, new String[] {COLUMN_ITEM_ID}, null, null, null, null, COLUMN_ITEM_ID + " DESC");

        int max_id = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            max_id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));
        }

        close();

        return max_id;
    }
}
