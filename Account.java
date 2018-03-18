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
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Account {
    private String login, password, revision, fb_dtsg, ttstamp, firstName, name, userID, clientID;
    private boolean loggedIn = false;
    private Interfaces.UserInfo info;
    private int reqCounter = 0;
    public CookieManager cookies;
    public Account(String l, String pass, CookieManager cm) throws IOException {
        login = l;
        password = pass;
        cookies = cm;

        Utils.SiteLoader sl = new Utils.SiteLoader("https://www.facebook.com");
        sl.addCookies(cm);
        sl.load();

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
    public Account(String JSON) throws JSONException {
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
        info.firstName = obj.getJSONObject("info").getString("firstName");
        info.name = obj.getJSONObject("info").getString("name");
        info.gender = obj.getJSONObject("info").getString("gender");
        info.isFriend = obj.getJSONObject("info").getBoolean("isFriend");
        info.profileUrl = obj.getJSONObject("info").getString("profileUrl");
        info.type = obj.getJSONObject("info").getString("type");
        info.vanity = obj.getJSONObject("info").getString("vanity");

        //TODO cookies parsing
    }

    public void loadUserdata(){
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
    public String toString(){//TODO stringify account
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
            obj.put("cookies", new JSONObject()); //TODO cookies stringify
        } catch (JSONException e) {
            Log.e("talkie", "Critical error while saving account: "+e.toString());
        }

        return obj.toString();
    }
}
