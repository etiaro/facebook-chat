package com.etiaro.facebook.functions;

import android.os.AsyncTask;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by jakub on 20.03.18.
 */

public class GetThreadList extends AsyncTask<GetThreadList.ThreadListCallback, Void, Boolean> {
    ThreadListCallback[] callbacks;
    ArrayList<Thread> threadList = new ArrayList<>();
    boolean success = true;
    boolean isAsync = false;
    int attepts = 0;

    Account ac;
    float timestamp;
    int limit;
    String[] tags;

    public GetThreadList(Account ac, int limit, float timestamp, String[] tags){
        this.ac = ac;
        this.timestamp = timestamp;
        this.limit = limit;
        this.tags = tags;
    }


    public ArrayList<Thread> getThreadList(){ // sync Call
        if(attepts++ > 5) {
            success = false;
            return null;
        }

        try {
            Utils.SiteLoader sl = new Utils.SiteLoader("https://www.facebook.com/api/graphqlbatch/");
            sl.addCookies(ac.cookies);

            String params = ac.getFormParams()+"&queries="+new JSONObject().put("o0",
                    new JSONObject().put("doc_id", "1349387578499440")
                    .put("query_params",new JSONObject()
                            .put("limit",limit+(timestamp>0?1:0))
                            .put("before", timestamp>0?timestamp:null)
                            .put("tags", new JSONArray(tags))
                            .put("includeDeliveryReceipts", true)
                            .put("includeSeqID", false)));
            sl.post(params);
            sl.load();
            ac.cookies = sl.getCookiesManager();


            String json = Utils.checkAndFormatResponse(sl.getData());
            if(json == null){
                Log.e("talkie", "failed GetUserInfo");
                if(isAsync){
                    TimeUnit.SECONDS.sleep(new Random().nextLong()*5);
                    getThreadList();
                }
                return null;
            }else if(json.equals("NotLoggedIn")){
                Log.e("talkie", "NotLoggedIn");
                success = false;
                return null;
                //TODO RELOG
            }

            JSONArray threads = new JSONObject(json).getJSONObject("o0").getJSONObject("data")
                .getJSONObject("viewer").getJSONObject("message_threads").getJSONArray("nodes");
            for(int i = 0; i < threads.length(); i++){
                threadList.add(new Thread(threads.getJSONObject(i)));
            }

        } catch (Exception e) {
            Log.e("threadList", e.toString());
            success = false;
        }
        return threadList;
    }

    @Override
    protected Boolean doInBackground(ThreadListCallback... userInfoCallbacks) { //async call
        callbacks = userInfoCallbacks;
        //if(callbacks.length <= 0)
            //return false;
        isAsync = true;

        getThreadList();

        return success;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (success) {
            for (ThreadListCallback c : callbacks) {
                c.success(threadList);
            }
        } else {
            for (ThreadListCallback c : callbacks) {
                c.fail();
            }
        }
    }

    @Override
    protected void onCancelled() {
        for (ThreadListCallback c : callbacks) {
            c.cancelled();
        }
    }

    public interface ThreadListCallback{
        void success(ArrayList<Thread> list);
        void fail();
        void cancelled();
    }
}