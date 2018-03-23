package com.etiaro.facebook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jakub on 21.03.18.
 */

public class Message {
    public String text, senderID, __typename, message_id, offline_threading_id, sender_email;
    public int ttl;
    public Long timestamp_precise;
    public boolean unread, is_sponsored;
    Message(JSONObject json) throws JSONException {
        if(!json.has("message_id")) {
            text = json.getString("snippet");
            senderID = json.getJSONObject("message_sender").getJSONObject("messaging_actor").getString("id");
        }else{
            __typename = json.getString("__typename");
            message_id = json.getString("message_id");
            offline_threading_id = json.getString("offline_threading_id");  //???
            senderID = json.getJSONObject("message_sender").getString("id");
            sender_email = json.getJSONObject("message_sender").getString("email");
            ttl = json.getInt("ttl");
            unread = json.getBoolean("unread");
            is_sponsored = json.getBoolean("is_sponsored");
            text = json.getJSONObject("message").getString("text");
        }
        timestamp_precise = Long.valueOf(json.getString("timestamp_precise"));
    }
}

//TODO more args(threadhistory) and not too much IFs statements


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
    "blob_attachments":[],
}*/