package com.example.selma.homebudget;

public class Notification {
    private String notificationMessage, senderUserEmail;

    public Notification(){}

    public Notification(String notificationMessage, String senderUserEmail) {
        this.notificationMessage = notificationMessage;
        this.senderUserEmail = senderUserEmail;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }
    public String getsenderUserEmail() {
        return senderUserEmail;
    }
}
