## XDownloader

一个简单的、支持多任务、断点下载、可控的下载库，无任何三方依赖，使用SQLite存储下载记录，使用相当简单。

注意：不支持单个下载任务的多线程下载


* 打开Logger
```
DLogger.enable()
```

## 使用XDownload下载文件
XDownload对象可以统一管理多个下载任务，请根据自己的需要传入Config，Config为空则使用默认的配置
建议创建一个单例类管理XDownload对象。

注意：默认的下载存储路径为/storage/emulated/0/Android/data/{package}/cache/xdownload/，package为你的app包名。
如果你需要在其他位置创建下载存储文件夹，务必保证在下载之前已经获取到读写文件夹运行时权限!

参考示例App配置，可以在Application中设置一个全局的XDownload，你也可以在你需要的地方创建XDownload对象，使用完之后释放即可
```
public class DownloadApp extends Application {
    private static XDownload xDownload;

    @Override
    public void onCreate() {
        super.onCreate();
        Config config = Config.defaultConfig(this);
        //这里务必保证文件夹已经获取到运行时读写权限
        File saveDir = new File(getExternalCacheDir().getAbsolutePath(), "cache_dir");
        if (!saveDir.isDirectory() || !saveDir.exists()) {
            saveDir.mkdir();
        }
        config.setSaveDirPath(saveDir.getAbsolutePath());
        DLogger.enable();
        xDownload = new XDownload(this, config);
    }

    public static XDownload getDownload() {
        return xDownload;
    }
}
```
* 添加一个下载任务，如果任务已经存在，则不再添加，返回当前url对应的task对象
```
 xDownload.addTask(url, allDownloadListener);
```

* 根据下载url获取一个下载任务，如果任务不存在将返回空
```
xDownload.getTask(String url)
```
* 根据下载url取消下载任务
```
xDownload.removeTask(String url)
```

* 判断文件是否已下载完成

```
xDownload.getFile  //返回为空则文件未下载完成
```

* 下载状态监听DownloadListener#onDownloadStatus
```
public class DownloadStatus {
    public static final int ERROR = 0;  //下载出错
    public static final int PREPARE = 1; //下载准备中
    public static final int PREPARED = 2;//下载已准备好
    public static final int STARTED = 3;//已经开始下载
    public static final int DOWNLOADING = 4;//正在下载
    public static final int FINISH = 5;//下载完成
    public static final int STOP = 6;//下载停止
```

* 关闭XDownload，会停止所有下载任务
```
xDownload.shutdown()
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

注意：默认使用TotalSizeLruDiskUsage，限制大小512M。