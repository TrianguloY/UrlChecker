package com.trianguloy.urlchecker.utilities.wrappers;

import com.trianguloy.urlchecker.utilities.methods.StreamUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/** Wrapper of HttpsURLConnection with some util methods */
public class Connection {
    public static final int CONNECT_TIMEOUT = 5000;

    /** Initializes a new connection to a specific url */
    public static Request to(String url) {
        return new Request(url);
    }

    public static class Request {
        private HttpsURLConnection connection = null;

        private Request(String url) {
            try {
                connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /** Adds a header to the connection */
        public Request addHeader(String header, String value) {
            if (connection == null) return this;
            connection.setRequestProperty(header, value);
            return this;
        }

        /** Marks this request as accepting json */
        public Request acceptJson() {
            if (connection != null) connection.setRequestProperty("accept", "application/json");
            return this;
        }

        /** Connects and posts a string as body */
        public Response postString(String body) {
            if (connection == null) return new Response(null);

            connection.setDoOutput(true);
            try (var writer = new OutputStreamWriter(connection.getOutputStream())) {
                writer.write(body);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                connection = null;
            }
            return new Response(connection);
        }

        /** Connects and posts a key=value collection as body */
        public Response postFormUrlEncoded(Map<String, String> form) {
            if (connection == null) return new Response(null);

            addHeader("content-type", "application/x-www-form-urlencoded");

            try {
                var body = new StringBuilder();
                for (var param : form.entrySet()) {
                    if (body.length() != 0) body.append('&');
                    body.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    body.append('=');
                    body.append(URLEncoder.encode(param.getValue(), "UTF-8"));
                }
                return postString(body.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new Response(null);
            }
        }

        /** Performs the connection */
        public Response connect() {
            if (connection == null) return new Response(null);
            try {
                connection.connect();
                return new Response(connection);
            } catch (IOException e) {
                e.printStackTrace();
                return new Response(null);
            }
        }
    }

    public static class Response {
        private HttpsURLConnection connection;

        private Response(HttpsURLConnection connection) {
            this.connection = connection;
        }

        /** Returns the status code, or -1 if something failed */
        public int getStatusCode() {
            if (connection == null) return -1;

            try {
                return connection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
                connection = null;
                return -1;
            }
        }

        /** Returns the result as string, or null if something failed */
        public String getResultAsString() {
            if (connection == null) return null;

            try {
                return StreamUtils.inputStream2String(getStream());
            } catch (IOException e) {
                e.printStackTrace();
                connection = null;
                return null;
            }
        }

        /** Returns the result as json, or null if something failed */
        public JSONObject getResultAsJson() {
            var string = getResultAsString();
            if (string == null) return null;
            try {
                return new JSONObject(string);
            } catch (JSONException e) {
                e.printStackTrace();
                connection = null;
                return null;
            }
        }

        private InputStream getStream() throws IOException {
            return connection.getResponseCode() >= 200 && connection.getResponseCode() < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
        }
    }
}
