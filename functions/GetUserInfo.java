package com.etiaro.facebook.functions;

import android.os.AsyncTask;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by jakub on 17.03.18.
 */

public class GetUserInfo extends AsyncTask<GetUserInfo.UserInfoCallback, Void, Boolean> {
    UserInfoCallback[] callbacks;
    UserInfo userInfo;
    Account ac;
    String ID;
    boolean success = true;
    boolean isAsync = false;
    int attepts = 0;

    public GetUserInfo(Account ac, String ID){
        this.ac = ac;
        this.ID = ID;
    }


    public UserInfo getUserInfo(){ // sync Call
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
            userInfo = new UserInfo(obj, ID);
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
    protected Boolean doInBackground(UserInfoCallback... userInfoCallbacks) { //async call
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
            for (UserInfoCallback c : callbacks) {
                c.success(userInfo);
            }
        } else {
            for (UserInfoCallback c : callbacks) {
                c.fail();
            }
        }
    }

    @Override
    protected void onCancelled() {
        for (UserInfoCallback c : callbacks) {
            c.cancelled();
        }
    }


    public static class UserInfo{
        public String name, firstName, vanity, thumbSrc, profileUrl, gender, type, id;
        public boolean isFriend;
        public UserInfo(JSONObject obj, String id) throws JSONException {
            name = obj.getString("name");
            firstName = obj.getString("firstName");
            vanity = obj.getString("vanity");
            thumbSrc = obj.getString("thumbSrc");
            profileUrl = obj.getString("uri");
            gender = obj.getString("gender");
            type = obj.getString("type");
            isFriend = obj.getBoolean("is_friend");
            this.id = id;
        }
        public UserInfo(JSONObject obj) throws JSONException {
            id = obj.getJSONObject("messaging_actor").getString("id");
            name = obj.getJSONObject("messaging_actor").getString("name");
            type = obj.getJSONObject("messaging_actor").getString("__typename");
            profileUrl = obj.getJSONObject("messaging_actor").getString("url");
            thumbSrc = obj.getJSONObject("messaging_actor").getJSONObject("big_image_src").getString("uri");
            vanity = obj.getJSONObject("messaging_actor").getString("username"); //NOT sure what "vanity" is

            if(type.equals("User")){ // facebook sends removed from conversation users as ReducedMessagingActor
                firstName = obj.getJSONObject("messaging_actor").getString("short_name");
                isFriend = obj.getJSONObject("messaging_actor").getBoolean("is_viewer_friend");
                gender = obj.getJSONObject("messaging_actor").getString("gender");
            }
        }
        public String toString(){
            try {
                JSONObject obj = new JSONObject().put("messaging_actor", new JSONObject()
                        .put("id", id).put("name", name).put("__typename", type)
                        .put("url", profileUrl).put("big_image_src", new JSONObject().put("uri", thumbSrc))
                        .put("username", vanity).put("short_name",firstName)
                        .put("is_viewer_friend", isFriend).put("gender", gender));
                return obj.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
    public interface UserInfoCallback{
        void success(UserInfo info);
        void fail();
        void cancelled();
    }
}