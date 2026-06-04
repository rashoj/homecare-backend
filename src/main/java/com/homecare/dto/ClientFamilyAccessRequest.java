package com.homecare.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientFamilyAccessRequest {

    private Long clientId;

    private Long familyUserId;

    private String relationship;
}