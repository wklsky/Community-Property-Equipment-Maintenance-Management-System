package com.property.system.dto;

import lombok.Data;

@Data
public class MyPropertyVO {
    private Long id;
    private Long communityId;
    private String communityName;
    private Long buildingId;
    private String buildingName;
    private Long roomId;
    private String roomNo;
    private Integer isDefault;

    private String fullAddress;
}
