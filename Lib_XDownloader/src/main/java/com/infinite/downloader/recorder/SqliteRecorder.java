package com.infinite.downloader.recorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;

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

    private static final int DB_VERSION = 2;
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
    private static final String COL_START_TIME = "start_time";
    private static final String COL_FINISH_TIME = "finish_time";

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
            COL_START_TIME,
            COL_FINISH_TIME,
    };

    private static final String SQL_DROP_RECORD_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";

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
                    COL_FILE_NAME + " TEXT," +
                    COL_START_TIME + " INTEGER," +
                    COL_FINISH_TIME + " INTEGER" +
                    ");";

    private static final String SQL_CREATE_URL_MD5_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + URL_MD5_INDEX_NAME
                    + " ON " + TABLE_NAME + " (" + COL_URL_MD5 + ");";

    private static final String SQL_VERSION2_UPGRADE_TABLE_ADD_START_TIME =
            "ALTER TABLE " + TABLE_NAME
                    + " ADD COLUMN " + COL_START_TIME + " INTEGER;";

    private static final String SQL_VERSION2_UPGRADE_TABLE_ADD_FINISH_TIME =
            "ALTER TABLE " + TABLE_NAME
                    + " ADD COLUMN " + COL_FINISH_TIME + " INTEGER;";

    private static final String SQL_VERSION2_UPGRADE_SET_START_TIME =
            "UPDATE " + TABLE_NAME
                    + " SET " + COL_START_TIME + " = " + System.currentTimeMillis()
                    + " WHERE " + COL_FILE_LENGTH + " = " + COL_COMPLETED_LENGTH + ";";

    private static final String SQL_VERSION2_UPGRADE_SET_FINISH_TIME =
            "UPDATE " + TABLE_NAME
                    + " SET " + COL_FINISH_TIME + " = " + System.currentTimeMillis()
                    + " WHERE " + COL_FILE_LENGTH + " = " + COL_COMPLETED_LENGTH + ";";


    private Context context;

    public SqliteRecorder(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DLogger.e("SQLiteDataSaver onCreate");
        recreateDbTables(db);
    }

    private void recreateDbTables(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            DbUtils.executeSQL(db, SQL_CREATE_RECORD_TABLE);
            DbUtils.executeSQL(db, SQL_CREATE_URL_MD5_INDEX);
            db.setTransactionSuccessful();
        } catch (SQLiteException var13) {
            var13.printStackTrace();
            DLogger.e(var13.getMessage());
            DbUtils.deleteDbFile(context, DB_NAME);
            DbUtils.executeSQL(db, SQL_CREATE_RECORD_TABLE);
            DbUtils.executeSQL(db, SQL_CREATE_URL_MD5_INDEX);
        } catch (Throwable throwable) {
            DLogger.e(throwable.getMessage());
        } finally {
            try {
                if (db != null) {
                    db.endTransaction();
                }
            } catch (Exception e) {
                DLogger.e(e.getMessage());
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DLogger.d("SQLiteDataSaver onUpgrade,oldVersion:" + oldVersion + ",newVersion:" + newVersion);
        long startTime = SystemClock.elapsedRealtime();
        if (oldVersion == 1 && newVersion == 2) {
            try {
                DbUtils.executeSQL(db, SQL_VERSION2_UPGRADE_TABLE_ADD_START_TIME);
                DbUtils.executeSQL(db, SQL_VERSION2_UPGRADE_TABLE_ADD_FINISH_TIME);
                DbUtils.executeSQL(db, SQL_VERSION2_UPGRADE_SET_START_TIME);
                DbUtils.executeSQL(db, SQL_VERSION2_UPGRADE_SET_FINISH_TIME);
            } catch (SQLException e) {
                e.printStackTrace();
                DLogger.e("onUpgrade exception:" + e.getMessage());
                DLogger.e("delete db tables and recreate db tables!!!");
                try {
                    recoveryData(db);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    DLogger.e("recoveryData old data exception!!!，delete table");
                    db.execSQL(SQL_DROP_RECORD_TABLE);
                    recreateDbTables(db);
                }
            }
        }
        DLogger.d("SQLiteDataSaver onUpgrade finish,cost time:" + (SystemClock.elapsedRealtime() - startTime));
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DLogger.e("SQLiteDataSaver onDowngrade,oldVersion:" + oldVersion + ",newVersion:" + newVersion);
        try {
            recoveryData(db);
        } catch (Exception ex) {
            ex.printStackTrace();
            DLogger.e("recoveryData old data exception!!!，delete table");
            db.execSQL(SQL_DROP_RECORD_TABLE);
            recreateDbTables(db);
        }
    }

    @Override
    public FileInfo get(String urlMd5) {
//        long start = System.currentTimeMillis();
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
//        DLogger.d("query item finish，result exist?" + (fileInfo != null)
//                + ",cost time：" + (System.currentTimeMillis() - start));
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
    public int shrink() {
        List<FileInfo> infoList = query(0);
        int count = infoList != null ? infoList.size() : 0;
        DLogger.d("shrink download record,all size is:" + count);
        if (count > 0) {
            List<FileInfo> deleteList = new ArrayList<>(16);
            for (FileInfo info : infoList) {
                if (info != null && info.recordInvalid()) {
                    deleteList.add(info);
                }
            }
            DLogger.d("need shrink size is:" + deleteList.size());
            if (deleteList.size() > 0) {
                return deleteByList(deleteList);
            }
        }
        return 0;
    }

    private int deleteByList(List<FileInfo> deleteList) {
        long start = SystemClock.elapsedRealtime();
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
        DLogger.d("delete item finish，cost：" + (SystemClock.elapsedRealtime() - start)
                + ",delete count：" + count);
        return count;
    }

    @Override
    public long put(String urlMd5, FileInfo fileInfo) {
//        long start = System.currentTimeMillis();
        long result = 0;
        boolean exist = false;
        if (fileInfo != null) {
            FileInfo info = get(urlMd5);
            exist = info != null;
            if (exist) {
                result = getWritableDatabase().update(TABLE_NAME, convertColumns(fileInfo),
                        COL_URL_MD5 + "=?", new String[]{info.getUrlMd5()});
            } else {
                result = getWritableDatabase().insert(TABLE_NAME,
                        null, convertColumns(fileInfo));
            }
        }
//        DLogger.d((exist ? "update" : "insert") + " item finish,"
//                + (exist ? "affected rows:" : "record id") + ":" + result +
//                ",cost time：" + (System.currentTimeMillis() - start));
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
    public int deleteList(List<FileInfo> fileInfoList) {
        if (fileInfoList != null && fileInfoList.size() > 0) {
            return deleteByList(fileInfoList);
        }
        return 0;
    }

    @Override
    public List<FileInfo> queryByFinishTime(long minTimestamp, long maxTimestamp) {
        long start = SystemClock.elapsedRealtime();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME, ALL_COLUMNS,
                COL_FINISH_TIME + " >= ? AND " + COL_FINISH_TIME + " <= ?",
                new String[]{String.valueOf(minTimestamp), String.valueOf(maxTimestamp)},
                null, null,
                null, null);
        List<FileInfo> list = new ArrayList<>(32);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(convertFileInfo(cursor));
            }
            cursor.close();
        }
        if (DLogger.isDebugEnable()) {
            DLogger.d("queryByFinishTime:" + minTimestamp + " to " + maxTimestamp
                    + " item finish,count:" + list.size()
                    + ",cost time:" + (SystemClock.elapsedRealtime() - start));
        }
        return list;
    }

    @Override
    public List<FileInfo> queryByStartTime(long minTimestamp, long maxTimestamp) {
        long start = SystemClock.elapsedRealtime();
        Cursor cursor = getReadableDatabase().query(TABLE_NAME, ALL_COLUMNS,
                COL_START_TIME + " >= ? AND " + COL_START_TIME + " <= ?",
                new String[]{String.valueOf(minTimestamp), String.valueOf(maxTimestamp)},
                null, null,
                null, null);
        List<FileInfo> list = new ArrayList<>(32);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(convertFileInfo(cursor));
            }
            cursor.close();
        }
        if (DLogger.isDebugEnable()) {
            DLogger.d("queryByStartTime:" + minTimestamp + " to " + maxTimestamp
                    + " item finish,count:" + list.size()
                    + ",cost time:" + (SystemClock.elapsedRealtime() - start));
        }
        return list;
    }

    @Override
    public void release() {
        close();
    }

    private void recoveryData(SQLiteDatabase db) {
        int count = 0;
        List<FileInfo> oldDataList = queryOldData(db);
        count = oldDataList.size();
        DLogger.e("queryOldData,count:" + count);

        db.execSQL(SQL_DROP_RECORD_TABLE);
        DLogger.e("drop table sql " + SQL_DROP_RECORD_TABLE);
        recreateDbTables(db);
        DLogger.e("recreateDbTables");

        count = restoreOldData(db, oldDataList);
        DLogger.e("restoreOldData cost time,count:" + count);
    }

    @NonNull
    private List<FileInfo> queryOldData(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        List<FileInfo> dataList = new ArrayList<>(64);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                dataList.add(convertOldFileInfo(cursor));
            }
            cursor.close();
        }
        return dataList;
    }

    private int restoreOldData(SQLiteDatabase db, List<FileInfo> dataList) {
        int count = 0;
        if (dataList != null && dataList.size() > 0) {
            StringBuilder sql = new StringBuilder(100);
            sql.append("INSERT INTO ").append(TABLE_NAME).append(" (");
            sql.append(COL_ID).append(",");
            sql.append(COL_URL_MD5).append(",");
            sql.append(COL_REQUEST_URL).append(",");
            sql.append(COL_DOWNLOAD_URL).append(",");
            sql.append(COL_FILE_LENGTH).append(",");
            sql.append(COL_FILE_MD5).append(",");
            sql.append(COL_SAVE_PATH).append(",");
            sql.append(COL_CONTENT_TYPE).append(",");
            sql.append(COL_COMPLETED_LENGTH).append(",");
            sql.append(COL_SUPPORT_RANGE).append(",");
            sql.append(COL_COST_TIME).append(",");
            sql.append(COL_FILE_NAME).append(")");
            sql.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?,?);");
            db.beginTransaction();
            if (DLogger.isDebugEnable()) {
                DLogger.e("restoreOldData sql is:");
                DLogger.e(sql.toString());
            }
            SQLiteStatement stat = db.compileStatement(sql.toString());
            for (FileInfo info : dataList) {
                if (info != null) {
                    //这里index是必须是从1开始
                    stat.bindLong(1, info.getId());
                    DbUtils.bindMaybeNull(stat, info.getUrlMd5(), 2);
                    DbUtils.bindMaybeNull(stat, info.getRequestUrl(), 3);
                    DbUtils.bindMaybeNull(stat, info.getDownloadUrl(), 4);
                    stat.bindLong(5, info.getFileSize());
                    DbUtils.bindMaybeNull(stat, info.getFileMd5(), 6);
                    DbUtils.bindMaybeNull(stat, info.getSaveDirPath(), 7);
                    DbUtils.bindMaybeNull(stat, info.getContentType(), 8);
                    stat.bindLong(9, info.getCurrentSize());
                    stat.bindLong(10, info.isSupportRange() ? 1 : 0);
                    stat.bindLong(11, info.getCostTime());
                    DbUtils.bindMaybeNull(stat, info.getFileName(), 12);
                    if (stat.executeInsert() > -1) {
                        count++;
                    }
                    stat.clearBindings();
                }
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return count;
    }

    private ContentValues convertColumns(FileInfo fileInfo) {
        ContentValues values = new ContentValues(16);
        values.put(COL_URL_MD5, fileInfo.getUrlMd5());
        values.put(COL_REQUEST_URL, fileInfo.getRequestUrl());
        values.put(COL_DOWNLOAD_URL, fileInfo.getDownloadUrl());
        values.put(COL_FILE_LENGTH, fileInfo.getFileSize());
        values.put(COL_FILE_MD5, fileInfo.getFileMd5());
        values.put(COL_COMPLETED_LENGTH, fileInfo.getCurrentSize());
        values.put(COL_SAVE_PATH, fileInfo.getSaveDirPath());
        values.put(COL_CONTENT_TYPE, fileInfo.getContentType());
        values.put(COL_SUPPORT_RANGE, fileInfo.isSupportRange() ? 1 : 0);
        values.put(COL_COST_TIME, fileInfo.getCostTime());
        values.put(COL_FILE_NAME, fileInfo.getFileName());
        values.put(COL_START_TIME, fileInfo.getStartTime());
        values.put(COL_FINISH_TIME, fileInfo.getFinishTime());
        return values;
    }

    /**
     * 注意：这里的顺序必须要与{@link #ALL_COLUMNS}保持一致
     *
     * @param cursor result set cursor
     * @return FileInfo
     */
    private FileInfo convertFileInfo(Cursor cursor) {
        FileInfo fileInfo = convertOldFileInfo(cursor);

        long startTime = cursor.getLong(12);
        long finishTime = cursor.getLong(13);
        fileInfo.setStartTime(startTime);
        fileInfo.setFinishTime(finishTime);
        return fileInfo;
    }

    private FileInfo convertOldFileInfo(Cursor cursor) {
        long id = cursor.getLong(0);
        String urlMd5 = cursor.getString(1);
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
        fileInfo.setUrlMd5(urlMd5);
        fileInfo.setSaveDirPath(savePath);
        fileInfo.setRequestUrl(requestUrl);
        fileInfo.setDownloadUrl(downloadUrl);
        fileInfo.setFileSize(fileLength);
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setCurrentSize(completedLength);
        fileInfo.setContentType(contentType);
        fileInfo.setSupportRange(supportRange == 1);
        fileInfo.setCostTime(costTime);
        fileInfo.setFileName(fileName);
        return fileInfo;
    }
}
