package com.example.ourbookapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库信息
    private static final String DATABASE_NAME = "UsedBooks.db";
    private static final int DATABASE_VERSION = 1;

    // 表名和列名
    public static final String TABLE_BOOKS = "books";
    public static final String COLUMN_BOOK_ID = "book_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_LATITUDE = "latitude"; // 核心：存储纬度
    public static final String COLUMN_LONGITUDE = "longitude"; // 核心：存储经度
    public static final String COLUMN_LOCATION_NAME = "location_name";
    public static final String COLUMN_SELLER_ID = "seller_id";

    // 创建表的SQL命令
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_BOOKS + " (" +
                    COLUMN_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // 主键，自增
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_PRICE + " REAL, " + // REAL 类型用于存储浮点数（价格）
                    COLUMN_LATITUDE + " REAL NOT NULL, " + // 存储纬度，不能为空
                    COLUMN_LONGITUDE + " REAL NOT NULL, " + // 存储经度，不能为空
                    COLUMN_LOCATION_NAME + " TEXT, " +
                    COLUMN_SELLER_ID + " TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 当数据库第一次被创建时执行此方法
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 当数据库需要升级时（版本号增加），执行此方法
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        onCreate(db);
    }


    public long addBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        try {
            values.put(COLUMN_TITLE, book.getTitle());
            values.put(COLUMN_PRICE, book.getPrice());
            values.put(COLUMN_LATITUDE, book.getLatitude());
            values.put(COLUMN_LONGITUDE, book.getLongitude());
            values.put(COLUMN_LOCATION_NAME, book.getLocationName());
            values.put(COLUMN_SELLER_ID, book.getSellerId());

            // 插入数据并返回行ID
            long result = db.insert(TABLE_BOOKS, null, values);
            Log.d("Database", "添加书籍成功，ID: " + result);
            return result;

        } finally {
            db.close(); // 确保关闭数据库
        }
    }

    // 后续可以在这里添加插入（insertBook）、查询（getAllBooks）等方法
    // 在 DatabaseHelper 类中添加此方法
    public List<Book> getAllBooks() {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_BOOKS, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Book book = new Book(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION_NAME)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELLER_ID))
                    );
                    book.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)));
                    bookList.add(book);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return bookList;
    }
}