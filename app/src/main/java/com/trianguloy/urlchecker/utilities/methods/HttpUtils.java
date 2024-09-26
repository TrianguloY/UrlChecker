package com.trianguloy.urlchecker.utilities.methods;

import static java.util.Collections.emptyMap;

import android.util.Pair;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


/** HttpUtils class contains the method related to url. */
public class HttpUtils {
    public static final int CONNECT_TIMEOUT = 5000;

    /** GETs an URL and returns the content as a string. */
    public static String readFromUrl(String url) throws IOException {
        return readFromUrl(url, emptyMap()).second;
    }

    /** GETs an URL and returns the content as a string. */
    public static Pair<Integer, String> readFromUrl(String url, Map<String, String> headers) throws IOException {
        var conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        for (var header : headers.entrySet()) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }

        return Pair.create(conn.getResponseCode(), StreamUtils.inputStream2String(
                conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        ));
    }

    /** GETs an URL and streams its lines. */
    public static void streamFromUrl(String url, JavaUtils.Consumer<String> consumer) throws IOException {
        var connection = new URL(url).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        StreamUtils.consumeLines(connection.getInputStream(), consumer);
    }

    /** POSTs something (a body) to an URL and returns its content as a string. */
    public static String performPOST(String url, String body) throws IOException {
        // Send POST data request
        var conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        try (var wr = new OutputStreamWriter(conn.getOutputStream())) {
            wr.write(body);
            wr.flush();
        }
        // Get the server response
        return StreamUtils.inputStream2String(
                conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        );
    }

    /** POSTs a form body to an URL and returns its content as a string. */
    public static Pair<Integer, String> performPOST(String url, String body, Map<String, String> headers) throws IOException {
        // Send POST data request
        var conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        for (var header : headers.entrySet()) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }
        try (var wr = new OutputStreamWriter(conn.getOutputStream())) {
            wr.write(body);
            wr.flush();
        }
        // Get the server response
        return Pair.create(conn.getResponseCode(), StreamUtils.inputStream2String(
                conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        ));
    }
}
