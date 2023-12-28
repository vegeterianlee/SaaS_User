package com.imcloud.saas_user.common.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum Product {
    STANDARD(18, 1, 100 * 1024 * 1024L, 300000,"BASIC", false, false),
    PREMIUM(18, 2, 200 * 1024 * 1024L, 600000,"BASIC", true, false),
    ENTERPRISE(18, 5, 1024 * 1024 * 1024L, 2000000, "ADVANCED", true, true),
    FREE_PLAN(0, 1, 0L, 0, "BASIC", false, false); // 구독을 하지 않는 경우 또는 무료 플랜

    private final int numOfTechniques;

    private final int numOfAccounts;

    private final Long dataCapacity;

    private final int charge;

    private final String monitoringCapability;

    private final boolean isKeyProvided;

    private final boolean isEncrypted;

    Product(int numOfTechniques, int numOfAccounts, Long dataCapacity, int charge,
            String monitoringCapability, boolean isKeyProvided, boolean isEncrypted) {
        this.numOfTechniques = numOfTechniques;
        this.numOfAccounts = numOfAccounts;
        this.dataCapacity = dataCapacity;
        this.charge = charge;
        this.monitoringCapability = monitoringCapability;
        this.isKeyProvided = isKeyProvided;
        this.isEncrypted = isEncrypted;
    }

    public String getDisplayName() {
        return this.name();
    }
}



