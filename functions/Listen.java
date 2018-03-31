package com.etiaro.facebook.functions;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Message;
import com.etiaro.facebook.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;

public class Listen extends IntentService {
    static boolean isStarted = false;
    static ListenCallbacks callbacks;

    public Listen() {
        super("Listen");
    }

    public static void start(Context context, Account ac){
        start(context, ac, null);
    }
    public static void start(Context context, Account ac, ListenCallbacks lc){
        if(lc != null)
            callbacks = lc;
        if(isStarted)
            return;
        Intent intent = new Intent(context, Listen.class);
        intent.putExtra("account", ac.toString());
        context.startService(intent);
        isStarted = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                Account ac = new Account(intent.getStringExtra("account"));
                pool(ac);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pool(Account ac) throws IOException, JSONException {
        String form = "channel=p_"+ac.getUserID()+
                "&seq=0&partition=-2&clientid="+ac.getClientID()+
                "&viewer_uid="+ac.getUserID()+
                "&uid="+ac.getUserID()+
                "&state=active&idle=0&idle=0&cap=8&msgs_recv"+ac.msgs_recv;
        ac.cookies.getCookieStore().add(URI.create("https://www.facebook.com"), HttpCookie.parse("presence="+ Utils.generatePresence(ac.getUserID())+"; path=/; domain=.facebook.com; secure").get(0));

        Utils.SiteLoader sl = new Utils.SiteLoader("https://"+ac.serverNumber+"-edge-chat.facebook.com/pull?"+form);
        sl.addCookies(ac.cookies);
        sl.load();
        ac.cookies=sl.getCookiesManager();

        JSONObject result = new JSONObject(Utils.checkAndFormatResponse(sl.getData()));
        if (result.getString("t") == "lb") {
            form += "&sticky_token="+ result.getJSONObject("lb_info").getString("sticky");
            form += "&sticky_pool="+ result.getJSONObject("lb_info").getString("pool");
        }
        Log.d("LISTENER", "did 1st pool");
    }

    public interface ListenCallbacks{
        void newMessage(String msg);
    }
}
