package com.etiaro.facebook;

import android.support.annotation.NonNull;
import android.util.Log;

import com.etiaro.talkie.MemoryManger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jakub on 21.03.18.
 */

public class Conversation implements Comparator<Conversation>, Comparable<Conversation>{
    public LinkedHashMap<String, Message> messages = new LinkedHashMap<>();
    public String thread_key, name, image, folder, cannot_reply_reason, emoji, outgoing_bubble_color, snippet, accountID;
    public int unread_count, messages_count, ephemeral_ttl_mode;
    public Boolean isGroup, is_pin_protected, is_viewer_subscribed, thread_queue_enabled, has_viewer_archived,
            is_page_follow_up;
    public Long updated_time_precise = 0l, mute_until = 0l;
    public HashMap<String, String> nicknames = new HashMap<>();
    public ArrayList<String> userIDs = new ArrayList<>();
    public ArrayList<String> typing = new ArrayList<>();

    public Conversation(JSONObject json, String ownerID) throws JSONException {
        update(json);
        accountID = ownerID;
    }
	
    public void update(JSONObject json) throws JSONException {
        if(json.has("messages")) {
           JSONArray msgs = json.getJSONObject("messages").getJSONArray("nodes");
            for (int i = 0; i < msgs.length(); i++) {
                Message msg = new Message(msgs.getJSONObject(i));
                if(messages.containsKey(msg.message_id))
                    messages.get(msg.message_id).update(msg.toJSON());
                else
                    messages.put(msg.message_id, msg);
            }
        }
        sortMessages();
        snippet = json.getJSONObject("last_message").getJSONArray("nodes").getJSONObject(0).getString("snippet");

        unread_count = json.getInt("unread_count");
        messages_count = json.getInt("messages_count");
        if(json.getString("updated_time_precise") != "null")
            updated_time_precise = Long.valueOf(json.getString("updated_time_precise"));
        if(json.getString("mute_until") != "null")
            mute_until = Long.valueOf(json.getString("mute_until")); //TODO check is that float
        is_pin_protected = json.getBoolean("is_pin_protected");
        is_viewer_subscribed = json.getBoolean("is_viewer_subscribed"); //not sure what that means
        thread_queue_enabled = json.getBoolean("thread_queue_enabled");
        folder = json.getString("folder");
        has_viewer_archived = json.getBoolean("has_viewer_archived");
        if(json.has("is_page_follow_up"))
            is_page_follow_up = json.getBoolean("is_page_follow_up");
        cannot_reply_reason = json.getString("cannot_reply_reason");
        ephemeral_ttl_mode = json.getInt("ephemeral_ttl_mode"); //not shure what that means
        if(json.has("all_participants")){
            JSONArray users = json.getJSONObject("all_participants").getJSONArray("nodes");
            for(int i = 0; i < users.length(); i++){
                userIDs.add(users.getJSONObject(i).getJSONObject("messaging_actor").getString("id"));
                if(users.getJSONObject(i).getJSONObject("messaging_actor").has("name"))
                    nicknames.put(users.getJSONObject(i).getJSONObject("messaging_actor").getString("id"),
                        users.getJSONObject(i).getJSONObject("messaging_actor").getString("name"));
            }
        }
        if(json.has("customization_info") && json.get("customization_info") instanceof JSONObject) {
            if(json.getJSONObject("customization_info").has("emoji"))
                emoji = json.getJSONObject("customization_info").getString("emoji");
            if(json.getJSONObject("customization_info").has("outgoing_bubble_color"))
                outgoing_bubble_color = json.getJSONObject("customization_info").getString("outgoing_bubble_color");
            JSONArray customizations = json.getJSONObject("customization_info").getJSONArray("participant_customizations");
            for (int i = 0; i < customizations.length(); i++)
                nicknames.put(customizations.getJSONObject(i).getString("participant_id"),
                        customizations.getJSONObject(i).getString("nickname"));
        }
        if(json.get("thread_key") instanceof String) {
            thread_key = json.getString("thread_key");
            isGroup = json.getBoolean("isGroup");
            if(json.has("name"))
                name = json.getString("name");
            if(json.has("image"))
                image = json.getString("image");
        }else if (json.getJSONObject("thread_key").getString("thread_fbid") != "null") {
            thread_key = json.getJSONObject("thread_key").getString("thread_fbid");
            name = json.getString("name");
            if(json.has("image") && json.get("image") instanceof JSONObject)
                image = json.getJSONObject("image").getString("uri");
            isGroup = true;
        } else if (json.getJSONObject("thread_key").getString("other_user_id") != "null") {
            thread_key = json.getJSONObject("thread_key").getString("other_user_id");
            if(nicknames.containsKey(thread_key))
                name = nicknames.get(thread_key);
            else if(json.getJSONObject("all_participants").getJSONArray("nodes")
                    .getJSONObject(0).getJSONObject("messaging_actor").has("name"))
                name = json.getJSONObject("all_participants").getJSONArray("nodes")
                        .getJSONObject(0).getJSONObject("messaging_actor").getString("name");
            if(json.getJSONObject("all_participants").getJSONArray("nodes")
                    .getJSONObject(0).getJSONObject("messaging_actor").has("big_image_src"))
                image = json.getJSONObject("all_participants").getJSONArray("nodes")
                        .getJSONObject(0).getJSONObject("messaging_actor").getJSONObject("big_image_src").getString("uri");
            isGroup = false;
        }
	}

	public JSONObject toJSON(){
        JSONObject obj = null;
        try {
            obj = new JSONObject().put("accountID", accountID).put("thread_key", thread_key).put("isGroup", isGroup)
                .put("unread_count", unread_count).put("messages_count", messages_count)
                .put("image", image).put("updated_time_precise", updated_time_precise)
                .put("mute_until", mute_until).put("is_pin_protected", is_pin_protected)
                .put("is_viewer_subscribed", is_viewer_subscribed).put("thread_queue_enabled", thread_queue_enabled)
                .put("folder", folder).put("has_viewer_archived", has_viewer_archived)
                .put("is_page_follow_up", is_page_follow_up).put("cannot_reply_reason", cannot_reply_reason)
                .put("ephemeral_ttl_mode", ephemeral_ttl_mode).put("name", name).put("last_message", new JSONObject()
                    .put("nodes", new JSONArray().put(new JSONObject().put("snippet", snippet))));


            JSONArray tmp = new JSONArray(); //Add all messages
            for(Message m : messages.values())
                tmp.put(m.toJSON());
            obj.put("messages", new JSONObject().put("nodes", tmp));

            tmp = new JSONArray(); //Add all customizations
            for(Map.Entry<String, String> nick : nicknames.entrySet())
                tmp.put(new JSONObject().put("participant_id", nick.getKey()).put("nickname", nick.getValue()));

           obj.put("customization_info", (new JSONObject().put("emoji", emoji)
                .put("outgoing_bubble_color", outgoing_bubble_color)
                .put("participant_customizations", tmp)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void updateMessages(Message... msgs){
	    for(Message m : msgs){
	        if(messages.containsKey(m.message_id))
                try {
                    messages.get(m.message_id).update(m.toJSON());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            else
                messages.put(m.message_id, m);
        }
        sortMessages();
    }
    public void sortMessages(){
        if(messages.size() <= 0)
            return;
        orderByValue(messages, new Comparator<Message>() {
                @Override
            public int compare(Message c1, Message c2) {
                return c1.compareTo(c2);
            }
        });

        updated_time_precise = ((Message) messages.values().toArray()[0]).timestamp_precise;
        snippet = ((Message) messages.values().toArray()[0]).text;
    }
    static <K, V> void orderByValue(LinkedHashMap<K, V> m, final Comparator<? super V> c) {
        List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> lhs, Map.Entry<K, V> rhs) {
                return c.compare(lhs.getValue(), rhs.getValue());
            }
        });
        m.clear();
        for(Map.Entry<K, V> e : entries) {
            m.put(e.getKey(), e.getValue());
        }
    }
	
    public String toString(){
        return toJSON().toString();
    }

    @Override
    public int compare(Conversation c1, Conversation c2) {
        return (int)(c2.updated_time_precise - c1.updated_time_precise);
    }

    @Override
    public int compareTo(@NonNull Conversation conversation) {
        return (int)(conversation.updated_time_precise - this.updated_time_precise);
    }
}
//TODO more variables
//TODO add from which account
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
                "actor":{"id":"100014199343435"}
            },
            {
                "watermark":"1521643267761",
                "action":"1521643267761",
                "actor":{"id":"100003213968599"}
            }]
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
