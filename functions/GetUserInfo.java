package com.etiaro.facebook.functions;

import android.os.AsyncTask;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Interfaces;
import com.etiaro.facebook.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by jakub on 17.03.18.
 */

public class GetUserInfo extends AsyncTask<Interfaces.UserInfoCallback, Void, Boolean> {
    Interfaces.UserInfoCallback[] callbacks;
    Interfaces.UserInfo userInfo = new Interfaces.UserInfo();
    Account ac;
    String ID;
    boolean success = true;

    public GetUserInfo(Account ac, String ID){
        this.ac = ac;
        this.ID = ID;
    }


    public Interfaces.UserInfo getUserInfo(){
        try {
            Utils.SiteLoader sl = new Utils.SiteLoader("https://www.facebook.com/chat/user_info/");
            sl.addCookies(ac.cookies);
            String params = ac.getFormParams()+"&ids[]="+ID;
            Log.d("params",params);
            sl.post(params);
            sl.load();
            ac.cookies = sl.getCookiesManager();
            Log.d("data", sl.getData());

            //TODO utils function which checks some stuff/errors
            JSONObject obj = new JSONObject(sl.getData().substring(9)).getJSONObject("payload").getJSONObject("profiles").getJSONObject(ID);
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
    protected Boolean doInBackground(Interfaces.UserInfoCallback... userInfoCallbacks) {
        callbacks = userInfoCallbacks;
        if(callbacks.length <= 0)
            return false;

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
