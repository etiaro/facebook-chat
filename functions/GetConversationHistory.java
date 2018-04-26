package com.etiaro.facebook.functions;

import android.os.AsyncTask;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.Utils;

import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by jakub on 21.03.18.
 */

public class GetConversationHistory extends AsyncTask<GetConversationHistory.ConversationHistoryCallback, Void, Boolean> {
    ConversationHistoryCallback[] callbacks;
    Conversation conversation;
    boolean success = true;
    boolean isAsync = false;
    int attepts = 0;

    Account ac;
    String threadID;
    int amount;
    float timestamp;

    public GetConversationHistory(Account ac, String threadID, int amount, float timestamp){
        this.ac = ac;
        this.threadID = threadID;
        this.amount = amount;
        this.timestamp = timestamp;
    }


    public Conversation getThreadHistory(){ // sync Call
        if(attepts++ > 5) {
            success = false;
            return null;
        }

        try {
            Utils.SiteLoader sl = new Utils.SiteLoader("https://www.facebook.com/api/graphqlbatch/");
            sl.addCookies(ac.cookies);

            String params = ac.getFormParams()+"&queries="+new JSONObject().put("o0",
                    new JSONObject().put("doc_id", "1527774147243246")
                            .put("query_params",new JSONObject()
                                    .put("id",threadID)
                                    .put("message_limit", amount)
                                    .put("load_messages", 1)
                                    .put("load_read_receipts", false)
                                    .put("before", timestamp>0?timestamp:null)));
            sl.post(params);
            sl.load();
            ac.cookies = sl.getCookiesManager();


            String json = Utils.checkAndFormatResponse(sl.getData());
            if(json == null){
                Log.e("talkie", "failed GetConversationHistory");
                if(isAsync){
                    TimeUnit.SECONDS.sleep(new Random().nextLong()*5);
                    getThreadHistory();
                }
                return null;
            }else if(json.equals("NotLoggedIn")){
                Log.e("talkie", "NotLoggedIn");
                success = false;
                return null;
                //TODO RELOG
            }

            JSONObject threads = new JSONObject(json).getJSONObject("o0").getJSONObject("data")
                    .getJSONObject("message_thread");
            conversation = new Conversation(threads, ac.getUserID());

        } catch (Exception e) {
            Log.e("threadHistory", e.toString());
            success = false;
        }
        return conversation;
    }

    @Override
    protected Boolean doInBackground(ConversationHistoryCallback... userInfoCallbacks) { //async call
        callbacks = userInfoCallbacks;
        if(callbacks.length <= 0)
            return false;
        isAsync = true;
        getThreadHistory();

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            for (ConversationHistoryCallback c : callbacks) {
                c.success(conversation);
            }
        } else {
            for (ConversationHistoryCallback c : callbacks) {
                c.fail();
            }
        }
    }

    @Override
    protected void onCancelled() {
        for (ConversationHistoryCallback c : callbacks) {
            c.cancelled();
        }
    }

    public interface ConversationHistoryCallback {
        void success(Conversation conversation);
        void fail();
        void cancelled();
    }
}