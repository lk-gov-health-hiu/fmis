/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
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
package lk.gov.health.phsp.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.SearchFilterType;
import lk.gov.health.phsp.enums.ItemType;
import lk.gov.health.phsp.facade.ItemFacade;

/**
 *
 * @author buddhika
 */
@Named(value = "itemApplicationController")
@ApplicationScoped
public class ItemApplicationController {

    @EJB
    private ItemFacade facade;

    private List<Item> items;
    private List<ItemType> unitsTypes;
    private List<Item> packUnits;
    private List<Item> frequencyUnits;
    private List<Item> durationUnits;
    private List<Item> strengthUnits;
    private List<Item> issueUnits;

    private List<Item> investigationFilters;

    private List<Item> pcrResults;

    private List<Item> managementTypes;

    private List<Item> citizenships;

    private List<Item> sexes;

    private List<Item> minutes;
    private List<Item> symptomaticStatuses;

    private List<Item> letterReceiveCategories;
    private List<Item> letterStatuses;
    private List<Item> documentLanguages;
    private Item pcr;
    private Item rat;

    private Item pcrPositive;
    private Item pcrNegative;
    private Item pcrInconclusive;
    private Item pcrInvalid;

    /**
     * Creates a new instance of ItemApplicationController
     */
    public ItemApplicationController() {
    }

    public List<Item> completeDictionaryItem(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Dictionary_Item);
        return completeItem(its, qry);
    }

    public List<Item> completeDictionaryCategory(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Dictionary_Category);
        return completeItem(its, qry);
    }

    public List<Item> completeDictionaryItemOrCategory(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Dictionary_Item);
        its.add(ItemType.Dictionary_Category);
        return completeItem(its, qry);
    }

    public List<Item> completePharmaceuticalItem(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Atm);
        its.add(ItemType.Vtm);
        its.add(ItemType.Amp);
        its.add(ItemType.Vmp);
        its.add(ItemType.Ampp);
        its.add(ItemType.Vmpp);
        return completeItem(its, qry);
    }

    public List<Item> completeVtm(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Vtm);
        return completeItem(its, qry);
    }

    public List<Item> completeAtm(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Atm);
        return completeItem(its, qry);
    }

    public List<Item> completeVmp(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Vmp);
        return completeItem(its, qry);
    }

    public List<Item> completeAmp(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Amp);
        return completeItem(its, qry);
    }

    public List<Item> completeVmpp(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Vmpp);
        return completeItem(its, qry);
    }

    public List<Item> completeAmpp(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Ampp);
        return completeItem(its, qry);
    }

    public List<Item> findPharmaceuticalItems() {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Atm);
        its.add(ItemType.Vtm);
        its.add(ItemType.Amp);
        its.add(ItemType.Vmp);
        its.add(ItemType.Ampp);
        its.add(ItemType.Vmpp);
        return findItems(its);
    }

    public List<Item> findVtms() {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Vtm);
        return findItems(its);
    }

    public List<Item> findAtms() {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Atm);
        return findItems(its);
    }

    public List<Item> findVmps() {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Vmp);
        return findItems(its);
    }

    public List<Item> findAmps() {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Amp);
        return findItems(its);
    }

    public List<Item> findUnits() {
        return findItems(getUnitsTypes());
    }

    public List<Item> findDictionaryItems() {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Dictionary_Category);
        its.add(ItemType.Dictionary_Item);
        return findItems(its);
    }

    public List<Item> findVmpp() {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Vmpp);
        return findItems(its);
    }

    public List<Item> findAmpp(String qry) {
        List<ItemType> its = new ArrayList<>();
        its.add(ItemType.Ampp);
        return findItems(its);
    }

    public List<Item> completeItem(List<ItemType> types, String qry) {
        List<Item> tis = new ArrayList<>();
        if (qry == null || qry.trim().equals("")) {
            return tis;
        }
        if (types == null || types.isEmpty()) {
            return tis;
        }
        qry = qry.trim().toLowerCase();
        for (Item i : getItems()) {
            boolean nameOk = false;
            boolean typeOk = false;
            if (i.getName() != null && !i.getName().trim().equals("")) {
                if (i.getName().toLowerCase().contains(qry)) {
                    nameOk = true;
                }
            }
            if (i.getCode() != null && !i.getCode().trim().equals("")) {
                if (i.getCode().toLowerCase().contains(qry)) {
                    nameOk = true;
                }
            }
            if (i.getDisplayName() != null && !i.getDisplayName().trim().equals("")) {
                if (i.getDisplayName().toLowerCase().contains(qry)) {
                    nameOk = true;
                }
            }

            for (ItemType t : types) {
                if (i.getItemType().equals(t)) {
                    typeOk = true;
                }
            }

            if (nameOk && typeOk) {
                tis.add(i);
            }
        }

        return tis;
    }

    public List<Item> findItems(ItemType type) {
        List<ItemType> its = new ArrayList<>();
        its.add(type);
        return findItems(its);
    }

    public List<Item> findItems(List<ItemType> types) {
        List<Item> tis = new ArrayList<>();
        if (types == null || types.isEmpty()) {
            return tis;
        }
        for (Item i : getItems()) {
            boolean typeOk = false;
            for (ItemType t : types) {
                if (i.getItemType().equals(t)) {
                    typeOk = true;
                }
            }
            if (typeOk) {
                tis.add(i);
            }
        }
        return tis;
    }

    public Item findItemByCode(String code) {
        Item item = null;
        int counter = 0;
        for (Item ti : getItems()) {
            if (ti.getCode() != null && ti.getCode().equalsIgnoreCase(code)) {
                counter++;
                item = ti;
            }
        }
        if (counter > 1) {
        }
        return item;
    }

    public Item findItemById(Long code) {
        Item item = null;
        if (code == null) {
            return item;
        }
        for (Item ti : getItems()) {
            if (ti.getId().equals(code)) {
                item = ti;
            }
        }
        return item;
    }

    public List<Item> findItemsByCode(String code) {
        List<Item> titems = new ArrayList<>();
        for (Item ti : getItems()) {
            if (ti.getCode() != null && ti.getCode().equalsIgnoreCase(code)) {
                titems.add(ti);
            }
        }
        return titems;
    }

    public List<Item> findChildren(String code) {
        List<Item> tis = new ArrayList<>();
        for (Item ti : getItems()) {
            if (ti.getParent() != null && ti.getParent().getCode() != null && ti.getParent().getCode().equalsIgnoreCase(code)) {
                tis.add(ti);
            }
        }
        return tis;
    }

    public List<Item> findChildren(String code, InstitutionType insType) {
        List<Item> tis = new ArrayList<>();
        for (Item ti : getItems()) {
            if (ti.getParent() != null && ti.getParent().getCode() != null && ti.getParent().getCode().equalsIgnoreCase(code)) {
                if (ti.getInsType() != null) {
                    if (insType.equals(ti.getInsType())) {
                        tis.add(ti);
                    }
                } else {
                    tis.add(ti);
                }
            }
        }
        return tis;
    }

    public List<Item> findChildDictionaryItems(String code) {
        List<Item> os = new ArrayList<>();
        if (code == null || code.trim().equals("")) {
            return os;
        }
        code = code.trim().toLowerCase();
        List<Item> ns = new ArrayList<>();
        for (Item i : getItems()) {
            if (i.getParent() == null) {
                continue;
            }
            if (i.getParent().getCode().trim().equalsIgnoreCase(code)) {
                if (i.getItemType().equals(ItemType.Dictionary_Item)) {
                    os.add(i);
                }
                os.addAll(findChildDictionaryItems(i.getCode()));
            }
        }
        for (Item i : os) {
            if (!ns.contains(i)) {
                ns.add(i);
            }
        }
        Collections.sort(ns, Comparator.comparing(Item::getName));
        return ns;
    }

    public Item getMale() {
        return findItemByCode("sex_male");
    }

    public Item getFemale() {
        return findItemByCode("sex_female");
    }

    public List<Item> getItems() {
        if (items == null) {
            items = fillItems();
        }
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    private List<Item> fillItems() {
        String j = "select i "
                + "from Item i "
                + "where i.retired=:ret "
                + "order by i.displayName, i.name";
        Map m = new HashMap();
        m.put("ret", false);
        return facade.findByJpql(j, m);
    }

    public void invalidateItems() {
        items = null;
        packUnits = null;
        frequencyUnits = null;
        strengthUnits = null;
        durationUnits = null;
        issueUnits = null;
        pcrResults = null;
    }

    public List<ItemType> getUnitsTypes() {
        if (unitsTypes == null) {
            List<ItemType> its = new ArrayList<>();
            its.add(ItemType.Strength_Unit);
            its.add(ItemType.Pack_Unit);
            its.add(ItemType.Duration_Unit);
            its.add(ItemType.Frequency_Unit);
            its.add(ItemType.Issue_Unit);
            unitsTypes = its;
        }
        return unitsTypes;
    }

    public void setUnitsTypes(List<ItemType> unitsTypes) {
        this.unitsTypes = unitsTypes;
    }

    public List<Item> getFrequencyUnits() {
        if (frequencyUnits == null) {
            frequencyUnits = findItems(ItemType.Frequency_Unit);
        }
        return frequencyUnits;
    }

    public List<Item> getDurationUnits() {
        if (durationUnits == null) {
            durationUnits = findItems(ItemType.Duration_Unit);
        }
        return durationUnits;
    }

    public List<Item> getStrengthUnits() {
        if (strengthUnits == null) {
            strengthUnits = findItems(ItemType.Strength_Unit);
        }
        return strengthUnits;
    }

    public List<Item> getIssueUnits() {
        if (issueUnits == null) {
            issueUnits = findItems(ItemType.Issue_Unit);
        }
        return issueUnits;
    }

    public List<Item> getPcrResults() {
        if (pcrResults == null) {
            pcrResults = findChildDictionaryItems("pcr_results");
        }
        return pcrResults;
    }

    public List<Item> getManagementTypes() {
        if (managementTypes == null) {
            managementTypes = findChildDictionaryItems("categories_for_the_placement_of_diagnosed_patient");
        }
        return managementTypes;
    }

    public List<Item> getPackUnits() {
        if (packUnits == null) {
            packUnits = findItems(ItemType.Pack_Unit);
        }
        return packUnits;
    }

    public Item getPcrPositive() {
        if (pcrPositive == null) {
            pcrPositive = findItemByCode("pcr_positive");
        }
        return pcrPositive;
    }

    public void setPcrPositive(Item pcrPositive) {
        this.pcrPositive = pcrPositive;
    }

    public Item getPcrNegative() {
        if (pcrNegative == null) {
            pcrNegative = findItemByCode("pcr_negative");
        }
        return pcrNegative;
    }

    public void setPcrNegative(Item pcrNegative) {
        this.pcrNegative = pcrNegative;
    }

    public Item getPcrInconclusive() {
        if (pcrInconclusive == null) {
            pcrInconclusive = findItemByCode("pcr_inconclusive");
        }
        return pcrInconclusive;
    }

    public void setPcrInconclusive(Item pcrInconclusive) {
        this.pcrInconclusive = pcrInconclusive;
    }

    public Item getPcrInvalid() {
        if (pcrInvalid == null) {
            pcrInvalid = findItemByCode("pcr_invalid");
        }
        return pcrInvalid;
    }

    public void setPcrInvalid(Item pcrInvalid) {
        this.pcrInvalid = pcrInvalid;
    }

    public Item getPcr() {
        if (pcr == null) {
            pcr = findItemByCode("covid19_pcr_test");
        }
        return pcr;
    }

    public void setPcr(Item pcr) {
        this.pcr = pcr;
    }

    public Item getRat() {
        if (rat == null) {
            rat = findItemByCode("covid19_rat");
        }
        return rat;
    }

    public void setRat(Item rat) {
        this.rat = rat;
    }

    public List<Item> getInvestigationFilters() {
        List<Item> output = new ArrayList<>();
        for (SearchFilterType filter : SearchFilterType.values()) {
            String code = filter.getCode();
            String label = filter.getLabel();
            Item item = new Item();
            item.setCode(code);
            item.setName(label);
            output.add(item);
        }
        this.investigationFilters = output;
        return this.investigationFilters;
    }

    public void setInvestigationFilters(List<Item> investigationFilters) {
        this.investigationFilters = investigationFilters;
    }

    public List<Item> getLetterReceiveCategories() {
        if (letterReceiveCategories == null) {
            letterReceiveCategories = findChildDictionaryItems("letter_receive_category");
        }
        return letterReceiveCategories;
    }

    public void setLetterReceiveCategories(List<Item> letterReceiveCategories) {
        this.letterReceiveCategories = letterReceiveCategories;
    }

    public List<Item> getDocumentLanguages() {
        if (documentLanguages == null) {
            documentLanguages = findChildDictionaryItems("document_language");
        }
        return documentLanguages;
    }

    public void setDocumentLanguages(List<Item> documentLanguages) {
        this.documentLanguages = documentLanguages;
    }

    public List<Item> getCitizenships() {
        if (citizenships == null) {
            citizenships = findChildDictionaryItems("citizenship");
        }
        return citizenships;
    }

    public List<Item> getSexes() {
        if (sexes == null) {
            sexes = findChildDictionaryItems("sex");
        }
        return sexes;
    }

    public List<Item> getMinutes() {
        if (minutes == null) {
            minutes = findChildDictionaryItems("letter_minute");
        }
        return minutes;
    }

    public void setMinutes(List<Item> minutes) {
        this.minutes = minutes;
    }

    public List<Item> getSymptomaticStatuses() {
        if (symptomaticStatuses == null) {
            symptomaticStatuses = findChildDictionaryItems("symptomatic_statuses");
        }
        return symptomaticStatuses;
    }

    public void setSymptomaticStatuses(List<Item> symptomaticStatuses) {
        this.symptomaticStatuses = symptomaticStatuses;
    }

    public List<Item> getLetterStatuses() {
        if (letterStatuses == null) {
            letterStatuses = findChildDictionaryItems("letter_statuses");
        }
        return letterStatuses;
    }

    public void setLetterStatuses(List<Item> letterStatuses) {
        this.letterStatuses = letterStatuses;
    }

}
