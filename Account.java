package com.etiaro.facebook;

import android.util.Log;

import com.etiaro.facebook.functions.GetUserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class Account {
    private String login, password, revision, fb_dtsg, ttstamp, firstName, name, userID, clientID;
    private boolean loggedIn = false;
    private GetUserInfo.UserInfo info;
    private int reqCounter = 0;
    public CookieManager cookies;
    public Account(String l, String pass, CookieManager cm) throws IOException {
        login = l;
        password = pass;
        cookies = cm;

        Utils.SiteLoader sl = new Utils.SiteLoader("https://www.facebook.com");
        sl.addCookies(cookies);
        sl.load();
        cookies = sl.getCookiesManager();

        revision = Utils.cutString(sl.getData(), "revision\":",",");
        fb_dtsg = Utils.cutString(sl.getData(), "fb_dtsg", "NONE").substring(11, 36);

        ttstamp = "2";
        for (int i = 0; i < fb_dtsg.length(); i++)
            ttstamp += fb_dtsg.charAt(i);
        List<HttpCookie> list = cookies.getCookieStore().get(URI.create("https://www.facebook.com"));

        this.userID = null;
        for(HttpCookie cookie : list)
            if(cookie.getName().equals("c_user")) {
                this.userID = cookie.getValue();
                loggedIn = true;
            }

        clientID = Integer.toHexString(new Random().nextInt(2147483647) | 0);
    }
    public Account(String JSON) throws JSONException{
        JSONObject obj = new JSONObject(JSON);
        login = obj.getString("login");
        password = obj.getString("password");
        revision = obj.getString("revision");
        fb_dtsg = obj.getString("fb_dtsg");
        ttstamp = obj.getString("ttstamp");
        firstName = obj.getString("firstName");
        name = obj.getString("name");
        userID = obj.getString("userID");
        clientID = obj.getString("clientID");
        loggedIn = obj.getBoolean("loggedIn");
        reqCounter = obj.getInt("reqCounter");
        info = new GetUserInfo.UserInfo();
        info.firstName = obj.getJSONObject("info").getString("firstName");
        info.name = obj.getJSONObject("info").getString("name");
        info.gender = obj.getJSONObject("info").getString("gender");
        info.isFriend = obj.getJSONObject("info").getBoolean("isFriend");
        info.profileUrl = obj.getJSONObject("info").getString("profileUrl");
        info.type = obj.getJSONObject("info").getString("type");
        info.vanity = obj.getJSONObject("info").getString("vanity");

        //cookies parsing
        cookies = new CookieManager();
        Iterator<String> itURI = obj.getJSONObject("cookies").keys();
        while (itURI.hasNext()){
            String uri = itURI.next();
            JSONObject arr = obj.getJSONObject("cookies").getJSONObject(uri);
            Iterator<String> itCookie = arr.keys();
            while(itCookie.hasNext()){
                String key = itCookie.next();
                String val = arr.getString(key);
                cookies.getCookieStore().add(URI.create(uri), HttpCookie.parse(key+"="+val+"; Path=/; Domain="+URI.create(uri).getAuthority()).get(0));
            }
        }
    }

    public void loadUserdata() throws IOException {
        info = new GetUserInfo(this, this.userID).getUserInfo();
        firstName = info.firstName;
        name = info.name;
    }

    public String getLogin(){       return login;       }
    public String getPassword(){    return password;    }
    public String getFirstName(){   return firstName;   }
    public String getName(){    return name;            }
    public String getClientID(){    return clientID;    }
    public String getUserID(){      return userID;      }
    public boolean isLogged(){      return loggedIn;    }
    public String getFormParams(){
        return "__user="+userID+
                "&__req="+ Integer.toString((reqCounter++),36)+
                "&__rev="+ revision+
                "&__a=1"+
                "&fb_dtsg="+fb_dtsg+
                "&jazoest="+ttstamp;
    }
    public String toString(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("login", login).put("password", password)
            .put("revision", revision).put("fb_dtsg", fb_dtsg)
            .put("ttstamp",ttstamp).put("firstName",firstName)
            .put("name", name).put("userID", userID).put("clientID",clientID)
            .put("loggedIn", loggedIn).put("reqCounter", reqCounter);
            obj.put("info", new JSONObject()
                    .put("firstName", info.firstName).put("name", info.name)
                    .put("gender", info.gender).put("isFriend", info.isFriend)
                    .put("profileUrl", info.profileUrl).put("type", info.type)
                    .put("vanity",info.vanity));

            //Stringify cookies
            JSONObject c = new JSONObject();
            for(URI uri : cookies.getCookieStore().getURIs()){
                JSONObject arr = new JSONObject();
                for(HttpCookie cookie : cookies.getCookieStore().get(uri)){
                    arr.put(cookie.getName(), cookie.getValue());
                }
                c.put(uri.toString(), arr);
            }
            obj.put("cookies", c);

        } catch (JSONException e) {
            Log.e("talkie", "Critical error while saving account: "+e.toString());
        }

        return obj.toString();
    }
}

//TODO add thread list