package com.example.bezru.converter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CurrencyFullList extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "currencyList";
    private static final String TABLE_CURRENCY = "currency";
    private static final String TABLE_CURRENCY_TO_CURRENCY = "currencyToCurrency";
    private static final String KEY_ID = "id";
    private static final String NAME = "name";
    private static final String NAME_SECOND = "nameTo";
    private static final String VALUE = "value";
    private LocalDateTime lastCachedDateTime = null;

    public CurrencyFullList(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CURRENCY_TABLE = "CREATE TABLE " + TABLE_CURRENCY + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + NAME + " TEXT " + ")";
        db.execSQL(CREATE_CURRENCY_TABLE);
        String CREATE_CURRENCY_TO_CURRENCY_TABLE = "CREATE TABLE " + TABLE_CURRENCY_TO_CURRENCY + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + NAME + " TEXT, " + NAME_SECOND + " TEXT, "
                + VALUE + " TEXT )";
        db.execSQL(CREATE_CURRENCY_TO_CURRENCY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENCY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENCY_TO_CURRENCY);

        onCreate(db);
    }

    public void addCurrencies(List<String> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < list.size(); i++) {
            ContentValues values = new ContentValues();
            values.put(NAME, list.get(i));
            db.insert(TABLE_CURRENCY, null, values);
        }
        db.close();

    }

    public List<String> getAllCurrenciesNames() {
        List<String> currencyList = new ArrayList<>();
        String selectQuery = "SELECT DISTINCT " + NAME + " FROM " + TABLE_CURRENCY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                currencyList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        return currencyList.stream().sorted().collect(Collectors.toList());
    }


    public List<String> getShortCurrenciesNamesList() {
        List<String> currencyList = new ArrayList<>();
        String selectQuery = "SELECT DISTINCT " + NAME + " FROM " + TABLE_CURRENCY + " WHERE " + NAME + " IN (" + "'RUB', "
                + "'EUR', " + "'USD', " + "'BYN')";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                currencyList.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        return currencyList.stream().sorted().collect(Collectors.toList());
    }

    public void addCacheValues(List<String> strings) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (String from : strings) {
            for (String to : strings) {
                if (!from.equals(to)) {
                    ConvectValuesAsyncTask convectValuesAsyncTask = new ConvectValuesAsyncTask();
                    convectValuesAsyncTask.execute(from, to);
                    ContentValues values = new ContentValues();
                    values.put(NAME, from);
                    values.put(NAME_SECOND, to);
                    try {
                        values.put(VALUE, convectValuesAsyncTask.get());
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    db.insert(TABLE_CURRENCY_TO_CURRENCY, null, values);
                }
            }
        }
        lastCachedDateTime = LocalDateTime.now();
    }

    public String getValue(String from, String to) {
        String result = null;
        String selectQuery = "SELECT " + VALUE + " FROM " + TABLE_CURRENCY_TO_CURRENCY + " WHERE "
                + NAME + " = '" + from + "' AND " + NAME_SECOND + " = '" + to + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                result = cursor.getString(0);
            } while (cursor.moveToNext());
        }

        return result;
    }

    public LocalDateTime getLastCachedDateTime() {
        return lastCachedDateTime;
    }
}