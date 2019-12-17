package com.infinite.downloaderapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infinite.downloader.utils.DLogger;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-12-16 - 15:55
 * Description: Class description
 */
public class AsyncTaskActivity extends AppCompatActivity {
    private static final String TAG = "AsyncTaskActivity";
    private AsyncTask<Integer, Integer, Integer> myAsyncTask;
    private TextView tvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async_task);
        tvResult = findViewById(R.id.tvResult);
        findViewById(R.id.btStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAsyncTask = new AsyncTask<Integer, Integer, Integer>() {
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
                            result += i;
                            publishProgress(result);
                            DLogger.d(TAG, "current index:" + i + ",result is " + result);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        return result;
                    }

                    @Override
                    protected void onProgressUpdate(Integer... params) {
                        super.onProgressUpdate(params);
                        DLogger.d(TAG, "onProgressUpdate,params:" + Arrays.toString(params));
                        int result = params != null && params.length > 0 ? params[0] : 0;
                        tvResult.setText("结果：" + result);
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        super.onPostExecute(result);
                        DLogger.d(TAG, "onPostExecute,result:" + result);
                        tvResult.setText("结果：" + result);
                    }

                    @Override
                    protected void onCancelled() {
                        super.onCancelled();
                        DLogger.d(TAG, "onCancelled");
                    }
                };
                myAsyncTask.execute(1);
            }
        });

        findViewById(R.id.btStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myAsyncTask != null) {
                    boolean canceled = myAsyncTask.cancel(true);
                    DLogger.d(TAG, "canceled:" + canceled);
                }
            }
        });

        findViewById(R.id.btGetResult).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myAsyncTask != null
                        && !myAsyncTask.isCancelled()) {
                    getResult();
                }
            }
        });
    }

    private int getResult() {
        int result = 0;
        try {
            result = myAsyncTask.get(1, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            DLogger.e(TAG, "get result time out");
        }
        DLogger.d(TAG, "get result:" + result);
        return result;
    }
}
