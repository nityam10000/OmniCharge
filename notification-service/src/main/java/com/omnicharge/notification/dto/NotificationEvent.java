package com.omnicharge.notification.dto;

public class NotificationEvent {

    // ✅ Existing fields (keep for backward compatibility)
    private String message;
    private String email;
    private String phoneNumber;
    private String type;
    private String subject;

    // 🔥 NEW fields (for proper recharge email UI)
    private String rechargeId;
    private String mobile;
    private String operator;
    private double amount;
    private String date;

    // ✅ Default Constructor
    public NotificationEvent() {}

    // ✅ Old Constructor (DO NOT REMOVE - warna existing flow toot jayega)
    public NotificationEvent(String message, String email, String phoneNumber, String type) {
        this.message = message;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.type = type;
    }

    // 🔥 New Constructor (use this going forward)
    public NotificationEvent(String email, String phoneNumber, String type,
                             String rechargeId, String mobile,
                             String operator, double amount, String date) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.type = type;
        this.rechargeId = rechargeId;
        this.mobile = mobile;
        this.operator = operator;
        this.amount = amount;
        this.date = date;
    }

    // ================= GETTERS =================

    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getType() {
        return type;
    }

    public String getSubject() {
        return subject;
    }

    public String getRechargeId() {
        return rechargeId;
    }

    public String getMobile() {
        return mobile;
    }

    public String getOperator() {
        return operator;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    // ================= SETTERS =================

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setRechargeId(String rechargeId) {
        this.rechargeId = rechargeId;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(String date) {
        this.date = date;
    }
}