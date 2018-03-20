package com.etiaro.facebook.functions;

import android.os.AsyncTask;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Interfaces;
import com.etiaro.facebook.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by jakub on 17.03.18.
 */

public class GetUserInfo extends AsyncTask<Interfaces.UserInfoCallback, Void, Boolean> {
    Interfaces.UserInfoCallback[] callbacks;
    Interfaces.UserInfo userInfo = new Interfaces.UserInfo();
    Account ac;
    String ID;
    boolean success = true;
    boolean isAsync = false;
    int attepts = 0;

    public GetUserInfo(Account ac, String ID){
        this.ac = ac;
        this.ID = ID;
    }


    public Interfaces.UserInfo getUserInfo(){ // sync Call
        if(attepts++ > 5) {
            success = false;
            return null;
        }
        try {
            Utils.SiteLoader sl = new Utils.SiteLoader("https://www.facebook.com/chat/user_info/");
            sl.addCookies(ac.cookies);
            String params = ac.getFormParams()+"&ids[]="+ID;
            sl.post(params);
            sl.load();
            ac.cookies = sl.getCookiesManager();

            String json = Utils.checkAndFormatResponse(sl.getData());
            if(json == null){
                Log.e("talkie", "failed GetUserInfo");
                if(isAsync){
                    TimeUnit.SECONDS.sleep(new Random().nextLong()*5);
                    getUserInfo();
                }
                return null;
            }else if(json.equals("NotLoggedIn")){
                Log.e("talkie", "NotLoggedIn");
                success = false;
                return null;
                //TODO RELOG
            }

            JSONObject obj = new JSONObject(json).getJSONObject("payload").getJSONObject("profiles").getJSONObject(ID);
            userInfo.name = obj.getString("name");
            userInfo.firstName = obj.getString("firstName");
            userInfo.vanity = obj.getString("vanity");
            userInfo.thumbSrc = obj.getString("thumbSrc");
            userInfo.profileUrl = obj.getString("uri");
            userInfo.gender = obj.getString("gender");
            userInfo.type = obj.getString("type");
            userInfo.isFriend = obj.getBoolean("is_friend");
            /*
                                "i18nGender":1,
                                "is_active":false,
                                "searchTokens":["Klimek","Kuba"],
                                "alternateName":"",
                                "is_nonfriend_messenger_contact":true
            }*/
        } catch (Exception e) {
            Log.e("UserInfo", e.toString());
            success = false;
        }
        return userInfo;
    }

    @Override
    protected Boolean doInBackground(Interfaces.UserInfoCallback... userInfoCallbacks) { //async call
        callbacks = userInfoCallbacks;
        if(callbacks.length <= 0)
            return false;
        isAsync = true;

        getUserInfo();

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            for (Interfaces.UserInfoCallback c : callbacks) {
                c.success(userInfo);
            }
        } else {
            for (Interfaces.UserInfoCallback c : callbacks) {
                c.fail();
            }
        }
    }

    @Override
    protected void onCancelled() {
        for (Interfaces.UserInfoCallback c : callbacks) {
            c.cancelled();
        }
    }
}