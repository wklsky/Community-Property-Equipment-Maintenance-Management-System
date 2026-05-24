package com.property.system.service;

import com.property.system.exception.BusinessException;
import com.property.system.sms.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SmsService {

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_SECONDS = 300;
    private static final int SEND_INTERVAL_SECONDS = 60;

    private final SmsSender smsSender;
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();
    private final Map<String, Long> lastSendTime = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public SmsService(SmsSender smsSender) {
        this.smsSender = smsSender;
    }

    public String generateCode(String phone, Long tenantId) {
        String key = buildKey(phone, tenantId);

        Long lastTime = lastSendTime.get(key);
        if (lastTime != null && System.currentTimeMillis() - lastTime < SEND_INTERVAL_SECONDS * 1000L) {
            long remainSeconds = SEND_INTERVAL_SECONDS - (System.currentTimeMillis() - lastTime) / 1000;
            throw new BusinessException("请" + remainSeconds + "秒后再获取验证码");
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        codeStore.put(key, new CodeEntry(code, System.currentTimeMillis() + CODE_EXPIRE_SECONDS * 1000L));
        lastSendTime.put(key, System.currentTimeMillis());

        smsSender.sendVerifyCode(phone, code, CODE_EXPIRE_SECONDS / 60);

        return code;
    }

    public boolean verifyCode(String phone, Long tenantId, String code) {
        String key = buildKey(phone, tenantId);
        CodeEntry entry = codeStore.get(key);

        if (entry == null) {
            throw new BusinessException("验证码不存在或已使用，请重新获取");
        }

        if (System.currentTimeMillis() > entry.expireAt) {
            codeStore.remove(key);
            throw new BusinessException("验证码已过期，请重新获取");
        }

        if (!entry.code.equals(code)) {
            throw new BusinessException("验证码错误");
        }

        codeStore.remove(key);
        lastSendTime.remove(key);
        return true;
    }

    public boolean checkCodeValid(String phone, Long tenantId, String code) {
        String key = buildKey(phone, tenantId);
        CodeEntry entry = codeStore.get(key);
        if (entry == null) {
            throw new BusinessException("验证码不存在或已使用，请重新获取");
        }
        if (System.currentTimeMillis() > entry.expireAt) {
            codeStore.remove(key);
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!entry.code.equals(code)) {
            throw new BusinessException("验证码错误");
        }
        return true;
    }

    public void consumeCode(String phone, Long tenantId) {
        String key = buildKey(phone, tenantId);
        codeStore.remove(key);
        lastSendTime.remove(key);
    }

    @Scheduled(fixedRate = 60000)
    public void cleanExpiredCodes() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, CodeEntry>> it = codeStore.entrySet().iterator();
        int removed = 0;
        while (it.hasNext()) {
            Map.Entry<String, CodeEntry> entry = it.next();
            if (now > entry.getValue().expireAt) {
                it.remove();
                removed++;
            }
        }

        lastSendTime.entrySet().removeIf(e -> !codeStore.containsKey(e.getKey()));
        if (removed > 0) {
            log.debug("清理了 {} 个过期验证码", removed);
        }
    }

    private String buildKey(String phone, Long tenantId) {
        return tenantId + ":" + phone;
    }

    private static class CodeEntry {
        final String code;
        final long expireAt;

        CodeEntry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }
}
