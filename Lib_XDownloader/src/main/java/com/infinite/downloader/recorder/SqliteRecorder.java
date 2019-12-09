package com.infinite.downloader.recorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import com.infinite.downloader.config.FileInfo;
import com.infinite.downloader.utils.DLogger;
import com.infinite.downloader.utils.DbUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-10-09 - 15:23
 * Description: Class description
 */
public class SqliteRecorder extends SQLiteOpenHelper implements Recorder {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "infinite_xdownload_record.db";
    private static final String TABLE_NAME = "tb_record";
    private static final String URL_MD5_INDEX_NAME = "idx_url_md5";

    private static final String COL_ID = "_id";
    private static final String COL_URL_MD5 = "url_md5";
    private static final String COL_REQUEST_URL = "request_url";
    private static final String COL_DOWNLOAD_URL = "download_url";
    private static final String COL_FILE_LENGTH = "file_length";
    private static final String COL_FILE_MD5 = "file_md5";
    private static final String COL_COMPLETED_LENGTH = "completed_length";
    private static final String COL_SAVE_PATH = "save_path";
    private static final String COL_CONTENT_TYPE = "content_type";
    private static final String COL_SUPPORT_RANGE = "support_range";
    private static final String COL_COST_TIME = "cost_time";
    private static final String COL_FILE_NAME = "file_name";

    private static final String[] ALL_COLUMNS = new String[]{
            COL_ID,
            COL_URL_MD5,
            COL_REQUEST_URL,
            COL_DOWNLOAD_URL,
            COL_FILE_LENGTH,
            COL_FILE_MD5,
            COL_SAVE_PATH,
            COL_CONTENT_TYPE,
            COL_COMPLETED_LENGTH,
            COL_SUPPORT_RANGE,
            COL_COST_TIME,
            COL_FILE_NAME,
    };

    private static final String SQL_CREATE_RECORD_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    COL_URL_MD5 + " TEXT NOT NULL," +
                    COL_REQUEST_URL + " TEXT NOT NULL," +
                    COL_DOWNLOAD_URL + " TEXT NOT NULL," +
                    COL_FILE_LENGTH + " INTEGER," +
                    COL_FILE_MD5 + " TEXT," +
                    COL_SAVE_PATH + " TEXT," +
                    COL_CONTENT_TYPE + " TEXT," +
                    COL_COMPLETED_LENGTH + " INTEGER," +
                    COL_SUPPORT_RANGE + " INTEGER," +
                    COL_COST_TIME + " INTEGER," +
                    COL_FILE_NAME + " TEXT" +
                    ");";

    private static final String SQL_CREATE_URL_MD5_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + URL_MD5_INDEX_NAME
                    + " ON " + TABLE_NAME + " (" + COL_URL_MD5 + ")";

    private Context context;

    public SqliteRecorder(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DLogger.d("SQLiteDataSaver onCreate");
        try {
            db.beginTransaction();
            DbUtils.executeSQL(db, SQL_CREATE_RECORD_TABLE);
            DbUtils.executeSQL(db, SQL_CREATE_URL_MD5_INDEX);
            db.setTransactionSuccessful();
        } catch (SQLiteDatabaseCorruptException var13) {
            DbUtils.deleteDbFile(context, DB_NAME);
            DbUtils.executeSQL(db, SQL_CREATE_RECORD_TABLE);
            DbUtils.executeSQL(db, SQL_CREATE_URL_MD5_INDEX);
        } catch (Throwable throwable) {
            DLogger.d(throwable.getMessage());
        } finally {
            try {
                if (db != null) {
                    db.endTransaction();
                }
            } catch (Exception e) {
                DLogger.d(e.getMessage());
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DLogger.d("SQLiteDataSaver onUpgrade");
    }

    @Override
    public FileInfo get(String urlMd5) {
        long start = System.currentTimeMillis();
        FileInfo fileInfo = null;
        if (!TextUtils.isEmpty(urlMd5)) {
            Cursor cursor = getReadableDatabase().query(TABLE_NAME, ALL_COLUMNS,
                    COL_URL_MD5 + "=?", new String[]{urlMd5},
                    null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    fileInfo = convertFileInfo(cursor);
                }
                cursor.close();
            }
        }
        DLogger.d("query item finish，result exist?" + (fileInfo != null)
                + ",cost time：" + (System.currentTimeMillis() - start));
        return fileInfo;
    }

    @Override
    public List<FileInfo> query(int count) {
        long start = System.currentTimeMillis();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME, ALL_COLUMNS, null,
                null, null, null,
                null, count > 0 ? String.valueOf(count) : null);
        List<FileInfo> list = new ArrayList<>(16);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(convertFileInfo(cursor));
            }
            cursor.close();
        }
        DLogger.d("query all item finish,count:" + list.size()
                + ",cost time:" + (System.currentTimeMillis() - start));
        return list;
    }

    @Override
    public void shrink() {
        List<FileInfo> infoList = query(0);
        if (infoList != null && infoList.size() > 0) {
            List<FileInfo> deleteList = new ArrayList<>(16);
            for (FileInfo info : infoList) {
                if (info != null && info.recordInvalid()) {
                    deleteList.add(info);
                }
            }
            if (deleteList.size() > 0) {
                long start = System.currentTimeMillis();
                int count = 0;
                String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + COL_URL_MD5 + "=?";
                SQLiteDatabase db = getWritableDatabase();
                try {
                    SQLiteStatement stat = db.compileStatement(sql);
                    db.beginTransaction();
                    for (FileInfo entity : deleteList) {
                        if (entity != null) {
                            stat.bindString(1, entity.getUrlMd5());
                            stat.execute();
                            count++;
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    DLogger.e(e.getMessage());
                } finally {
                    DbUtils.endTransaction(db);
                }
                DLogger.d("delete item finish，cost：" + (System.currentTimeMillis() - start)
                        + ",delete count：" + count);
            }
        }
    }

    @Override
    public long put(String urlMd5, FileInfo fileInfo) {
        long start = System.currentTimeMillis();
        long result;
        boolean exist;
        FileInfo info = get(urlMd5);
        exist = info != null;
        if (exist) {
            result = getWritableDatabase().update(TABLE_NAME, convertColumns(fileInfo, false),
                    COL_URL_MD5 + "=?", new String[]{info.getUrlMd5()});
        } else {
            result = getWritableDatabase().insert(TABLE_NAME,
                    null, convertColumns(fileInfo, true));
        }
        DLogger.d((exist ? "update" : "insert") + " item finish,"
                + (exist ? "affected rows:" : "record id") + ":" + result +
                ",cost time：" + (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public int delete(String urlMd5) {
        long start = System.currentTimeMillis();
        int result = 0;
        if (!TextUtils.isEmpty(urlMd5)) {
            result = getWritableDatabase().delete(TABLE_NAME,
                    COL_URL_MD5 + "=?", new String[]{urlMd5});
        }
        DLogger.d("delete item finish,count:" + result
                + ",cost time:" + (System.currentTimeMillis() - start));
        return result;
    }


    @Override
    public void release() {
        close();
    }

    private ContentValues convertColumns(FileInfo fileInfo, boolean updateUrlMd5) {
        ContentValues values = new ContentValues(16);
        if (updateUrlMd5) {
            values.put(COL_URL_MD5, fileInfo.getUrlMd5());
        }
        values.put(COL_REQUEST_URL, fileInfo.getRequestUrl());
        values.put(COL_DOWNLOAD_URL, fileInfo.getDownloadUrl());
        values.put(COL_FILE_LENGTH, fileInfo.getFileSize());
        values.put(COL_FILE_MD5, fileInfo.getFileMd5());
        values.put(COL_COMPLETED_LENGTH, fileInfo.getCurrentSize());
        values.put(COL_SAVE_PATH, fileInfo.getSavePath());
        values.put(COL_CONTENT_TYPE, fileInfo.getContentType());
        values.put(COL_SUPPORT_RANGE, fileInfo.isSupportRange() ? 1 : 0);
        values.put(COL_COST_TIME, fileInfo.getCostTime());
        values.put(COL_FILE_NAME, fileInfo.getFileName());
        return values;
    }

    /**
     * 注意：这里的顺序必须要与{@link #ALL_COLUMNS}保持一致
     *
     * @param cursor result set cursor
     * @return FileInfo
     */
    private FileInfo convertFileInfo(Cursor cursor) {
        long id = cursor.getLong(0);
        String requestUrl = cursor.getString(2);
        String downloadUrl = cursor.getString(3);
        long fileLength = cursor.getLong(4);
        String fileMd5 = cursor.getString(5);
        String savePath = cursor.getString(6);
        String contentType = cursor.getString(7);
        long completedLength = cursor.getLong(8);
        int supportRange = cursor.getInt(9);
        long costTime = cursor.getLong(10);
        String fileName = cursor.getString(11);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(id);
        fileInfo.setRequestUrl(requestUrl);
        fileInfo.setDownloadUrl(downloadUrl);
        fileInfo.setFileSize(fileLength);
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setCurrentSize(completedLength);
        fileInfo.setSavePath(savePath);
        fileInfo.setContentType(contentType);
        fileInfo.setSupportRange(supportRange == 1);
        fileInfo.setCostTime(costTime);
        fileInfo.setFileName(fileName);
        return fileInfo;
    }
}
