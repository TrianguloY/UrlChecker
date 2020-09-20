package com.trianguloy.urlchecker.utilities;

import android.content.Context;

import com.trianguloy.urlchecker.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Class that manages the virusTotal connection
 * // TODO: replace with generic POST class and move logic to VirusTotalModule
 */
public class VirusTotalUtility {

    static private final String urlGetReport = "https://www.virustotal.com/vtapi/v2/url/report";

    static public class InternalReponse {
        public String error = "Unknown error";
        public int detectionsPositive;
        public int detectionsTotal;
        public String date;
        public String scanUrl;
        public String info;
    }

    public static InternalReponse scanUrl(String urlToScan, String key, Context cntx) {
        InternalReponse result = new InternalReponse();

        String responseJSON = performPOST(urlGetReport, getPOSTparameters(urlToScan, key));

        if (responseJSON == null) {
            result.error = cntx.getString(R.string.mVT_connectError);
            return result;
        }

        // parse response
        try {
            JSONObject response = new JSONObject(responseJSON);

            result.info = response.toString(1);

            if (response.getInt("response_code") == 1) {
                result.detectionsPositive = response.optInt("positives", -1);
                result.detectionsTotal = response.optInt("total", -1);
                result.date = response.optString("scan_date", "");
                result.scanUrl = response.optString("permalink", "");

                result.error = null;
                return result;
            } else {
                result.error = response.getString("verbose_msg"); // untranslated
            }
        } catch (JSONException e) {
            e.printStackTrace();
            result.error = cntx.getString(R.string.mVT_jsonError);
        }

        return result;

    }

    static private String getPOSTparameters(String url, String key) {
        String data = null;
        try {
            data = URLEncoder.encode("resource", "UTF-8")
                    + "=" + URLEncoder.encode(url, "UTF-8");

            data += "&" + URLEncoder.encode("scan", "UTF-8") + "="
                    + URLEncoder.encode("1", "UTF-8");

            data += "&" + URLEncoder.encode("apikey", "UTF-8")
                    + "=" + URLEncoder.encode(key, "UTF-8");

            data += "&" + URLEncoder.encode("allinfo", "UTF-8")
                    + "=" + URLEncoder.encode("true", "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;
    }


    private static String performPOST(String urlString, String parameters) {
        BufferedReader reader = null;
        try {

            // Defined URL  where to send data
            URL url = new URL(urlString);

            // Send POST data request
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(parameters);
            wr.flush();

            // Get the server response
            reader = new BufferedReader(new InputStreamReader(
                    conn.getResponseCode() >= 200 && conn.getResponseCode() < 300
                            ? conn.getInputStream()
                            : conn.getErrorStream()
            ));
            StringBuilder sb = new StringBuilder();
            String line;
            // Read Server Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }
}
