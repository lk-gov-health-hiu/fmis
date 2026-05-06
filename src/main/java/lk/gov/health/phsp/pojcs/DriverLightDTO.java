/*
 * The MIT License
 *
 * Copyright 2026 Dr M H B Ariyaratne <buddhika.ari at gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 */
package lk.gov.health.phsp.pojcs;

import java.io.Serializable;
import lk.gov.health.phsp.enums.DriverAllocationType;

/**
 * Lightweight DTO used to populate the driver autocomplete without loading
 * full Driver entities for every row.
 */
public class DriverLightDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String nic;
    private String phone;
    private DriverAllocationType allocationType;
    private Long institutionId;
    private String institutionName;

    public DriverLightDTO() {
    }

    public DriverLightDTO(Long id) {
        this.id = id;
    }

    public DriverLightDTO(Long id, String name, String nic, String phone,
            DriverAllocationType allocationType,
            Long institutionId, String institutionName) {
        this.id = id;
        this.name = name;
        this.nic = nic;
        this.phone = phone;
        this.allocationType = allocationType;
        this.institutionId = institutionId;
        this.institutionName = institutionName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public DriverAllocationType getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(DriverAllocationType allocationType) {
        this.allocationType = allocationType;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }
}
