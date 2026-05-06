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
import lk.gov.health.phsp.enums.VehicleAllocationType;

/**
 * Lightweight DTO used to populate the vehicle autocomplete without loading
 * full Vehicle entities for thousands of rows.
 */
public class VehicleLightDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String vehicleNumber;
    private String name;
    private VehicleAllocationType allocationType;
    private Long institutionId;
    private String institutionName;

    public VehicleLightDTO() {
    }

    public VehicleLightDTO(Long id) {
        this.id = id;
    }

    public VehicleLightDTO(Long id, String vehicleNumber, String name,
            VehicleAllocationType allocationType,
            Long institutionId, String institutionName) {
        this.id = id;
        this.vehicleNumber = vehicleNumber;
        this.name = name;
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

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VehicleAllocationType getAllocationType() {
        return allocationType;
    }

    public void setAllocationType(VehicleAllocationType allocationType) {
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
