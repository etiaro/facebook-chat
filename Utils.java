package com.etiaro.facebook;

import android.text.TextUtils;
import android.util.Log;
import android.util.MalformedJsonException;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jakub on 15.03.18.
 */

public class Utils {
    public static String encrypt(String from, String key){
        String generatedString = "";
        try {
            // Create key and cipher
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(from.getBytes());
            generatedString = new String(encrypted);
        } catch (Exception e) {
            Log.e("Encrypt", e.toString());
        }
        return generatedString;
    }
    public static String decrypt(String from, String key){
        String generatedString = "";
        try {
            // Create key and cipher
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            byte[] encrypted = from.getBytes();
            // decrypt the text
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            generatedString = new String(cipher.doFinal(encrypted));
        } catch (Exception e) {
            Log.e("Encrypt", e.toString());
        }
        return generatedString;
    }

    public static String readBuffer(InputStream is) throws IOException {
        String ret = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        ret += out;
        reader.close();
        return ret;
    }

    public static String cutString(String str, String start, String end){
        if (str.split(start).length <= 1)
            return "null";
        str = str.split(start)[1];
        str = str.split(end)[0];
        return str;
    }

    public static String generatePresence(String ID) {
        long time = Calendar.getInstance().getTimeInMillis();
        try {
            return "E" + URLEncoder.encode("{\"v\": 3, \"time\":" + Math.round(time / 1000) +
                    ",\"user\"" + ID + ",\"state\":{" +
                    "\"ut\":0" + ",\"t2\":[]" + ",\"lm2\":null" + ",\"uct2\":" + time + ",\"tr\":null" +
                    ",\"tw\":" + ((int) (new Random().nextFloat() * 4294967295f) + 1) +
                    ",\"at\":" + time +
                    "},\"ch\":{" +
                    "[\"p_\"" + ID + "]: 0" +
                    "}" + "}", "UTF-8").toLowerCase().replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return "null";
        }
    }
    public static String generateAccessibilityCookie() {
        long time = Calendar.getInstance().getTimeInMillis();
        try {
            return URLEncoder.encode(
                    "{'sr': 0,'sr-ts':"+ time+
                    ",'jk': 0,'jk-ts':"+ time+
                    ",'kb': 0,'kb-ts':"+ time+
                    ",'hcm': 0"+
                    ",'hcm-ts':"+ time+"}","UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return "null";
        }
    }

    public static String formatGetData(HashMap<String, String>values){
        String s = "";
        for(Map.Entry<String, String> e : values.entrySet())
            s+= "&"+e.getKey()+"="+e.getValue();
        return s;
    }

    public static String checkAndFormatResponse(String response){
        if(response.indexOf("{") >0)
            response = response.substring(response.indexOf("{"));
        try{
            String er = new JSONObject(response).getString("error");
            if(er.equals("1357001"))
                return "NotLoggedIn";
            if(er != null)
                return null;
        }catch (Exception e){
            return response;
        }
        return response;
    }

    //NEVER call from main thread!
    public static class SiteLoader {
        private String data;
        private CookieManager cookiesManager = new CookieManager();
        private HttpURLConnection connection;
        private boolean headersSet = false;

        public SiteLoader(String URL) throws IOException {
            java.net.URL url = new URL(URL);
            connection = (HttpURLConnection) url.openConnection();

            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
        }

        public void followRedirects(boolean b){
            connection.setInstanceFollowRedirects( b );
        }

        public void addCookies(CookieManager cookieManager){
            cookiesManager = cookieManager;
            if (cookieManager.getCookieStore().getCookies().size() > 0) {
                connection.setRequestProperty("Cookie",
                        TextUtils.join(";",  cookieManager.getCookieStore().getCookies()));
            }
        }

        public void post(String params) throws IOException {

            connection.setDoOutput(true);
            connection.setRequestMethod( "POST" );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty( "charset", "utf-8");
            connection.setRequestProperty( "Content-Length", Integer.toString(params.length()));
            connection.setRequestProperty( "Referer", "https://www.facebook.com/");
            connection.setRequestProperty( "Origin", "https://www.facebook.com");
            connection.setRequestProperty( "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/600.3.18 (KHTML, like Gecko) Version/8.0.3 Safari/600.3.18");
            connection.setRequestProperty( "Connection", "keep-alive");
            connection.setUseCaches( false );

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            byte[] postData = params.getBytes( StandardCharsets.UTF_8 );
            out.write(postData);
            out.flush();
            out.close();
        }

        public void load() throws IOException {
            int status = connection.getResponseCode();
            Log.d("ResponseCode", String.valueOf(status));
            InputStream in;
            if (status == HttpURLConnection.HTTP_BAD_REQUEST)
                in = new BufferedInputStream(connection.getErrorStream());
           else
                in = new BufferedInputStream(connection.getInputStream());

            data = readBuffer(in);  //connected loads site content
            connection.disconnect();

            Map<String, List<String>> headerFields = connection.getHeaderFields();

            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    cookiesManager.getCookieStore().add(URI.create(connection.getURL().toString()), HttpCookie.parse(cookie).get(0));
                }
            }
        }

        public String getData(){   return data;    }
        public CookieManager getCookiesManager(){  return cookiesManager;  }
        public String getHeaderField(String name){ return connection.getHeaderField(name);  }
    }
}
