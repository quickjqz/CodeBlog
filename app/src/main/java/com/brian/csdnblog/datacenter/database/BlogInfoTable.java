package com.brian.csdnblog.datacenter.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.brian.csdnblog.manager.ThreadManager;
import com.brian.csdnblog.model.BlogInfo;
import com.brian.csdnblog.util.LogUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlogInfoTable extends BaseTable<BlogInfo> {
    private static final String TAG = BlogInfoTable.class.getSimpleName();

    // 文章表名称
    public static final String TABLE_NAME = "BlogInfoTable";

    // 文章表字段ID
    private static final String ID = "id";
    public static final String BLOG_ID = "blog_id";
    public static final String TITLE = "title";
    public static final String LINK = "link";
    public static final String BLOGER_ID = "bloger_id";
    public static final String SUMMARY = "summary";
    public static final String LOACAL_PATH = "local_path";
    public static final String DATESTAMP = "datestamp";
    public static final String EXTRA_MSG = "extra_msg";
    public static final String TYPE = "type";


    // 创建帖子表的sql语言
    protected static final String SQL_CREATE_TABLE = "create table if not exists " + TABLE_NAME
            + " ( "
            + ID + " integer primary key autoincrement, "
            + BLOG_ID + " text, "
            + TITLE + " text, "
            + LINK + " text, "
            + BLOGER_ID + " text, "
            + SUMMARY + " text, "
            + LOACAL_PATH + " text, "
            + DATESTAMP + " text, "
            + EXTRA_MSG + " text, "
            + TYPE + " integer "
            + " ) ";

    private static BlogInfoTable mInstance;

    private BlogInfoTable() {
    }

    public static BlogInfoTable getInstance() {
        if (mInstance == null) {
            synchronized (TAG) {
                if (mInstance == null) {
                    mInstance = new BlogInfoTable();
                }
            }
        }
        return mInstance;
    }

    public static void onCreate(SQLiteDatabase db) {
        LogUtil.log("SQL_CREATE_TABLE=" + SQL_CREATE_TABLE);
        db.execSQL(SQL_CREATE_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO 实现升级逻辑
    }

    public void saveAsyc(final BlogInfo info) {
        ThreadManager.getPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                save(info);
            }
        });
    }

    /**
     * 插入新的消息
     */
    public boolean save(BlogInfo info) {
        if (info == null) {
            return false;
        }

        String selection = BLOG_ID + " = ? ";
        String[] selectionArgs = new String[]{info.blogId};

        ContentValues values = toContentValues(info);
        return insertOrUpdate(TABLE_NAME, selection, selectionArgs, values);
    }

    public boolean delete(BlogInfo info) {
        if (info == null) {
            return false;
        }
        return delete(info.blogId);
    }

    /**
     * 根据id删除
     */
    public boolean delete(String blogId) {
        if (TextUtils.isEmpty(blogId)) {
            return false;
        }

        String selection = BLOG_ID + " = ? ";
        String[] selectionArgs = new String[]{blogId};

        return delete(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * 删除指定类型的文章
     */
    public boolean deleteBlogsByType(int type) {
        String selection = TYPE + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(type)};
        return delete(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * 清空当前记录
     */
    public boolean clearTable() {
        return delete(TABLE_NAME, null, null);
    }

    /**
     * 查询指定id的信息
     */
    public BlogInfo query(String blogId) {
        if (TextUtils.isEmpty(blogId)) {
            return null;
        }

        String selection = BLOG_ID + " = ? ";
        String[] selectionArgs = new String[]{blogId};

        return query(TABLE_NAME, selection, selectionArgs);
    }

    /**
     * 查询指定id博主的信息
     */
    public BlogInfo queryByBlogerId(String blogerId) {
        if (TextUtils.isEmpty(blogerId)) {
            return null;
        }

        String selection = BLOGER_ID + " = ? ";
        String[] selectionArgs = new String[]{blogerId};

        return query(TABLE_NAME, selection, selectionArgs);
    }

    public List<BlogInfo> queryList(int type) {
        return queryList(type, 0, 0);
    }

    /**
     * 批量查询消息（按照接收时间倒序）
     */
    public List<BlogInfo> queryList(int type, int startIndex, int num) {
        if (type <= 0 || startIndex < 0) {
            return null;
        }

        String orderBy = DATESTAMP + " desc ";
        String selection = null;
        String[] selectionArgs = null;
//        String selection = TYPE + " = ? ";
//        String[] selectionArgs = new String[]{String.valueOf(type)};

        String limit = null;
        if (num > 0) {
            limit = String.format(Locale.ENGLISH, " %d, %d ", startIndex, num);
        }

        return queryList(TABLE_NAME, selection, selectionArgs, orderBy, limit);
    }

    @Override
    protected ContentValues toContentValues(BlogInfo info) {
        if (info != null) {
            ContentValues values = new ContentValues();
            values.put(BLOG_ID, info.blogId);
            values.put(TITLE, info.title);
            values.put(LINK, info.link);
            values.put(BLOGER_ID, info.blogerID);
            values.put(SUMMARY, info.summary);
            values.put(LOACAL_PATH, info.localPath);
            values.put(DATESTAMP, info.dateStamp);
            values.put(EXTRA_MSG, info.extraMsg);
            values.put(TYPE, info.type);
            return values;
        }
        return null;
    }

    @Override
    protected void readCursor(ArrayList<BlogInfo> list, Cursor cursor) throws JSONException {
        if (cursor != null && cursor.moveToFirst()) {
            BlogInfo blogInfo;
            do {
                blogInfo = new BlogInfo();
                blogInfo.blogId = cursor.getString(cursor.getColumnIndex(BLOG_ID));
                blogInfo.title = cursor.getString(cursor.getColumnIndex(TITLE));
                blogInfo.link = cursor.getString(cursor.getColumnIndex(LINK));
                blogInfo.blogerID = cursor.getString(cursor.getColumnIndex(BLOGER_ID));
                blogInfo.summary = cursor.getString(cursor.getColumnIndex(SUMMARY));
                blogInfo.localPath = cursor.getString(cursor.getColumnIndex(LOACAL_PATH));
                blogInfo.dateStamp = cursor.getString(cursor.getColumnIndex(DATESTAMP));
                blogInfo.extraMsg = cursor.getString(cursor.getColumnIndex(EXTRA_MSG));
                blogInfo.type = cursor.getInt(cursor.getColumnIndex(TYPE));
                list.add(blogInfo);
            } while (cursor.moveToNext());
        }
    }

}
