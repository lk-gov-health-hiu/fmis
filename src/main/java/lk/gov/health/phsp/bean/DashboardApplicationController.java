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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
import lk.gov.health.phsp.facade.FuelTransactionHistoryFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import org.joda.time.DateTimeComparator;
import org.json.JSONObject;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class DashboardApplicationController {

    @EJB
    FuelTransactionHistoryFacade encounterFacade;
    @EJB
    FuelTransactionFacade fuelTransactionFacade;

    @Inject
    ItemApplicationController itemApplicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    AreaApplicationController areaApplicationController;

//    AreaController
    @Inject
    private AreaController areaController;

    private static DecimalFormat df = new DecimalFormat("0.0");

    private List<InstitutionCount> orderingCounts;

    Boolean dashboardPrepared;

    /**
     * Creates a new instance of DashboardController
     */
    public DashboardApplicationController() {
    }

    @PostConstruct
    public void updateDashboard() {

    }

    public List<InstitutionCount> fuelOrdersByInstitution(
            Date fromDate,
            Date toDate
    ) {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.institution, sum(c.requestQuantity), sum(c.issuedQuantity)) "
                + " from FuelTransaction c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.createdAt between :fd and :td ";
        j += " group by c.institution ";
        j += " order by sum(c.requestQuantity) desc ";

        m.put("fd", fromDate);
        m.put("td", toDate);
        List<InstitutionCount> tics = fuelTransactionFacade.findLightsByJpql(j, m, TemporalType.DATE, 10);
        return tics;
    }

    public Double totalIssuedQuantity() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ret", false);
       

        String jpql = "select sum(c.issuedQuantity) "
                + "from FuelTransaction c "
                + "where (c.retired is null or c.retired = :ret) ";

        try {
            List<?> result = fuelTransactionFacade.findLightsByJpql(jpql, parameters, TemporalType.DATE);
            if (result != null && !result.isEmpty()) {
                return (Double) result.get(0); // Cast and return the first (and only) result
            }
            return 0.0; // Return 0 if the query found no matching data
        } catch (Exception e) {
            e.printStackTrace();
            return null; // or handle the exception as appropriate
        }
    }

    public List<InstitutionCount> fuelSupplyByFuelStations(
            Date fromDate,
            Date toDate
    ) {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.toInstitution, sum(c.requestQuantity), sum(c.issuedQuantity)) "
                + " from FuelTransaction c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.createdAt between :fd and :td ";
        j += " group by c.toInstitution ";
        j += " order by sum(c.requestQuantity) desc ";

        m.put("fd", fromDate);
        m.put("td", toDate);
        List<InstitutionCount> tics = fuelTransactionFacade.findLightsByJpql(j, m, TemporalType.DATE, 10);
        return tics;
    }

}
