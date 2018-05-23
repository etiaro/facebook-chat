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
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Listen extends IntentService {
    public static boolean isStarted = false;
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

        ac.listenForm.clear();
        ac.msgs_recv = 0;
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
        try {
            sl.load();
            ac.cookies = sl.getCookiesManager();
        }catch (SocketTimeoutException e){
            Log.d("pull", "Timedout...");
            return;
        }catch (Exception e){//TODO just EAI_AGAIN
            ac.serverNumber = new Random().nextInt(5)+1;
        }
        if(sl.getResponseCode() != 200)
            return;

        JSONObject result = new JSONObject(Utils.checkAndFormatResponse(sl.getData()));
        Log.e("res", result.toString());
        if (result.getString("t").equals("lb")) {
            ac.listenForm.put("sticky_token", result.getJSONObject("lb_info").getString("sticky"));
            ac.listenForm.put("sticky_pool", result.getJSONObject("lb_info").getString("pool"));
        }
        if(result.has("tr"))
            ac.listenForm.put("traceid", result.getString("traceid"));
        if(result.has("seq"))
            ac.listenForm.put("seq", String.valueOf(result.getInt("seq")));

        //TODO "fullReload" - is it really needed?
        if(result.has("ms")){
            ac.msgs_recv+= result.getJSONArray("ms").length();
            for(int i = 0; i < result.getJSONArray("ms").length(); i++){
                try {
                    handleData(result.getJSONArray("ms").getJSONObject(i), ac);
                }catch (Exception e){
                    Log.e("listen", "Error on listen data parsing");
                }
            }
        }
    }

    public interface ListenCallbacks{
        void newMessage(Message msg);
        void typing(String threadid, String userid, boolean isTyping);
        void presenceUpdate(Map<String, Long> users);
        void readReceipt(JSONObject ob);
        void deliveryReceipt(JSONObject ob);
    }
    private void handleData(JSONObject ms, Account ac) throws JSONException {
        String userID;
        switch (ms.getString("type")){
            case "typ":
                userID = "";
                if(ms.has("realtime_viewer_fbid"))
                    userID = ms.getString("realtime_viewer_fbid");
                else if(ms.has("from"))
                    userID = ms.getString("from");

                String threadID = "";
                if(ms.has("to"))
                    threadID = ms.getString("to");
                else if(ms.has("thread_fbid"))
                    threadID = ms.getString("thread_fbid");
                else if(ms.has("from"))
                    threadID = ms.getString("from");

                boolean isTyping = ms.getInt("st") != 0;
                callbacks.typing(userID, threadID, isTyping);
                break;
            case "chatproxy-presence":
                HashMap<String, Long> users = new HashMap<>();
                Iterator<?> keys = ms.getJSONObject("buddyList").keys();
                while(keys.hasNext()) {
                    userID = (String)keys.next();
                    JSONObject obj = ms.getJSONObject("buddyList").getJSONObject(userID);
                    if (!obj.has("lat") || !obj.has("p")) continue; //TODO there can be only lat!
                    if(obj.getInt("p") == 2)
                        users.put(userID, 0l);
                    else
                        users.put(userID, obj.getLong("lat")*1000);
                }
                callbacks.presenceUpdate(users);
                break;
            case "buddylist_overlay":
                HashMap<String, Long> usersov = new HashMap<>();
                Iterator<?> overlayKeys = ms.getJSONObject("overlay").keys();
                while (overlayKeys.hasNext()){
                    userID = (String) overlayKeys.next();
                    JSONObject obj = ms.getJSONObject("overlay").getJSONObject(userID);
                    if(obj.getInt("a") == 2)
                        usersov.put(userID, 0l);
                    else
                        usersov.put(userID, obj.getLong("la")*1000);
                }
                callbacks.presenceUpdate(usersov);
                break;
            case "delta":
                JSONObject delta = ms.getJSONObject("delta");
                switch (delta.getString("class")){
                    case "NewMessage":
                        Message msg = new Message(delta);
                        callbacks.newMessage(msg);
                        break;
                    case "ReadReceipt":
                        callbacks.readReceipt(delta);
                        break;
                    case "DeliveryReceipt":
                        callbacks.deliveryReceipt(delta);
                        break;
                }
                break;
        }
    }
}
