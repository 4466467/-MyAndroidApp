package com.example.ourbookapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.sqlite.SQLiteConstraintException;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 统一数据库信息
    private static final String DATABASE_NAME = "UsedBooks.db";
    private static final int DATABASE_VERSION = 5;

    // 用户表定义
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id"; // 统一使用自增ID
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";

    // 书籍表定义
    public static final String TABLE_BOOKS = "books";
    public static final String COLUMN_BOOK_ID = "book_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LOCATION_NAME = "location_name";
    public static final String COLUMN_SELLER_ID = "seller_id";
    public static final String COLUMN_DESCRIPTION = "description";

    // 用户表创建语句（统一版本）
    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USERNAME + " TEXT UNIQUE NOT NULL," +
            COLUMN_PASSWORD + " TEXT NOT NULL," +
            COLUMN_EMAIL + " TEXT UNIQUE" +
            ");";

    // 书籍表创建语句
    private static final String CREATE_BOOK_TABLE =
            "CREATE TABLE " + TABLE_BOOKS + " (" +
                    COLUMN_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_PRICE + " REAL, " +
                    COLUMN_LATITUDE + " REAL NOT NULL, " +
                    COLUMN_LONGITUDE + " REAL NOT NULL, " +
                    COLUMN_LOCATION_NAME + " TEXT, " +
                    COLUMN_SELLER_ID + " TEXT," +
                    COLUMN_DESCRIPTION + " TEXT," +
                    "seller_contact TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    } */

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d("DatabaseHelper", "创建数据库表，版本: " + DATABASE_VERSION);
            db.execSQL(CREATE_USER_TABLE);
            db.execSQL(CREATE_BOOK_TABLE);
            Log.d("DatabaseHelper", "数据库表创建成功");
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "表创建失败: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "数据库升级: 从版本 " + oldVersion + " 到 " + newVersion);

        try {
            // 强制重建所有表（从任何旧版本升级到版本5）
            if (oldVersion < 5) {
                Log.d("DatabaseHelper", "重建数据库表...");

                // 删除旧表
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

                // 重新创建表
                db.execSQL(CREATE_USER_TABLE);
                db.execSQL(CREATE_BOOK_TABLE);

                Log.d("DatabaseHelper", "数据库表已重建（版本" + DATABASE_VERSION + "）");
            }
        } catch (SQLException e) {
            Log.e("DatabaseHelper", "数据库升级失败: " + e.getMessage());
        }
    }

    // 注册用户
    public synchronized boolean registerUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Log.e("registerUser", "用户名或密码为空");
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PASSWORD, password); // 实际应用中应加密存储
            long rowId = db.insert(TABLE_USERS, null, values);
            return rowId != -1;
        } catch (SQLiteConstraintException e) {
            Log.e("registerUser", "用户名已存在: " + username);
            return false;
        } catch (Exception e) {
            Log.e("registerUser", "注册失败: " + e.getMessage());
            return false;
        } finally {
            if (db != null) db.close();
        }
    }

    // 验证登录
    public synchronized boolean loginUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Log.e("loginUser", "用户名或密码为空");
            return false;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                    new String[]{username, password},
                    null, null, null);
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("loginUser", "登录验证失败: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // 检查用户名是否存在
    public synchronized boolean isUsernameExists(String username) {
        if (username.isEmpty()) return false;

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_USER_ID},
                    COLUMN_USERNAME + "=?",
                    new String[]{username},
                    null, null, null);
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e("isUsernameExists", "检查失败: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    // 添加用户123
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password); // 实际应用中应加密

        long result = db.insert("users", null, values);
        db.close();
        return result != -1;
    }

    // 验证用户
    public boolean verifyUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users",
                new String[]{"username", "password"},
                "username=? AND password=?",
                new String[]{username, password},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // 添加用户相关操作
    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_ID, user.getUserId());
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_PASSWORD, user.getPassword());
        values.put(COLUMN_EMAIL, user.getEmail());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public User getUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID, COLUMN_USERNAME, COLUMN_EMAIL},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setUserId(cursor.getString(0));
            user.setUsername(cursor.getString(1));
            user.setEmail(cursor.getString(2));
            cursor.close();
            db.close();
            return user;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    /*public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    } */

    /*@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 当数据库需要升级时（版本号增加），执行此方法
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        onCreate(db);
    } */


    public long addBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // 先检查表结构
            Cursor tableInfo = db.rawQuery("PRAGMA table_info(books)", null);
            if (tableInfo != null) {
                List<String> columns = new ArrayList<>();
                while (tableInfo.moveToNext()) {
                    columns.add(tableInfo.getString(tableInfo.getColumnIndexOrThrow("name")));
                }
                tableInfo.close();
                Log.d("DatabaseHelper", "books表现有列: " + columns.toString());
            }

            ContentValues values = new ContentValues();
            values.put("title", book.getTitle());
            values.put("price", book.getPrice());
            values.put("latitude", book.getLatitude());
            values.put("longitude", book.getLongitude());
            values.put("location_name", book.getLocationName());
            values.put("seller_id", book.getSellerId());

            // 检查description列是否存在
            if (book.getDescription() != null) {
                values.put("description", book.getDescription());
            }

            if (book.getSellerContact() != null) {
                values.put("seller_contact", book.getSellerContact());
            }

            Log.d("DatabaseHelper", "准备插入数据: " + values.toString());

            long result = db.insert("books", null, values);
            Log.d("DatabaseHelper", "插入结果: " + result);
            return result;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "添加书籍失败", e);

            // 如果插入失败，尝试重建表
            try {
                Log.w("DatabaseHelper", "尝试重建books表...");
                db.execSQL("DROP TABLE IF EXISTS books");
                db.execSQL(CREATE_BOOK_TABLE);

                // 重新插入
                ContentValues values = new ContentValues();
                values.put("title", book.getTitle());
                values.put("price", book.getPrice());
                values.put("latitude", book.getLatitude());
                values.put("longitude", book.getLongitude());
                values.put("location_name", book.getLocationName());
                values.put("seller_id", book.getSellerId());
                values.put("description", book.getDescription());
                values.put("seller_contact", book.getSellerContact());

                return db.insert("books", null, values);
            } catch (Exception ex) {
                Log.e("DatabaseHelper", "重建表也失败", ex);
                return -1;
            }
        } finally {
            db.close();
        }
    }

    public void clearTestData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // 删除所有测试数据（根据seller_id识别测试数据）
            db.delete(TABLE_BOOKS, COLUMN_SELLER_ID + " LIKE ?", new String[]{"test_seller_%"});
            Log.d("Database", "已清空测试数据");
        } finally {
            db.close();
        }
    }

    // 在 DatabaseHelper.java 中添加
    public void clearAllBooks() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_BOOKS, null, null); // 删除所有记录
            Log.d("Database", "已清空所有书籍数据");
        } finally {
            db.close();
        }
    }

    public Book getBookById(int bookId) {
        Book book = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOOKS,
                new String[]{COLUMN_BOOK_ID, COLUMN_TITLE, COLUMN_PRICE,
                        COLUMN_LATITUDE, COLUMN_LONGITUDE,
                        COLUMN_LOCATION_NAME, COLUMN_SELLER_ID, COLUMN_DESCRIPTION, "seller_contact"},
                COLUMN_BOOK_ID + "=?",
                new String[]{String.valueOf(bookId)},
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            book = new Book(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELLER_ID))
            );
            book.setBookId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)));
            book.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));

            int contactIndex = cursor.getColumnIndex("seller_contact");
            if (contactIndex >= 0) {
                book.setSellerContact(cursor.getString(contactIndex));
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();

        return book;
    }

    // 在 DatabaseHelper 类中添加以下方法
    public List<Book> searchBooks(String titleKeyword, String locationKeyword) {
        List<Book> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // 构建查询条件（模糊匹配）
            StringBuilder whereClause = new StringBuilder();
            List<String> whereArgs = new ArrayList<>();

            if (titleKeyword != null && !titleKeyword.isEmpty()) {
                whereClause.append(COLUMN_TITLE).append(" LIKE ?");
                whereArgs.add("%" + titleKeyword + "%");
            }

            if (locationKeyword != null && !locationKeyword.isEmpty()) {
                if (whereClause.length() > 0) {
                    whereClause.append(" AND ");
                }
                whereClause.append(COLUMN_LOCATION_NAME).append(" LIKE ?");
                whereArgs.add("%" + locationKeyword + "%");
            }

            // 执行查询
            cursor = db.query(
                    TABLE_BOOKS,
                    null,
                    whereClause.length() > 0 ? whereClause.toString() : null,
                    whereArgs.isEmpty() ? null : whereArgs.toArray(new String[0]),
                    null, null, null
            );

            // 解析结果
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
                    results.add(book);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "搜索书籍失败: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return results;
    }

    // 在DatabaseHelper类中添加以下方法
    /*public List<Book> getBooksBySellerId(String sellerId) {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // 查询指定卖家ID的书籍
            cursor = db.query(
                    TABLE_BOOKS,
                    null,
                    COLUMN_SELLER_ID + " = ?", // 查询条件
                    new String[]{sellerId},    // 查询参数
                    null, null, null
            );

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
    } */

    public List<Book> getBooksBySellerId(String sellerId) {
        List<Book> bookList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        Log.d("DatabaseHelper", "查询卖家ID为: " + sellerId + " 的书籍");

        try {
            // 使用更简单的查询方式
            String query = "SELECT * FROM " + TABLE_BOOKS +
                    " WHERE " + COLUMN_SELLER_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{sellerId});

            Log.d("DatabaseHelper", "查询结果数量: " + (cursor != null ? cursor.getCount() : 0));

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        // 创建Book对象 - 使用无参构造函数
                        Book book = new Book();

                        // 手动读取每个字段
                        int bookIdIndex = cursor.getColumnIndex(COLUMN_BOOK_ID);
                        if (bookIdIndex >= 0) {
                            int bookId = cursor.getInt(bookIdIndex);
                            book.setBookId(bookId);
                            book.setId(bookId);  // 同时设置id字段
                            Log.d("DatabaseHelper", "找到书籍ID: " + bookId);
                        }

                        int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                        if (titleIndex >= 0) {
                            String title = cursor.getString(titleIndex);
                            book.setTitle(title);
                            Log.d("DatabaseHelper", "书籍标题: " + title);
                        }

                        int priceIndex = cursor.getColumnIndex(COLUMN_PRICE);
                        if (priceIndex >= 0) {
                            double price = cursor.getDouble(priceIndex);
                            book.setPrice(price);
                        }

                        int descIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION);
                        if (descIndex >= 0) {
                            String description = cursor.getString(descIndex);
                            book.setDescription(description);
                        }

                        int sellerIndex = cursor.getColumnIndex(COLUMN_SELLER_ID);
                        if (sellerIndex >= 0) {
                            book.setSellerId(cursor.getString(sellerIndex));
                        }

                        int latIndex = cursor.getColumnIndex(COLUMN_LATITUDE);
                        if (latIndex >= 0) {
                            book.setLatitude(cursor.getDouble(latIndex));
                        }

                        int lngIndex = cursor.getColumnIndex(COLUMN_LONGITUDE);
                        if (lngIndex >= 0) {
                            book.setLongitude(cursor.getDouble(lngIndex));
                        }

                        int locIndex = cursor.getColumnIndex(COLUMN_LOCATION_NAME);
                        if (locIndex >= 0) {
                            book.setLocationName(cursor.getString(locIndex));
                        }

                        int contactIndex = cursor.getColumnIndex("seller_contact");
                        if (contactIndex >= 0) {
                            book.setSellerContact(cursor.getString(contactIndex));
                        }

                        bookList.add(book);
                        Log.d("DatabaseHelper", "成功添加书籍到列表");

                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "解析书籍数据失败", e);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "查询用户书籍失败: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        Log.d("DatabaseHelper", "总共找到 " + bookList.size() + " 本书籍");
        return bookList;
    }

    public List<Book> getBrowseHistory(String userId) {
        // 实现查询逻辑
        return new ArrayList<>();
    }

    // 添加调试方法：检查表结构
    public void debugTableStructure() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            // 检查books表结构
            cursor = db.rawQuery("PRAGMA table_info(books)", null);
            Log.d("DatabaseHelper", "=== Books表结构 ===");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String columnType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    Log.d("DatabaseHelper", "列: " + columnName + " 类型: " + columnType);
                } while (cursor.moveToNext());
            }

            // 检查users表结构
            if (cursor != null) cursor.close();
            cursor = db.rawQuery("PRAGMA table_info(users)", null);
            Log.d("DatabaseHelper", "=== Users表结构 ===");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String columnType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                    Log.d("DatabaseHelper", "列: " + columnName + " 类型: " + columnType);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            Log.e("DatabaseHelper", "调试表结构失败", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
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