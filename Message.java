package com.etiaro.facebook;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by jakub on 21.03.18.
 */

public class Message implements Comparator<Message>, Comparable<Message>{
    public String text, senderID, __typename, message_id, offline_threading_id, conversation_id, sender_email;
    public int ttl;
    public Long timestamp_precise;
    public boolean unread, is_sponsored;
    public ArrayList<Attachment> attachments = new ArrayList<>();
    public Message(JSONObject json) throws JSONException {
        update(json);
    }
	
	public void update(JSONObject json) throws JSONException {
        if(json.has("irisSeqId")){
            text = json.has("body") ?
                    json.getString("body") : "";
            senderID = json.getJSONObject("messageMetadata").getString("actorFbId");
            message_id = json.getJSONObject("messageMetadata").getString("messageId");
            offline_threading_id = json.getJSONObject("messageMetadata").getString("offlineThreadingId");
            if(json.getJSONObject("messageMetadata").getJSONObject("threadKey").has("threadFbId"))
                conversation_id = json.getJSONObject("messageMetadata").getJSONObject("threadKey").getString("threadFbId");
            else if(json.getJSONObject("messageMetadata").getJSONObject("threadKey").has("otherUserFbId"))
                conversation_id = json.getJSONObject("messageMetadata").getJSONObject("threadKey").getString("otherUserFbId");

            timestamp_precise = Long.valueOf(json.getJSONObject("messageMetadata").getString("timestamp"));
            unread = true;
            return;
        }
		if(!json.has("message_id")) {
            text = json.getString("snippet");
            senderID = json.getJSONObject("message_sender").getJSONObject("messaging_actor").getString("id");
        }else{
		    if(json.has("__typename"))
		        __typename = json.getString("__typename");
            message_id = json.getString("message_id");
            offline_threading_id = json.getString("offline_threading_id");  //???
            senderID = json.getJSONObject("message_sender").getString("id");
            if(json.getJSONObject("message_sender").has("email"))
                sender_email = json.getJSONObject("message_sender").getString("email");
            if(json.has("ttl"))
                ttl = json.getInt("ttl");
            unread = json.getBoolean("unread");
            if(json.has("is_sponsored"))
                is_sponsored = json.getBoolean("is_sponsored");
            if(json.has("message") && json.getJSONObject("message").has("text"))
                text = json.getJSONObject("message").getString("text");
        }
        if(json.has("blob_attachments") && json.getJSONArray("blob_attachments").length() > 0) {
            for(int i = 0; i < json.getJSONArray("blob_attachments").length(); i++){
                attachments.add(new Attachment(json.getJSONArray("blob_attachments").getJSONObject(i)));
            }
        }
        timestamp_precise = Long.valueOf(json.getString("timestamp_precise"));
	}

	public JSONObject toJSON(){
        JSONObject obj = null;
        try {
            obj = new JSONObject().put("message", new JSONObject().put("text", text)).put("senderID", senderID)
                .put("__typename", __typename).put("message_sender", new JSONObject()
                    .put("email", sender_email).put("id", senderID)
                    .put("messaging_actor", new JSONObject()
                        .put("id", senderID)))
                .put("message_id", message_id)
                .put("offline_threading_id", offline_threading_id).put("ttl", ttl)
                .put("unread", unread).put("is_sponsored", is_sponsored)
                .put("timestamp_precise", timestamp_precise).put("snippet", text);
            JSONArray arr = new JSONArray();
            for(Attachment a : attachments)
                arr.put(a.toJSON());
            obj.put("blob_attachments", arr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

	public String toString(){
        return toJSON().toString();
	}

    @Override
    public int compare(Message m1, Message m2) {
        return (int)(m2.timestamp_precise - m1.timestamp_precise);
    }

    @Override
    public int compareTo(@NonNull Message msg) {
        return (int)(msg.timestamp_precise - this.timestamp_precise);
    }
}


/*{
    "commerce_message_type":null,
    "customizations":[],
    "tags_list":[
        "source:messenger:web",
        "hot_emoji_size:small",
        "inbox"
    ],
    "platform_xmd_encoded":null,
    "message_source_data":null,
    "montage_reply_data":null,
    TODO "message_reactions":[],          <-------
    "message":{"ranges":[]},
    "extensible_attachment":null,
    "sticker":null,
}*/