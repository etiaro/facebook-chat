package com.etiaro.facebook;


import com.etiaro.facebook.Account;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class Interfaces {
    public interface LoginCallback{
        void success(Account ac);
        void fail();
        void cancelled();
    }
    public static class UserInfo{
        public String name, firstName, vanity, thumbSrc, profileUrl, gender, type;
        public boolean isFriend;
    }
    public interface UserInfoCallback{
        void success(UserInfo info);
        void fail();
        void cancelled();
    }
    //[{"thread_key":{"thread_fbid":null,"other_user_id":"100006952652301"},"name":null,"last_message":{"nodes":[{"snippet":"To pisz","message_sender":{"messaging_actor":{"id":"100003213968599"}},"timestamp_precise":"1521569805554","commerce_message_type":null,"extensible_attachment":null,"sticker":null,"blob_attachments":[]}]},"unread_count":0,"messages_count":10929,"image":null,"updated_time_precise":"1521569805554","mute_until":null,"is_pin_protected":false,"is_viewer_subscribed":true,"thread_queue_enabled":false,"folder":"INBOX","has_viewer_archived":false,"is_page_follow_up":false,"cannot_reply_reason":null,"ephemeral_ttl_mode":0,"customization_info":{"emoji":"🔫","participant_customizations":[{"participant_id":"100003213968599","nickname":"Jakub#wiktoria zuzia ...co"},{"participant_id":"100006952652301","nickname":"Mateusz#babiarz"}],"outgoing_bubble_color":null},"thread_admins":[],"approval_mode":null,"joinable_mode":{"mode":"0","link":""},"thread_queue_metadata":null,"event_reminders":{"nodes":[]},"montage_thread":{"id":"bWVzc2FnZV90aHJlYWQ6MTg5MjUwMzI1MDk5MTQxMg"},"last_read_receipt":{"nodes":[{"timestamp_precise":"1521569805554"}]},"related_page_thread":null,"rtc_call_data":{"call_state":"NO_ONGOING_CALL","server_info_data":"","initiator":null},"associated_object":null,"privacy_mode":1,"reactions_mute_mode":"REACTIONS_NOT_MUTED","mentions_mute_mode":"MENTIONS_NOT_MUTED","customization_enabled":true,"thread_type":"ONE_TO_ONE","participant_add_mode_as_string":null,"is_canonical_neo_user":false,"participants_event_status":[],"page_comm_item":null,"all_participants":{"nodes":[{"messaging_actor":{"id":"100006952652301","__typename":"User","name":"Mateusz Lorenc","gender":"MALE","url":"https:\/\/www.facebook.com\/mateusz.lorenc.79","big_image_src":{"uri":"https:\/\/scontent-waw1-1.xx.fbcdn.net\/v\/t1.0-1\/p50x50\/28577498_2023427427898993_2286512700123061608_n.jpg?_nc_cat=0&oh=428c3d0e1ff05dad5ae39ca887f5cdda&oe=5B37E2B8"},"short_name":"Mateusz","username":"mateusz.lorenc.79","is_viewer_friend":true,"is_messenger_user":true,"is_verified":false,"is_message_blocked_by_viewer":false,"is_viewer_coworker":false,"is_employee":null}},{"messaging_actor":{"id":"100003213968599","__typename":"User","name":"Kuba Klimek","gender":"MALE","url":"https:\/\/www.facebook.com\/kuba.klimek.14","big_image_src":{"uri":"https:\/\/scontent-waw1-1.xx.fbcdn.net\/v\/t1.0-1\/p50x50\/20800359_1392826077501178_2082577223295860735_n.jpg?oh=64cb4c9ab543532fda8098d8c6c63951&oe=5B47134D"},"short_name":"Kuba","username":"kuba.klimek.14","is_viewer_friend":false,"is_messenger_user":true,"is_verified":false,"is_message_blocked_by_viewer":false,"is_viewer_coworker":false,"is_employee":null}}]},"read_receipts":{"nodes":[{"watermark":"1521569805554","action":"1521569814107","actor":{"id":"100006952652301"}},{"watermark":"1521569805554","action":"1521569805554","actor":{"id":"100003213968599"}}]},"delivery_receipts":{"nodes":[{"timestamp_precise":"1521569805554"}]}},{"thread_key":{"thread_fbid":"1256202181146457","other_user_id":null},"name":"Xnxx.com","last_message":{"nodes":[{"snippet":"😦","message_sender":{"messaging_actor":{"id":"100005836111878"}},"timestamp_precise":"1521568842713","commerce_message_type":null,"extensible_attachment":null,"sticker":null,"blob_attachments":[]}]},"unread_count":0,"messages_count":40640,"image":{"uri":"https:\/\/scontent-waw1-1.xx.fbcdn.net\/v\/t34.0-12\/28233161_480585975670605_2081880811_n.jpg?_nc_cat=0&oh=d5b7e0f5fb0b4b460a095cd1786eae6d&oe=5AB3052B"},"updated_time_precise":"1521568842713","mute_until":-1,"is_pin_protected":false,"is_viewer_subscribed":true,"thread_queue_enabled":true,"folder":"INBOX","has_viewer_archived":false,"is_page_follow_up":false,"cannot_reply_reason":null,"ephemeral_ttl_mode":0,"customization_info":{"emoji":"🔥","participant_customizations":[{"participant_id":"100004998014271","nickname":"Malik Śmietana⚡💀"},{"participant_id":"100006532713776","nickname":"calineczka"},{"participant_id":"100003593825056","nickname":"penis"},{"participant_id":"100004889319416"

    public interface ThreadListCallback{
        void success(JSONArray list);
        void fail();
        void cancelled();
    }
}

