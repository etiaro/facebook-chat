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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    protected void onHandleIntent(final Intent intent) {
        if (intent != null) {
            try {
                Account ac = new Account(intent.getStringExtra("account"));
                while (true){
                    try {
                        pool(ac);
                    } catch (Exception e) {
                        Log.e("Listen", e.toString());
                    }
                    Thread.sleep(new Random().nextInt(200)+50);
                }
            } catch (Exception e) {
                Log.e("Listen", e.toString());
            }
        }
    }

    private void pool(Account ac) throws IOException, JSONException, InterruptedException {
        ac.listenForm.put("channel", "p_"+ac.getUserID());
        if(!ac.listenForm.containsKey("seq"))
            ac.listenForm.put("seq", "0");
        ac.listenForm.put("partition", "-2");
        ac.listenForm.put("clientid", ac.getClientID());
        ac.listenForm.put("viewer_uid", ac.getUserID());
        ac.listenForm.put("uid", ac.getUserID());
        ac.listenForm.put("state","active");
        ac.listenForm.put("idle","0");
        ac.listenForm.put("cap","8");
        ac.listenForm.put("msgs_recv", String.valueOf(ac.msgs_recv));
        ac.cookies.getCookieStore().add(URI.create("https://www.facebook.com"), HttpCookie.parse("presence="+ Utils.generatePresence(ac.getUserID())+"; path=/; domain=.facebook.com; secure").get(0));

        Utils.SiteLoader sl = new Utils.SiteLoader("https://"+ac.serverNumber+"-edge-chat.facebook.com/pull?"+ac.getFormParams()+Utils.formatGetData(ac.listenForm));
        sl.addCookies(ac.cookies);
        sl.load();
        ac.cookies=sl.getCookiesManager();

        JSONObject result = new JSONObject(Utils.checkAndFormatResponse(sl.getData()));
        Log.d("res", result.toString());
        if (result.getString("t").equals("lb")) {
            ac.listenForm.put("sticky_token", result.getJSONObject("lb_info").getString("sticky"));
            ac.listenForm.put("sticky_pool", result.getJSONObject("lb_info").getString("pool"));
        }
        //TODO "fullReload"
        //TODO types of "ms"
        if(result.has("ms")){
            ac.msgs_recv+= result.getJSONArray("ms").length();
            for(int i = 0; i < result.getJSONArray("ms").length(); i++){
                if(!result.getJSONArray("ms").getJSONObject(i).getString("type").equals("delta"))
                    continue;
                if(!result.getJSONArray("ms").getJSONObject(i).getJSONObject("delta").getString("class").equals("NewMessage"))
                    continue;
                callbacks.newMessage(result.getJSONArray("ms").getJSONObject(i).getJSONObject("delta").getString("body"));
                Log.d("message", result.getJSONArray("ms").getJSONObject(i).toString());
            }
        }
        if(result.has("seq"))
            ac.listenForm.put("seq", String.valueOf(result.getInt("seq")));
    }

    public interface ListenCallbacks{
        void newMessage(String msg);
    }
}
