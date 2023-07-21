/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
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
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lk.gov.health.phsp.enums;

/**
 * Web User Role Enum
 * 
 * Represents the roles assigned to web users.
 * 
 * @author Dr M H B Ariyaratne
 */
public enum WebUserRole {
    // National
    SYSTEM_ADMINISTRATOR("System Administrator"),
    SUPER_USER("Super User"),
    USER("User"),
    // Health Institutional Level
    INSTITUTION_ADMINISTRATOR("Institution Administrator"),
    INSTITUTION_SUPER_USER("Institution Super User"),
    INSTITUTION_USER("Institution User"),
    // CTB Level
    CTB_ADMINISTRATOR("CTB Administrator"),
    CTB_SUPER_USER("CTB Super User"),
    CTB_USER("CTB User"),
     // CPC Level
    CPC_ADMINISTRATOR("CPC Administrator"),
    CPC_SUPER_USER("CPC Super User"),
    CPC_USER("CPC User"),
     // CB Level
    CB_ADMINISTRATOR("CB Administrator"),
    CB_SUPER_USER("CB Super User"),
    CB_USER("CB User"),
     // ERD Level
    ERD_ADMINISTRATOR("ERD Administrator"),
    ERD_SUPER_USER("ERD Super User"),
    ERD_USER("ERD User"),
     // SUWASERIYA
    SUWASERIYA_ADMINISTRATOR("SUWASERIYA Administrator"),
    SUWASERIYA_SUPER_USER("SUWASERIYA Super User"),
    SUWASERIYA_USER("SUWASERIYA User"),
    // Donor Level
    DONOR_ADMINISTRATOR("Donor Administrator"),
    DONOR_SUPER_USER("Donor Super User"),
    DONOR_USER("Donor User"),
    // Auditor Level
    AUDITOR_ADMINISTRATOR("Auditor Administrator"),
    AUDITOR_SUPER_USER("Auditor Super User"),
    AUDITOR_USER("Auditor User");

    private final String label;

    private WebUserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
