package com.imcloud.saas_user.common.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum Product {
    STANDARD(17, 1, "1GB", 500000,"BASIC", false, false),
    PREMIUM(18, 2, "10GB", 700000,"BASIC", true, false),
    ENTERPRISE(18, 5, "10GB 이상", 1000000, "ADVANCED", true, true);

    private final int numOfTechniques;

    private final int numOfAccounts;

    private final String dataCapacity;

    private final int charge;

    private final String monitoringCapability;

    private final boolean isKeyProvided;

    private final boolean isEncrypted;

    Product(int numOfTechniques, int numOfAccounts, String dataCapacity, int charge,
            String monitoringCapability, boolean isKeyProvided, boolean isEncrypted) {
        this.numOfTechniques = numOfTechniques;
        this.numOfAccounts = numOfAccounts;
        this.dataCapacity = dataCapacity;
        this.charge = charge;
        this.monitoringCapability = monitoringCapability;
        this.isKeyProvided = isKeyProvided;
        this.isEncrypted = isEncrypted;
    }
}

