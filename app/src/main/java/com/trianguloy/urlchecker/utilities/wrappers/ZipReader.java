package com.trianguloy.urlchecker.utilities.wrappers;

import android.content.Context;
import android.net.Uri;

import com.trianguloy.urlchecker.utilities.methods.AndroidUtils;
import com.trianguloy.urlchecker.utilities.methods.StreamUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/** Utility class to manage loading zips */
public class ZipReader implements Closeable {
    private final ZipFile zip;
    private File cacheFile;

    /** Opens a zip from a uri */
    public ZipReader(Uri uri, Context cntx) throws IOException {
        // copy to temporal file to allow using ZipFile
        cacheFile = new File(cntx.getCacheDir(), "ZipReader");
        try {
            AndroidUtils.copyUri2File(uri, cacheFile, cntx);
            zip = new ZipFile(cacheFile);
        } catch (IOException e) {
            cacheFile.delete();
            cacheFile = null;
            throw e;
        }
    }

    /** Returns the content of file as string */
    public String getFileString(String name) throws IOException {
        var preferences = zip.getEntry(name);
        return preferences != null ? StreamUtils.inputStream2String(zip.getInputStream(preferences)) : null;
    }

    /** Return the content of a file as stream */
    public void getFileStream(String name, OutputStream out) throws IOException {
        try (var in = zip.getInputStream(zip.getEntry(name))) {
            StreamUtils.inputStream2OutputStream(in, out);
        }
    }

    /** Returns the list of files from a particular folder */
    public List<String> fileNames(String folder) {
        var fileNames = new ArrayList<String>();
        var zipEntries = zip.entries();
        while (zipEntries.hasMoreElements()) {
            var name = zipEntries.nextElement().getName();
            if (name.startsWith(folder)) fileNames.add(name);
        }
        return fileNames;
    }

    @Override
    public void close() throws IOException {
        if (zip != null) zip.close();
        if (cacheFile != null) cacheFile.delete();
    }
}
