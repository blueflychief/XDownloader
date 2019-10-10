package com.infinite.downloader.writer;

import com.infinite.downloader.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * Email: 690797861@qq.com
 * Author: Infinite
 * Date: 10/9/2019 - 21:42
 * Description: class description
 */
public class FileWriter implements Writer {
    private static final String FILE_WRITE_READ = "rw";
    private String filePath;
    private RandomAccessFile raf;
    private MappedByteBuffer mbb;
    private long currentSize;

    public FileWriter(String path, long currentSize) throws IOException {
        this.filePath = path;
        this.currentSize = currentSize;
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        if (file.exists()) {
            raf = new RandomAccessFile(file, FILE_WRITE_READ);
        } else {
            throw new FileNotFoundException();
        }
    }

    @Override
    public long saveFile(byte[] buffer, int length) throws IOException {
        mbb = raf.getChannel().map(READ_WRITE, currentSize, length);
        mbb.put(buffer, 0, length);
        currentSize += length;
        Logger.d("save buffer to file,current size:" + currentSize);
        return currentSize;
    }

    @Override
    public void close() {
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
