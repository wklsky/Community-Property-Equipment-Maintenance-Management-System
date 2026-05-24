package com.property.system.sms;

public interface SmsSender {

    void sendVerifyCode(String phone, String code, int validMinute);
}
