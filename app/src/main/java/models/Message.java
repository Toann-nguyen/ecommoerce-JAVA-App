package models;

import java.io.Serializable;

public class Message implements Serializable {
    private String senderId;
    private String receiverId;
    private String messageText;
    private long timestamp;
    private Product product;
    private String messageType;

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_PRODUCT = "product";

    public Message() {
        // Default constructor required for Firestore
        this.messageType = TYPE_TEXT;
    }

    public Message(String senderId, String receiverId, String messageText, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.messageType = TYPE_TEXT;
    }

    public Message(String senderId, String receiverId, String messageText, Product product, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.product = product;
        this.timestamp = timestamp;
        this.messageType = TYPE_PRODUCT;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isProductMessage() {
        return TYPE_PRODUCT.equals(messageType) && product != null;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            this.messageType = TYPE_PRODUCT;
        }
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
