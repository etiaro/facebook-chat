package com.etiaro.facebook.functions;

import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.telecom.Call;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.Utils;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SendMessage extends AsyncTask<SendMessage.SendMessageCallback, Void, Boolean>{
    boolean success;
    SendMessageCallback[] callbacks;
    Account ac;
    Conversation conversation;
    String body;
    long timestamp;

    public SendMessage(Account ac, Conversation conversation, String body, long timestamp){
        this.ac = ac;
        this.conversation = conversation;
        this.body = body;
        this.timestamp = timestamp > 0 ? timestamp : Calendar.getInstance().getTimeInMillis();
    }

    @Override
    protected Boolean doInBackground(SendMessageCallback... callbacks) {
        this.callbacks = callbacks;
        return sendMessage();
    }
    Boolean sendMessage(){
        try {
            Utils.SiteLoader sl = new Utils.SiteLoader("https://www.facebook.com/messaging/send/");
            sl.addCookies(ac.cookies);
            String msgAndOTID = Utils.generateOfflineThreadingID();

            String params = ac.getFormParams()+
                    "&body="+body+
                    "&action_type=ma-type:user-generated-message"+
                    "&client=mercury"+
                    "&timestamp="+timestamp+
                    "&timestamp_absolute=Today"+
                    "&timestamp_time_passed=0"+
                    "&source=source:chat:web"+
                    "&threading_id="+Utils.generateThreadingID(ac.getClientID())+
                    "&message_id="+msgAndOTID+
                    "&offline_threading_id="+msgAndOTID+
                    "&signatureID="+Integer.toHexString(new Random().nextInt(2147483647))+
                    "&ui_push_phase=C3"+
                    "&has_attachment=false";//TODO attachments
            if(conversation.isGroup){
                params+="&thread_fbid="+conversation.thread_key;
            }else
                params+= "&other_user_fbid="+conversation.thread_key+
                        "&specific_to_list[0]=fbid:"+conversation.thread_key+ //TODO multiple users
                        "&specific_to_list[1]=fbid:"+ac.getUserID();

            sl.post(params);
            sl.load();
            ac.cookies = sl.getCookiesManager();
            if (sl.getResponseCode() != HttpURLConnection.HTTP_OK) {
                success = false;
                Log.d("resp", String.valueOf(sl.getResponseCode()));
                return false;
            }


            String json = Utils.checkAndFormatResponse(sl.getData());
            if(json == null){
                Log.e("talkie", "failed SendMessage");
                //TODO async
                return false;
            }else if(json.equals("NotLoggedIn")){
                Log.e("talkie", "NotLoggedIn");
                success = false;
                return false;
                //TODO RELOG
            }

            if(new JSONObject(json).has("status"));
            return true;
        } catch (Exception e) {
            Log.e("sendMessage", e.toString());
            success = false;
        }
        return true;//TODO checking response
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            for (SendMessageCallback c : callbacks) {
                c.success();
            }
        } else {
            for (SendMessageCallback c : callbacks) {
                c.fail();
            }
        }
    }
    public interface SendMessageCallback{
        void success();
        void fail();
    }
}