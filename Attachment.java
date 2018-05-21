package com.etiaro.facebook;

import org.json.JSONException;
import org.json.JSONObject;

public class Attachment {
    enum Type{
        MessageImage,
        MessageAnimatedImage,
        MessageVideo,
        MessageFile,
        MessageAudio
    }

    public String id, filename, __typename, thumbnailUrl, previewUrl, largePreviewUrl, attachmentUrl; //Add dimensions to images - maybe new type Image?
    public Type type;
    public Attachment(JSONObject json) throws JSONException {
        update(json);
    }

    public void update(JSONObject json) throws JSONException{
        if(json.has("isCopy") && json.getBoolean("isCopy") == true){
            id = json.getString("id");
            filename = json.getString("filename");
            __typename = json.getString("__typename");
            thumbnailUrl = json.getString("thumbnailUrl");
            previewUrl = json.getString("previewUrl");
            largePreviewUrl = json.getString("largePreviewUrl");
            attachmentUrl = json.getString("attachmentUrl");
            type = Type.valueOf(json.getString("type"));
            return;
        }
        id = json.getString("legacy_attachment_id");
        filename = json.getString("filename");
        __typename = json.getString("__typename");

        switch (Type.valueOf(__typename)){
            case MessageImage:
                type = Type.MessageImage;
                thumbnailUrl = json.getJSONObject("thumbnail").getString("uri");
                previewUrl = json.getJSONObject("preview").getString("uri");
                largePreviewUrl = json.getJSONObject("large_preview").getString("uri");
                attachmentUrl = json.getJSONObject("large_preview").getString("uri");
                break;
            case MessageVideo:
                type = Type.MessageVideo;
                thumbnailUrl = json.getJSONObject("chat_image").getString("uri");
                previewUrl = json.getJSONObject("inbox_image").getString("uri");
                largePreviewUrl = json.getJSONObject("large_image").getString("uri");
                attachmentUrl = json.getString("playable_url");
                break;
        }//TODO more types
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject().put("isCopy", true)
                .put("id", id)
                .put("filename", filename)
                .put("__typename", __typename)
                .put("thumbnailUrl", thumbnailUrl)
                .put("previewUrl", previewUrl)
                .put("largePreviewUrl", largePreviewUrl)
                .put("attachmentUrl", attachmentUrl)
                .put("type", type.toString());
    }
    public String toString(){
        try {
            return toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
