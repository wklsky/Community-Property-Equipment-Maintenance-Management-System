package com.property.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.system.common.RepairOrderStatus;
import com.property.system.entity.RepairOrderFlow;
import com.property.system.exception.BusinessException;
import com.property.system.repository.RepairOrderFlowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderStateMachine {

    private final RepairOrderFlowMapper flowMapper;

    public void validateTransition(Integer fromStatus, Integer toStatus, String action) {

        List<RepairOrderFlow> flows = flowMapper.selectList(
                new LambdaQueryWrapper<RepairOrderFlow>()
                        .eq(RepairOrderFlow::getFromStatus, fromStatus));

        if (flows.isEmpty()) {
            throw new BusinessException("当前工单状态(" + getStatusName(fromStatus) +
                    ")不允许进行任何操作");
        }

        Set<Integer> allowedTargets = flows.stream()
                .map(RepairOrderFlow::getToStatus)
                .collect(Collectors.toSet());

        if (!allowedTargets.contains(toStatus)) {
            String allowedNames = flows.stream()
                    .map(f -> getStatusName(f.getToStatus()))
                    .distinct()
                    .collect(Collectors.joining("、"));
            throw new BusinessException("当前状态(" + getStatusName(fromStatus) +
                    ")不允许'" + action + "'操作，允许的操作: " + allowedNames);
        }
    }

    private String getStatusName(Integer status) {
        return RepairOrderStatus.getName(status);
    }
}
