package models;

import android.app.Activity;

public class Banner extends Activity {
    private String id;
    private String imageUrl;
    private String actionUrl;
    private int priority;

    // Constructor mặc định cần thiết cho Firebase
    public Banner() {}

    public Banner(String id, String imageUrl, String actionUrl, int priority) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.actionUrl = actionUrl;
        this.priority = priority;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
}
