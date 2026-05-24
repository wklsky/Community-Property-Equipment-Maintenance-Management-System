package com.property.system.dto;

import lombok.Data;

@Data
public class MyAddressVO {
    private Long id;
    private String type;          // "property" or "custom"
    private String address;       // full address string
    private String communityName;
    private String buildingName;
    private String roomNo;
    private Integer isDefault;
}
