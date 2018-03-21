package com.etiaro.facebook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jakub on 21.03.18.
 */

public class Message {
    public String snippet, senderID;
    public boolean isSenderUser;
    public Long timestamp_precise;
    Message(JSONObject json) throws JSONException {
        snippet = json.getString("snippet");
        isSenderUser = true; //TODO not-user
        if(json.getJSONObject("message_sender").has("messaging_actor"))
            senderID = json.getJSONObject("message_sender").getJSONObject("messaging_actor").getString("id");
        timestamp_precise = Long.valueOf(json.getString("timestamp_precise"));
    }
}

//TODO more args(threadhistory) and not too much IFs statements


/*{
    "__typename":"UserMessage",
    "message_id":"mid.$cAAAAB37oytpoezPdX1iSe6Ul01HF",
    "offline_threading_id":"6382299571171840453",
    "message_sender":{
        "id":"100006952652301",
        "email":"100006952652301\u0040facebook.com"
    },
    "ttl":0,
    "timestamp_precise":"1521658795359",
    "unread":false,
    "is_sponsored":false,
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
    "message_reactions":[],
    "message":{
        "text":"\ud83d\udd2b",
        "ranges":[]
    },
    "extensible_attachment":null,
    "sticker":null,
    "blob_attachments":[],
    "snippet":"\ud83d\udd2b"
}*/