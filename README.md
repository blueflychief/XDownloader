## Downloader

一个简单的、支持多任务、断点下载、可控的下载库，无任何三方依赖，使用SQLite存储下载记录，使用相当简单。

注意：不支持单个下载任务的多线程下载

### 使用方法

* 启动单任务下载
```
                DownloadTask downloadTask = new DownloadTask(MainActivity.this, Urls.URLS[0]);
                downloadThread = new Thread(downloadTask);
                downloadTask.addDownloadListener(downloadListener);
                downloadThread.start();
```

* 停止单任务下载
```
                downloadThread.interrupt();
```

* 启动多任务下载
```
                executorService = Executors.newFixedThreadPool(10);
                DownloadTask downloadTask;
                for (String url : Urls.URLS) {
                    downloadTask = new DownloadTask(MainActivity.this, url);
                    downloadTask.addDownloadListener(downloadListener);
                    executorService.submit(downloadTask);
                }
```

* 停止多任务下载
```
                executorService.shutdownNow();
```

* 下载回调处理，（下载状态参考类DownloadStatus.class）
```
    private DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onDownloadStatus(int status, @Nullable FileInfo info) {
            if (info != null) {
                tvResult.setText("status:" + status + ",message:" + info.getMessage()
                        + "\nspeed:" + String.format("%.2f", info.getSpeed())
                        + "\ncurrent:" + info.getCurrentSize()
                        + "\ncost time:" + info.getCostTime()
                        + "\nbreakpoint download:" + info.isBreakpointDownload());
            }
        }
    };
```
