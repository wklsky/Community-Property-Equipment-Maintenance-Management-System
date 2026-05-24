package com.property.system.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsSender implements SmsSender {

    @Override
    public void sendVerifyCode(String phone, String code, int validMinute) {
        log.info("【模拟短信】手机号: {}, 验证码已生成 ({}分钟内有效)", phone, validMinute);
    }
}
