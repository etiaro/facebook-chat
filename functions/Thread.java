package com.etiaro.facebook.functions;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jakub on 21.03.18.
 */

public class Thread {
    public ArrayList<Message> messages = new ArrayList<>();
    public String thread_key, name, image, folder, cannot_reply_reason, emoji, outgoing_bubble_color;
    public int unread_count, messages_count, ephemeral_ttl_mode;
    public Boolean isGroup, is_pin_protected, is_viewer_subscribed, thread_queue_enabled, has_viewer_archived,
            is_page_follow_up;
    public Long updated_time_precise, mute_until;
    public HashMap<String, String> nicknames = new HashMap<>();

    Thread(JSONObject json) throws JSONException {
        if (json.getJSONObject("thread_key").getString("thread_fbid") != null) {
            thread_key = json.getJSONObject("thread_key").getString("thread_fbid");
            isGroup = true;
        } else if (json.getJSONObject("thread_key").getString("other_user_id") != null) {
            thread_key = json.getJSONObject("thread_key").getString("other_user_id");
            isGroup = false;
        }
        for (int i = 0; i < json.getJSONObject("last_message").getJSONArray("nodes").length(); i++)
            messages.add(new Message(json.getJSONObject("last_message").getJSONArray("nodes").getJSONObject(i)));

        name = json.getString("name");
        unread_count = json.getInt("unread_count");
        messages_count = json.getInt("messages_count");
        image = json.getString("image");
        if(json.getString("updated_time_precise") != "null")
            updated_time_precise = Long.valueOf(json.getString("updated_time_precise"));
        if(json.getString("mute_until") != "null")
            mute_until = Long.valueOf(json.getString("mute_until")); //TODO check is that float
        is_pin_protected = json.getBoolean("is_pin_protected");
        is_viewer_subscribed = json.getBoolean("is_viewer_subscribed"); //not sure what that means
        thread_queue_enabled = json.getBoolean("thread_queue_enabled");
        folder = json.getString("folder");
        has_viewer_archived = json.getBoolean("has_viewer_archived");
        is_page_follow_up = json.getBoolean("is_page_follow_up");
        cannot_reply_reason = json.getString("cannot_reply_reason");
        ephemeral_ttl_mode = json.getInt("ephemeral_ttl_mode"); //not shure what that means
        if(json.get("customization_info") instanceof JSONObject) {
            emoji = json.getJSONObject("customization_info").getString("emoji");
            outgoing_bubble_color = json.getJSONObject("customization_info").getString("outgoing_bubble_color");
            JSONArray customizations = json.getJSONObject("customization_info").getJSONArray("participant_customizations");
            for (int i = 0; i < customizations.length(); i++)
                nicknames.put(customizations.getJSONObject(i).getString("participant_id"),
                        customizations.getJSONObject(i).getString("nickname"));
        }
    }
}
    /*{
        "thread_admins":[],
        "approval_mode":null,
        "joinable_mode":{
            "mode":"0",
            "link":""
        },
        "thread_queue_metadata":null,
        "event_reminders":{
            "nodes":[]
        },
        "montage_thread":null,
            "last_read_receipt":{
            "nodes":[
                {
                    "timestamp_precise":"1521643267761"
                }
            ]},
        "related_page_thread":null,
        "rtc_call_data":{
            "call_state":"NO_ONGOING_CALL",
            "server_info_data":"",
            "initiator":null
        },
        "associated_object":null,
        "privacy_mode":1,
        "reactions_mute_mode":"REACTIONS_NOT_MUTED",
        "mentions_mute_mode":"MENTIONS_NOT_MUTED",
        "customization_enabled":true,
        "thread_type":"ONE_TO_ONE",
        "participant_add_mode_as_string":null,
        "is_canonical_neo_user":false,
        "participants_event_status":[],
        "page_comm_item":null,
        "all_participants":{
        "nodes":[
            {
                "messaging_actor":{
                "id":"100014199343435",
                "__typename":"User",
                "name":"Jakub Behrendt",
                "gender":"MALE",
                "url":"https:\/\/www.facebook.com\/jakub.behrendt.9",
                "big_image_src":{
                "uri":"https:\/\/scontent-waw1-1.xx.fbcdn.net\/v\/t1.0-1\/p50x50\/20841921_279553982527911_5528938817813006843_n.jpg?_nc_cat=0&oh=7bb7d35ccf5dea12066e932adbb51981&oe=5B44676E"
            },
            "short_name":"Jakub",
                "username":"jakub.behrendt.9",
                "is_viewer_friend":true,
                "is_messenger_user":true,
                "is_verified":false,
                "is_message_blocked_by_viewer":false,
                "is_viewer_coworker":false,
                "is_employee":null
        }
        },
        {
            "messaging_actor":{
            "id":"100003213968599",
                    "__typename":"User",
                    "name":"Kuba Klimek",
                    "gender":"MALE",
                    "url":"https:\/\/www.facebook.com\/kuba.klimek.14",
                    "big_image_src":{
                "uri":"https:\/\/scontent-waw1-1.xx.fbcdn.net\/v\/t1.0-1\/p50x50\/20800359_1392826077501178_2082577223295860735_n.jpg?_nc_cat=0&oh=64cb4c9ab543532fda8098d8c6c63951&oe=5B47134D"
            },
            "short_name":"Kuba",
                    "username":"kuba.klimek.14",
                    "is_viewer_friend":false,
                    "is_messenger_user":true,
                    "is_verified":false,
                    "is_message_blocked_by_viewer":false,
                    "is_viewer_coworker":false,
                    "is_employee":null
        }
        }
        ]
    },
        "read_receipts":{
        "nodes":[
        {
            "watermark":"1521612131129",
                "action":"1521619200141",
                "actor":{
            "id":"100014199343435"
        }
        },
        {
            "watermark":"1521643267761",
                "action":"1521643267761",
                "actor":{
            "id":"100003213968599"
        }
        }
        ]
    },
        "delivery_receipts":{
        "nodes":[
        {
            "timestamp_precise":"1521612131129"
        }
        ]
    }

    }
*/
