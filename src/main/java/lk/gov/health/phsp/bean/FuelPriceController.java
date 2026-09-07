/*
 * The MIT License
 *
 * Copyright 2026 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.FuelPrice;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.FuelPriceFacade;

/**
 * CRUD for the fuel price schedule. Creating/editing/retiring is restricted
 * to CPC_ADMINISTRATOR and CPC_SUPER_USER - see isPriceManager().
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Named
@SessionScoped
public class FuelPriceController implements Serializable {

    @EJB
    private FuelPriceFacade ejbFacade;

    @Inject
    private WebUserController webUserController;

    @Inject
    private FuelPriceApplicationController fuelPriceApplicationController;

    private FuelPrice selected;

    public FuelPrice getSelected() {
        return selected;
    }

    public void setSelected(FuelPrice selected) {
        this.selected = selected;
    }

    public List<FuelPrice> getItems() {
        return fuelPriceApplicationController.getPrices();
    }

    public boolean isPriceManager() {
        if (webUserController.getLoggedUser() == null) {
            return false;
        }
        WebUserRole r = webUserController.getLoggedUser().getWebUserRole();
        return r == WebUserRole.CPC_ADMINISTRATOR || r == WebUserRole.CPC_SUPER_USER;
    }

    public String toListFuelPrices() {
        return "/cpc/admin/fuel_price_list";
    }

    public String toAddFuelPrice() {
        if (!isPriceManager()) {
            JsfUtil.addErrorMessage("You are not permitted to manage fuel prices");
            return "/cpc/admin/fuel_price_list";
        }
        selected = new FuelPrice();
        return "/cpc/admin/fuel_price";
    }

    public String toEditFuelPrice() {
        if (!isPriceManager()) {
            JsfUtil.addErrorMessage("You are not permitted to manage fuel prices");
            return "/cpc/admin/fuel_price_list";
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a price entry");
            return "/cpc/admin/fuel_price_list";
        }
        return "/cpc/admin/fuel_price";
    }

    public String saveFuelPrice() {
        if (!isPriceManager()) {
            JsfUtil.addErrorMessage("You are not permitted to manage fuel prices");
            return "/cpc/admin/fuel_price_list";
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to save");
            return null;
        }
        if (selected.getEffectiveFrom() == null) {
            JsfUtil.addErrorMessage("Effective From date is required");
            return null;
        }
        if (selected.getPricePerLiter() == null || selected.getPricePerLiter() <= 0) {
            JsfUtil.addErrorMessage("A valid price per liter is required");
            return null;
        }
        if (fuelPriceApplicationController.existsWithEffectiveFrom(selected.getEffectiveFrom(), selected.getId())) {
            JsfUtil.addErrorMessage("A price already exists with this Effective From date. Edit that entry instead.");
            return null;
        }

        if (selected.getId() == null) {
            selected.setCreater(webUserController.getLoggedUser());
            selected.setCreatedAt(new Date());
            ejbFacade.create(selected);
            JsfUtil.addSuccessMessage("Fuel price saved");
        } else {
            selected.setEditer(webUserController.getLoggedUser());
            selected.setEditedAt(new Date());
            ejbFacade.edit(selected);
            JsfUtil.addSuccessMessage("Fuel price updated");
        }
        fuelPriceApplicationController.resetAllPrices();
        selected = null;
        return "/cpc/admin/fuel_price_list?faces-redirect=true";
    }

    public String retireFuelPrice() {
        if (!isPriceManager()) {
            JsfUtil.addErrorMessage("You are not permitted to manage fuel prices");
            return "/cpc/admin/fuel_price_list";
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select a price entry");
            return null;
        }
        selected.setRetired(true);
        selected.setRetirer(webUserController.getLoggedUser());
        selected.setRetiredAt(new Date());
        ejbFacade.edit(selected);
        fuelPriceApplicationController.resetAllPrices();
        selected = null;
        JsfUtil.addSuccessMessage("Fuel price entry retired");
        return "/cpc/admin/fuel_price_list?faces-redirect=true";
    }

}
