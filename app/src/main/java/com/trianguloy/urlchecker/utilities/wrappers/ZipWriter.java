package com.trianguloy.urlchecker.utilities.wrappers;

import static com.trianguloy.urlchecker.utilities.methods.StreamUtils.UTF_8;

import com.trianguloy.urlchecker.utilities.methods.StreamUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class to manage creating zips
 */
public class ZipWriter implements Closeable {

    private final ZipOutputStream zip;

    public ZipWriter(File file, String comment) throws FileNotFoundException {
        file.delete();
        zip = new ZipOutputStream(new FileOutputStream(file));
        zip.setComment(comment);
    }

    public void addStringFile(String name, String content) throws IOException {
        var entry = new ZipEntry(name);
        zip.putNextEntry(entry);
        zip.write(content.getBytes(UTF_8));
    }

    public void addStreamFile(String name, InputStream stream) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        StreamUtils.inputStream2OutputStream(stream, zip);
    }

    @Override
    public void close() throws IOException {
        if (zip != null) zip.close();
    }
}
