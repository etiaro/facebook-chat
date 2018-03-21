package com.etiaro.facebook.functions;

import android.os.AsyncTask;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Calendar;




public class Login extends AsyncTask<Login.LoginCallback, Void, Boolean> {
    private final String mLogin;
    private final String mPassword;
    private LoginCallback[] callbacks;
    private Account ac = null;

    public Login(String email, String password) {
        mLogin = email;
        mPassword = password;
    }
    
    @Override
    protected Boolean doInBackground(LoginCallback... callbacks) {

        if(callbacks.length <= 0)
            return false;
        this.callbacks = callbacks;

        try {
            Utils.SiteLoader sl = new Utils.SiteLoader("https://facebook.com");
            sl.load();
            CookieManager cm = new CookieManager();

            //Getting cookies from HTML - really hacky :P
            String[] tmp = sl.getData().split("\"_js_");
            for(int i = 1; i < tmp.length;i++){
                String s = "[\""+tmp[i].substring(0, tmp[i].indexOf("]"))+ "]";
                JSONArray arr = new JSONArray(s);
                String formatCookie = arr.get(0)+"="+arr.get(1)+"; Path=" + arr.get(3) + "; Domain=facebook.com";
                cm.getCookieStore().add(new URI("https://www.facebook.com"), HttpCookie.parse(formatCookie).get(0));
            }

            Utils.SiteLoader sl2 = new Utils.SiteLoader("https://www.facebook.com/login.php?login_attempt=1&lwv=110");

            sl2.addCookies(cm);
            sl2.followRedirects(false);
            String params = "lsd="+Utils.cutString(sl.getData(),"\\[\"LSD\",\\[\\],\\{\"token\":\"", "\"\\}")+
                    "&lgndim={\"w\":1440,\"h\":900,\"aw\":1440,\"ah\":834,\"c\":24}"+
                    "&email="+ mLogin +"&pass="+ mPassword +"&default_persistent=0"+
                    //"&lgnrnd="+Utils.cutString(sl.getData(), "lgnrnd", "NONE").substring(11, 22)+   // no needed?
                    "&locale=en_US&timezone=240"+
                    "&lgnjs="+(int)(Calendar.getInstance().getTimeInMillis()/1000);
            sl2.post(params);

            sl2.load();
            if(!sl2.getHeaderField("location").equals("https://www.facebook.com/"))
                return false;//something gone wrong

            ac = new Account(mLogin, mPassword, cm);
            if(!ac.isLogged())  //Again, something gone wrong
                return false;

            Log.d("REQUEST", "Reconnect");
            sl = new Utils.SiteLoader("https://www.facebook.com/ajax/presence/reconnect.php?"+ac.getFormParams()+"&reason=6");
            sl.addCookies(ac.cookies);
            sl.load();
            ac.cookies = sl.getCookiesManager();

            Log.d("REQUEST", "Pull 1");

            //TODO Lot of 400 statusCode errors here
            params = ac.getFormParams()+"&channel=p_" + ac.getUserID()+
                    "&seq=0"+
                    "&partition=-2"+
                    "&clientid="+ ac.getClientID()+
                    "&viewer_uid="+ ac.getUserID()+
                    "&uid="+ac.getUserID()+
                    "&state=active"+
                    "&idle=0"+
                    "&cap=8"+
                    "&msgs_recv=0";
            sl = new Utils.SiteLoader("https://0-edge-chat.facebook.com/pull?"+params);
            String presence = Utils.generatePresence(ac.getUserID());
            ac.cookies.getCookieStore().add(URI.create("https://www.facebook.com"), HttpCookie.parse("presence="+presence+"; path=/; domain=.facebook.com; secure").get(0));
            ac.cookies.getCookieStore().add(URI.create("https://www.messenger.com"), HttpCookie.parse("presence=" + presence + "; path=/; domain=.messenger.com; secure").get(0));
            ac.cookies.getCookieStore().add(URI.create("https://www.facebook.com"), HttpCookie.parse("locale=en_US; path=/; domain=.facebook.com; secure").get(0));
            ac.cookies.getCookieStore().add(URI.create("https://www.messenger.com"), HttpCookie.parse("locale=en_US; path=/; domain=.messenger.com; secure").get(0));
            ac.cookies.getCookieStore().add(URI.create("https://www.facebook.com"), HttpCookie.parse("a11y="+ Utils.generateAccessibilityCookie() +"; path=/; domain=.facebook.com; secure").get(0));
            sl.addCookies(ac.cookies);
            sl.load();
            ac.cookies=sl.getCookiesManager();

            JSONObject json;
            try{
                json = new JSONObject(sl.getData().replace("for (;;); ", ""));
            }catch (JSONException e){
                Log.e("JSON", e.toString());
                //TODO komunikat o problemach, sprobuj w przegladarce
                return false;
            }

            if(!json.getString("t").equals("lb"))
                return false;//TODO komunikat o problemach, sprobuj w przegladarce

            Log.d("REQUEST", "Pull 2");
            params = ac.getFormParams()+"channel=p_"+ ac.getUserID()+
                    "&seq=0"+
                    "&partition=-2"+
                    "&clientid="+ ac.getClientID()+
                    "&viewer_uid="+ ac.getUserID()+
                    "&uid="+ ac.getUserID()+
                    "&state=active"+
                    "&idle=0"+
                    "&cap=8"+
                    "&msgs_recv=0"+
                    "&sticky_token="+ json.getJSONObject("lb_info").getString("sticky")+
                    "&sticky_pool="+ json.getJSONObject("lb_info").getString("pool");
            sl = new Utils.SiteLoader("https://0-edge-chat.facebook.com/pull?"+params);
            sl.addCookies(ac.cookies);
            sl.load();
            ac.cookies = sl.getCookiesManager();

            //TODO pages login, 2-step verification login
            ac.loadUserdata();

            return true;
        } catch (Exception e) {
            Log.e("internet", e.toString());
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean success) {

        if (success) {
            for (LoginCallback c : callbacks) {
                c.success(ac);
            }
        } else {
            for (LoginCallback c : callbacks) {
                c.fail();
            }
        }
    }

    @Override
    protected void onCancelled() {
        for (LoginCallback c : callbacks) {
            c.cancelled();
        }
    }


    public interface LoginCallback{
        void success(Account ac);
        void fail();
        void cancelled();
    }
}
