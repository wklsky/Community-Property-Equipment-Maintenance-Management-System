package com.property.system.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.system.entity.InspectionPlan;
import com.property.system.entity.InspectionTask;
import com.property.system.entity.Notice;
import com.property.system.repository.InspectionPlanMapper;
import com.property.system.repository.InspectionTaskMapper;
import com.property.system.repository.NoticeMapper;
import com.property.system.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTaskConfig {

    private final NoticeMapper noticeMapper;
    private final InspectionPlanMapper inspectionPlanMapper;
    private final InspectionTaskMapper inspectionTaskMapper;

    @Scheduled(fixedRate = 60000)
    public void publishScheduledNotices() {
        LocalDateTime now = LocalDateTime.now();

        TenantContextHolder.runWithoutTenant(() -> {

            List<Notice> notices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                    .eq(Notice::getPublishStatus, 2)
                    .le(Notice::getScheduledTime, now));

            for (Notice notice : notices) {
                notice.setPublishStatus(1);
                noticeMapper.updateById(notice);
                log.info("定时发布公告: id={}, title={}", notice.getId(), notice.getTitle());
            }
        });
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void generateInspectionTasks() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("开始检查巡检计划，当前时间: {}", now);

        TenantContextHolder.runWithoutTenant(() -> {

            List<InspectionPlan> plans = inspectionPlanMapper.selectList(
                    new LambdaQueryWrapper<InspectionPlan>()
                            .eq(InspectionPlan::getStatus, 1)
                            .le(InspectionPlan::getNextTime, now.toLocalDate())
                            .isNotNull(InspectionPlan::getNextTime)
            );

            log.debug("找到 {} 个需要生成任务的巡检计划", plans.size());

            for (InspectionPlan plan : plans) {
                try {
                    generateTaskForPlan(plan, now);
                } catch (Exception e) {
                    log.error("为计划生成任务失败: planId={}, error={}", plan.getId(), e.getMessage(), e);
                }
            }
        });
    }

    private void generateTaskForPlan(InspectionPlan plan, LocalDateTime now) {

        LocalDate today = LocalDate.now();
        Long existingTaskCount = inspectionTaskMapper.selectCount(
                new LambdaQueryWrapper<InspectionTask>()
                        .eq(InspectionTask::getPlanId, plan.getId())
                        .eq(InspectionTask::getTaskDate, today)
        );

        if (existingTaskCount > 0) {
            log.debug("计划 {} 今天已生成任务，跳过", plan.getId());

            updateNextTime(plan);
            return;
        }

        InspectionTask task = new InspectionTask();
        task.setTenantId(plan.getTenantId());
        task.setPlanId(plan.getId());
        task.setPlanName(plan.getName());
        task.setBuildingId(plan.getBuildingId());
        task.setCategoryId(plan.getCategoryId());
        task.setDeviceId(plan.getDeviceId());
        task.setTaskDate(today);
        task.setStatus(0);
        task.setCreateTime(now);
        inspectionTaskMapper.insert(task);

        log.info("自动生成巡检任务: planId={}, planName={}, taskId={}, tenantId={}",
                plan.getId(), plan.getName(), task.getId(), plan.getTenantId());

        updateNextTime(plan);
    }

    private void updateNextTime(InspectionPlan plan) {
        if (plan.getCycle() != null && plan.getCycle() > 0) {
            LocalDate nextTime = LocalDate.now().plusDays(plan.getCycle());
            plan.setNextTime(nextTime);
            inspectionPlanMapper.updateById(plan);
            log.debug("更新计划下次执行时间: planId={}, nextTime={}", plan.getId(), nextTime);
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanExpiredDraftPlans() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        TenantContextHolder.runWithoutTenant(() -> {
            List<InspectionPlan> expiredPlans = inspectionPlanMapper.selectList(
                    new LambdaQueryWrapper<InspectionPlan>()
                            .eq(InspectionPlan::getStatus, 0)
                            .lt(InspectionPlan::getCreateTime, threshold)
            );

            for (InspectionPlan plan : expiredPlans) {
                inspectionPlanMapper.deleteById(plan.getId());
                log.info("清理过期草稿计划: planId={}, name={}, createTime={}",
                        plan.getId(), plan.getName(), plan.getCreateTime());
            }

            if (!expiredPlans.isEmpty()) {
                log.info("共清理 {} 个过期草稿计划", expiredPlans.size());
            }
        });
    }
}
