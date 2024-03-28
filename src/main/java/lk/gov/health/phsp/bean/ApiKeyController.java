/*
 * The MIT License
 *
 * Copyright 2024 Dr M H B Ariyaratne <buddhika.ari at gmail.com>.
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

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.facade.ApiKeyFacade;
import lk.gov.health.phsp.pojcs.ApiKeyType;

/**
 *
 * @author Dr M H B Ariyaratne <buddhika.ari at gmail.com>
 */
@Named
@SessionScoped
public class ApiKeyController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    WebUserController sessionController;
    @EJB
    private ApiKeyFacade ejbFacade;
    private ApiKey current;
    private ApiKey removing;
    private List<ApiKey> items = null;

    public String toManageMyApiKeys() {
        listMyApiKeys();
        return "/user_api_key";
    }

    public ApiKeyType[] getApiKeyTypes() {
        return ApiKeyType.values();
    }

    public void listMyApiKeys() {
        String j;
        j = "select a "
                + " from ApiKey a "
                + " where a.retired=false "
                + " and a.webUser=:wu "
                + " and a.dateOfExpiary > :ed "
                + " order by a.dateOfExpiary";
        Map m = new HashMap();
        m.put("wu", sessionController.getLoggedUser());
        m.put("ed", new Date());
        items = getFacade().findByJpql(j, m, TemporalType.DATE);
    }

    public ApiKey findApiKey(String keyValue) {
        System.out.println("findApiKey = " );
        String j;
        j = "select a "
                + " from ApiKey a "
                + " where a.keyValue=:kv";
        Map m = new HashMap();
        m.put("kv", keyValue);
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        return getFacade().findFirstByJpql(j, m);
    }

    public void prepareAdd() {
        createNewApiKey();
    }

    public void createNewApiKey() {
        UUID uuid = UUID.randomUUID();
        current = new ApiKey();
        current.setWebUser(sessionController.getLoggedUser());
        current.setInstitution(sessionController.getInstitution());
        current.setKeyType(ApiKeyType.SUWASERIYA);
        current.setKeyValue(uuid.toString());
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, 1);
        current.setDateOfExpiary(c.getTime());
    }

    private void recreateModel() {
        items = null;
    }

    public void saveSelected() {
        if (getCurrent().getKeyValue() == null || getCurrent().getKeyValue().isEmpty()) {
            JsfUtil.addErrorMessage("Please enter Key Value");
            return;
        }

        if (getCurrent().getId() != null && getCurrent().getId() > 0) {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage("Updated Successfully.");
        } else {
            current.setCreatedAt(new Date());
            current.setCreater(sessionController.getLoggedUser());
            getFacade().create(current);
            JsfUtil.addSuccessMessage("Saved Successfully");
        }
        recreateModel();
        listMyApiKeys();
        setCurrent(null);
        getCurrent();
    }

    public ApiKeyFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(ApiKeyFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public ApiKeyController() {
    }

    public ApiKey getCurrent() {
        if (current == null) {
            createNewApiKey();
        }
        return current;
    }

    public void setCurrent(ApiKey current) {
        this.current = current;
    }

    public void delete() {
        if (removing != null) {
            removing.setRetired(true);
            removing.setRetiredAt(new Date());
            removing.setRetirer(sessionController.getLoggedUser());
            getFacade().edit(removing);
            JsfUtil.addSuccessMessage("Removed Successfully");
        } else {
            JsfUtil.addSuccessMessage("Nothing to Delete");
        }
        recreateModel();
        listMyApiKeys();
        removing = null;
    }

    private ApiKeyFacade getFacade() {
        return ejbFacade;
    }

    public List<ApiKey> getItems() {
        return items;
    }

    public ApiKey getRemoving() {
        return removing;
    }

    public void setRemoving(ApiKey removing) {
        this.removing = removing;
    }

    /**
     *
     */
    @FacesConverter(forClass = ApiKey.class)
    public static class ApiKeyConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ApiKeyController controller = (ApiKeyController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "apiKeyController");
            return controller.getEjbFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof ApiKey) {
                ApiKey o = (ApiKey) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type "
                        + object.getClass().getName() + "; expected type: " + ApiKeyController.class.getName());
            }
        }
    }

}
