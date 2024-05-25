package com.trianguloy.urlchecker.utilities.methods;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/** HttpUtils class contains the method related to url. */
public class HttpUtils {

    /** GETs an URL and returns the content as a string. */
    public static String readFromUrl(String url) throws IOException {
        var connection = new URL(url).openConnection();
        connection.setConnectTimeout(StreamUtils.CONNECT_TIMEOUT);
        return StreamUtils.inputStream2String(connection.getInputStream());
    }

    /** GETs an URL and streams its lines. */
    public static void streamFromUrl(String url, JavaUtils.Consumer<String> consumer) throws IOException {
        var connection = new URL(url).openConnection();
        connection.setConnectTimeout(StreamUtils.CONNECT_TIMEOUT);
        StreamUtils.consumeLines(connection.getInputStream(), consumer);
    }

    /** POSTs something (a body) to an URL and returns its content as a string. */
    public static String performPOST(String url, String body) throws IOException {
        // Send POST data request
        var conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setDoOutput(true);
        conn.setConnectTimeout(StreamUtils.CONNECT_TIMEOUT);
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
}
