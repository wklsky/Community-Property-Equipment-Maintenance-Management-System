package com.property.system.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.property.system.common.ResultCode;
import com.property.system.entity.Device;
import com.property.system.exception.BusinessException;
import com.property.system.repository.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/v1/qrcode")
@RequiredArgsConstructor
public class QrCodeController {

    private final DeviceMapper deviceMapper;

    @GetMapping(value = "/device/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] deviceQrCode(@PathVariable Long id,
            @RequestParam(defaultValue = "200") int width,
            @RequestParam(defaultValue = "200") int height) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_FOUND);
        }

        String qrContent = "DEVICE:" + device.getId() + ":" + device.getTenantId();
        if (device.getName() != null) {
            qrContent += ":" + device.getName();
        }

        QrConfig config = new QrConfig(width, height);
        config.setMargin(1);

        BufferedImage image = QrCodeUtil.generate(qrContent, config);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "二维码生成失败");
        }
    }
}
