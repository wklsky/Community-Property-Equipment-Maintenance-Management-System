package com.property.system.sms;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "aliyun")
public class AliyunSmsSender implements SmsSender {

    private final SmsProperties smsProperties;
    private Client client;

    public AliyunSmsSender(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    @PostConstruct
    public void init() throws Exception {
        Config config = new Config()
                .setAccessKeyId(smsProperties.getAccessKeyId())
                .setAccessKeySecret(smsProperties.getAccessKeySecret());
        config.endpoint = "dypnsapi.aliyuncs.com";
        this.client = new Client(config);
        log.info("阿里云短信客户端初始化完成");
    }

    @Override
    public void sendVerifyCode(String phone, String code, int validMinute) {
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setSchemeName(smsProperties.getSchemeName())
                .setCountryCode(smsProperties.getCountryCode())
                .setPhoneNumber(phone)
                .setSignName(smsProperties.getSignName())
                .setTemplateCode(smsProperties.getTemplateCode())
                .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"" + validMinute + "\"}")
                .setCodeLength((long) smsProperties.getCodeLength())
                .setValidTime((long) smsProperties.getValidTime())
                .setReturnVerifyCode(false);

        try {
            client.sendSmsVerifyCodeWithOptions(request, new RuntimeOptions());
            log.info("短信验证码发送成功: phone={}", phone);
        } catch (Exception e) {
            log.error("短信验证码发送失败: phone={}, error={}", phone, e.getMessage());
            throw new RuntimeException("短信发送失败，请稍后重试", e);
        }
    }
}
