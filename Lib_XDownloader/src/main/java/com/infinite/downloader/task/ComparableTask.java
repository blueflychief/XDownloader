package com.infinite.downloader.task;

import androidx.annotation.NonNull;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 2019-08-28 - 18:03
 * Description: Class description
 */
public abstract class ComparableTask implements Runnable, Comparable<ComparableTask> {
    protected volatile boolean stopped = false;
    protected int priority = 5;

    public ComparableTask() {
    }

    public ComparableTask(int priority) {
        this.priority = priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void stop() {
        stopped = true;
    }

    protected boolean isStopped() {
        boolean isStopped = stopped || Thread.currentThread().isInterrupted();
//        DLogger.d("task is shutdown?" + isStopped + ",thread name:" + Thread.currentThread().getName());
        return isStopped;
    }

    public boolean dead() {
        return stopped;
    }

    @Override
    public int compareTo(@NonNull ComparableTask o) {
        if (this == o) {
            return 0;
        }
        if (this.priority > o.priority) {
            return -1;
        } else if (this.priority < o.priority) {
            return 1;
        } else {
            return 0;
        }
    }
}
