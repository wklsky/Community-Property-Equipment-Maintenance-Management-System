package com.property.system.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.property.system.common.ResultCode;
import com.property.system.dto.Result;
import com.property.system.entity.BizFile;
import com.property.system.exception.BusinessException;
import com.property.system.repository.BizFileMapper;
import com.property.system.security.RequireRole;
import com.property.system.util.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final BizFileMapper bizFileMapper;

    @Value("${app.upload-dir:./upload}")
    private String uploadDir;

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    ));

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @RequireRole({"系统管理员", "维修工", "业主"})
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "仅支持 JPG、PNG、GIF、WebP、BMP 格式的图片");
        }

        try {

            String dateDir = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            File dir = new File(uploadDir, dateDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String originalName = file.getOriginalFilename();
            String ext = FileUtil.extName(originalName != null ? originalName : "jpg");
            String newFileName = IdUtil.fastSimpleUUID() + "." + ext;

            File dest = new File(dir, newFileName);
            file.transferTo(dest);

            String relativePath = "/upload/" + dateDir + "/" + newFileName;
            String url = "/api/v1/files/static" + relativePath;

            BizFile bizFile = new BizFile();
            bizFile.setTenantId(RequestContext.getTenantId());
            bizFile.setFileType(ext);
            bizFile.setUrl(url);
            bizFile.setCreateTime(LocalDateTime.now());
            bizFileMapper.insert(bizFile);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("url", url);
            result.put("fileId", bizFile.getId());
            result.put("fileName", originalName);

            log.info("文件上传成功: {} -> {}", originalName, url);
            return Result.success(result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败，请重试");
        }
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @PostMapping("/{fileId}/bind")
    public Result<Void> bindBiz(
            @PathVariable Long fileId,
            @RequestParam String bizType,
            @RequestParam Long bizId) {
        BizFile bizFile = bizFileMapper.selectById(fileId);
        if (bizFile == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        bizFile.setBizType(bizType);
        bizFile.setBizId(bizId);
        bizFileMapper.updateById(bizFile);
        return Result.success();
    }

    @RequireRole({"系统管理员", "维修工", "业主"})
    @GetMapping("/biz")
    public Result<List<BizFile>> listByBiz(
            @RequestParam String bizType,
            @RequestParam Long bizId) {
        return Result.success(bizFileMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizFile>()
                        .eq(BizFile::getBizType, bizType)
                        .eq(BizFile::getBizId, bizId)
                        .orderByDesc(BizFile::getCreateTime)));
    }
}
