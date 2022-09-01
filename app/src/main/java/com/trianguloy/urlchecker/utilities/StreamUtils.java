package com.trianguloy.urlchecker.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

/**
 * Generic utilities related to streams (urls, strings, bytes...)
 */
public class StreamUtils {
    public static final Charset UTF_8 = Charset.forName("UTF-8"); // StandardCharsets.UTF_8 requires api 19
    public static final int CONNECT_TIMEOUT = 5000;

    /**
     * GETs an url and returns the content as string
     */
    public static String readFromUrl(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        return inputStream2String(connection.getInputStream());
    }

    /**
     * POSTs something (a body) to an url, returns its content as a string
     */
    public static String performPOST(String url, String body) throws IOException {

        // Defined URL  where to send data
        URL urlObject = new URL(url);

        // Send POST data request
        HttpsURLConnection conn = (HttpsURLConnection) urlObject.openConnection();
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(body);
        wr.flush();

        // Get the server response
        return inputStream2String(
                conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                        ? conn.getInputStream()
                        : conn.getErrorStream()
        );

    }

    /**
     * Reads an input stream and returns its content as string.
     * The stream is closed afterwards
     */
    public static String inputStream2String(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() != 0) sb.append("\n");
                sb.append(line);
            }
            return sb.toString();
        }
    }

    /**
     * Returns the sha-256 of a string
     */
    public static String sha256(String string) {
        try {
            // get byte array
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(string.getBytes(UTF_8));

            // convert to string
            // adapted from https://stackoverflow.com/questions/7166129/how-can-i-calculate-the-sha-256-hash-of-a-string-in-android/7166240#7166240
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) hex.append(String.format("%02x", b & 0xFF));
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // this should never happen, all Androids must support sha-256
            return "";
        }
    }

}
