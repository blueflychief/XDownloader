## XDownloader

一个简单的、支持多任务、断点下载、可控的下载库，无任何三方依赖，使用SQLite存储下载记录，使用相当简单。

注意：不支持单个下载任务的多线程下载


* 打开Logger
```
DLogger.enable()
```

### 一般使用方法

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

## 使用XDownload下载文件
XDownload可以统一管理所有下载任务，使用XDownload必须先初始化，请根据自己的需要传入Config，Config为空则使用默认的配置

注意：默认的下载存储路径为/storage/emulated/0/Android/data/{package}/cache/xdownload/，package为你的app包名
```
XDownload.init(Context context)
//或者
XDownload.init(Context context, Config config)
```
示例App配置
```
public class DownloadApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Config config = Config.defaultConfig(this);
        config.setDiskUsage(new TotalSizeLruDiskUsage(20 * 1024 * 1024));//限制20M
        XDownload.get().init(this, config);
    }
}
```
* 添加一个下载任务，如果任务已经存在，则不再添加，返回当前url对应的task对象
```
 XDownload.get().addTask(url, allDownloadListener);
```

* 根据下载url获取一个下载任务，如果任务不存在将返回空
```
XDownload.get().getTask(String url)
```
* 根据下载url取消下载任务
```
XDownload.get().removeTask(String url)
```
* 关闭XDownload，会停止所有下载任务
```
XDownload.get().shutdown()
```

## 设置文件删除策略
可以在Config中设置文件删除策略

- 根据下载文件夹大小

    TotalSizeLruDiskUsage

    当文件夹大小超过给定的值后，按照下载完成时间先后，删除几个文件


- 根据文件数量

    TotalCountLruDiskUsage

    根据下载文件夹中的文件数量，当文件数量超过给定的值后，按照下载完成时间先后，删除几个文件

- 无任何限制

    UnlimitedDiskUsage

注意：默认使用TotalSizeLruDiskUsage，限制大小1G。