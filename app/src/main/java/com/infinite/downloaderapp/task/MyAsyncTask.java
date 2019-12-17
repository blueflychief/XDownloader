package com.infinite.downloaderapp.task;

import android.os.AsyncTask;

import com.infinite.downloader.utils.DLogger;

import java.util.Arrays;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-12-16 - 15:58
 * Description: Class description
 */
public class MyAsyncTask extends AsyncTask<Integer, Integer, Integer> {
    private static final String TAG = MyAsyncTask.class.getSimpleName();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        DLogger.d(TAG, "onPreExecute");
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        DLogger.d(TAG, "doInBackground,params:" + Arrays.toString(params));
        int start = 0;
        if (params != null && params.length > 0) {
            start = params[0];
        }
        String threadName = Thread.currentThread().getName();
        DLogger.d(TAG, "current thread is " + threadName);
        int result = 0;
        for (int i = start; i < 10; i++) {
            if (isCancelled()) {
                break;
            }
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result += i;
            publishProgress(result);
            DLogger.d(TAG, "current index:" + i + ",result is " + result);
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        super.onProgressUpdate(params);
        DLogger.d(TAG, "onProgressUpdate,params:" + Arrays.toString(params));
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        DLogger.d(TAG, "onPostExecute,result:" + result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        DLogger.d(TAG, "onCancelled");
    }
}
