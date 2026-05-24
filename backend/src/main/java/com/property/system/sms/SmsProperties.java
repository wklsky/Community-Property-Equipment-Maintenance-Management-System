package com.property.system.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sms.aliyun")
public class SmsProperties {

    private String accessKeyId;

    private String accessKeySecret;

    private String signName;

    private String templateCode = "100001";

    private String schemeName = "验证码方案";

    private String countryCode = "86";

    private int codeLength = 6;

    private int validTime = 300;
}
