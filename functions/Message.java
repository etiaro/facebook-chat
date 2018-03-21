package com.etiaro.facebook.functions;

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
        senderID = json.getJSONObject("message_sender").getJSONObject("messaging_actor").getString("id");
        timestamp_precise = Long.valueOf(json.getString("timestamp_precise"));
    }
}

        /*{,
        "commerce_message_type":null,
        "extensible_attachment":null,
        "sticker":null,
        "blob_attachments":[]
        }*/