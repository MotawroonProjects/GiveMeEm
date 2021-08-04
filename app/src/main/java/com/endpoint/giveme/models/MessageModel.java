package com.endpoint.giveme.models;

import java.io.Serializable;

public class MessageModel implements Serializable {
    private int id;
    private String room_id;
    private String from_user_id;
    private String to_user_id;
    private String type;
    private String message;
    private String image;
    private String voice;
    private String is_read;
    private String date;
    private UserModel.User from_user;
    private UserModel.User to_user;
    private boolean isLoaded = false;
    private int progress = 0;
    private int max_duration =0;
    private boolean isImageLoaded = false;

    public MessageModel() {
    }

    public MessageModel(int id, String room_id, String from_user_id, String to_user_id, String type, String message, String image, String voice, String date) {
        this.id = id;
        this.room_id = room_id;
        this.from_user_id = from_user_id;
        this.to_user_id = to_user_id;
        this.type = type;
        this.message = message;
        this.image = image;
        this.voice = voice;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getRoom_id() {
        return room_id;
    }

    public String getFrom_user_id() {
        return from_user_id;
    }

    public String getTo_user_id() {
        return to_user_id;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getImage() {
        return image;
    }

    public String getVoice() {
        return voice;
    }

    public String getIs_read() {
        return is_read;
    }

    public String getDate() {
        return date;
    }

    public UserModel.User getFrom_user() {
        return from_user;
    }

    public UserModel.User getTo_user() {
        return to_user;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getMax_duration() {
        return max_duration;
    }

    public void setMax_duration(int max_duration) {
        this.max_duration = max_duration;
    }

    public boolean isImageLoaded() {
        return isImageLoaded;
    }

    public void setImageLoaded(boolean imageLoaded) {
        isImageLoaded = imageLoaded;
    }
}
