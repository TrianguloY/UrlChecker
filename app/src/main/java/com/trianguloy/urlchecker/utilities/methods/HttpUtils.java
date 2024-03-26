package com.trianguloy.urlchecker.utilities.methods;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * HttpUtils class contains teh method related to url.
 */
public class HttpUtils {

    /**
     * GETs an URL and returns the content as a string.
     */
    public static String readFromUrl(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(StreamUtils.CONNECT_TIMEOUT);
        return StreamUtils.inputStream2String(connection.getInputStream());
    }

    /**
     * GETs an URL and streams its lines.
     */
    public static void streamFromUrl(String url, JavaUtils.Consumer<String> consumer) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(StreamUtils.CONNECT_TIMEOUT);
        StreamUtils.consumeLines(connection.getInputStream(), consumer);
    }

    /**
     * POSTs something (a body) to an URL and returns its content as a string.
     */
    public static String performPOST(String url, String body) throws IOException {
        URL urlObject = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) urlObject.openConnection();
        conn.setDoOutput(true);
        conn.setConnectTimeout(StreamUtils.CONNECT_TIMEOUT);
        try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
            wr.write(body);
            wr.flush();
        }
        return StreamUtils.inputStream2String(
                conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        );
    }
}
