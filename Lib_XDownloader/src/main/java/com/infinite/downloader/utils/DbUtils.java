package com.infinite.downloader.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import java.io.File;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-10-09 - 15:45
 * Description: Class description
 */
public class DbUtils {
    public static boolean hasTable(String tableName, SQLiteDatabase database) {
        boolean hasTable = false;
        if (tableName == null) {
            return false;
        } else {
            Cursor cursor = null;
            try {
                String sql = "select count(*) as c from sqlite_master " +
                        "where type ='table' and name ='" + tableName.trim() + "' ";
                cursor = database.rawQuery(sql, null);
                if (cursor.moveToNext()) {
                    int var5 = cursor.getInt(0);
                    if (var5 > 0) {
                        hasTable = true;
                    }
                }
            } catch (Exception var9) {
                var9.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return hasTable;
        }
    }

    public static void deleteDbFile(Context context, String dbName) {
        try {
            if (context == null || TextUtils.isEmpty(dbName)) {
                return;
            }
            File dbFile = context.getDatabasePath(dbName);
            if (dbFile != null && dbFile.exists()) {
                dbFile.delete();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void executeSQL(SQLiteDatabase db, String sql) throws SQLException {
        DLogger.d("executeSQL：" + sql);
        db.execSQL(sql);
    }


    public static void endTransaction(SQLiteDatabase db) {
        if (db != null && db.inTransaction()) {
            try {
                db.endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void bindMaybeNull(SQLiteStatement stat, String value, int index) {
        if (!TextUtils.isEmpty(value)) {
            stat.bindString(index, value);
        } else {
            stat.bindNull(index);
        }
    }
}
