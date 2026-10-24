package com.example.codefest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "relief_distribution.db";
    private static final int DATABASE_VERSION = 1;

    // History Table
    private static final String TABLE_HISTORY = "history";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "household_name";
    private static final String COLUMN_SIZE = "family_size";
    private static final String COLUMN_RICE = "rice_qty";
    private static final String COLUMN_CANS = "cans_qty";
    private static final String COLUMN_PRIORITY = "is_priority";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    // Inventory Table
    private static final String TABLE_INVENTORY = "inventory";
    private static final String COLUMN_INV_TYPE = "item_type";
    private static final String COLUMN_INV_QTY = "quantity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createHistoryTable = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_SIZE + " INTEGER, " +
                COLUMN_RICE + " INTEGER, " +
                COLUMN_CANS + " INTEGER, " +
                COLUMN_PRIORITY + " INTEGER, " +
                COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createHistoryTable);

        String createInventoryTable = "CREATE TABLE " + TABLE_INVENTORY + " (" +
                COLUMN_INV_TYPE + " TEXT PRIMARY KEY, " +
                COLUMN_INV_QTY + " INTEGER)";
        db.execSQL(createInventoryTable);

        // Initialize inventory
        db.execSQL("INSERT INTO " + TABLE_INVENTORY + " VALUES ('rice', 100)");
        db.execSQL("INSERT INTO " + TABLE_INVENTORY + " VALUES ('cans', 150)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // Inventory Methods
    public int getStock(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_INVENTORY, new String[]{COLUMN_INV_QTY}, COLUMN_INV_TYPE + "=?", new String[]{type}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int qty = cursor.getInt(0);
                cursor.close();
                return qty;
            }
            cursor.close();
        }
        return 0;
    }

    public void updateStock(String type, int change) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_INVENTORY + " SET " + COLUMN_INV_QTY + " = " + COLUMN_INV_QTY + " + (" + change + ") WHERE " + COLUMN_INV_TYPE + " = ?", new String[]{type});
    }

    // History Methods
    public long saveDistribution(String name, int size, int rice, int cans, boolean priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_SIZE, size);
        values.put(COLUMN_RICE, rice);
        values.put(COLUMN_CANS, cans);
        values.put(COLUMN_PRIORITY, priority ? 1 : 0);

        long id = db.insert(TABLE_HISTORY, null, values);
        if (id != -1) {
            updateStock("rice", -rice);
            updateStock("cans", -cans);
        }
        return id;
    }

    public List<DistributionRecord> getHistory() {
        List<DistributionRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + COLUMN_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new DistributionRecord(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getInt(5) == 1,
                        cursor.getString(6)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void deleteDistribution(int id, int rice, int cans) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        updateStock("rice", rice);
        updateStock("cans", cans);
    }
}
