package lk.gov.health.phsp.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.Bill;
import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.enums.BillAcceptanceStatus;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.enums.VehicleType;
import lk.gov.health.phsp.facade.BillFacade;
import lk.gov.health.phsp.facade.FuelTransactionFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.InstitutionDashboardSummary;
import lk.gov.health.phsp.pojcs.VehicleFuelEfficiency;

/**
 * Builds and caches the per-institution dashboard summary (numbers +
 * chart data) shown on dashboardInstitution.xhtml. Like the national
 * dashboard, this data only needs to be current to within a day, so
 * each institution's summary is cached for 24h, keyed by institution id.
 *
 * @author Dr M H B Ariyaratne
 */
@Named
@ApplicationScoped
public class InstitutionDashboardApplicationController {

    @EJB
    private FuelTransactionFacade fuelTransactionFacade;
    @EJB
    private BillFacade billFacade;

    private static final long CACHE_DURATION_MILLIS = 24L * 60 * 60 * 1000;
    private static final int PENDING_ISSUE_LOOKBACK_DAYS = 60;

    private final Map<Long, InstitutionDashboardSummary> summaryCache = new HashMap<>();
    private final Map<Long, Long> summaryCachedAt = new HashMap<>();

    private static final List<VehicleType> NON_VEHICLE_TYPES = Arrays.stream(VehicleType.values())
            .filter(vt -> !vt.isVehicle())
            .collect(Collectors.toList());

    /**
     * For a SpecialVehicleFuelRequest, ft.institution is overwritten to the
     * vehicle's owning institution (for fleet tracking), while ft.fromInstitution
     * stays the institution that actually requested/issued/pays for it. The
     * dashboard is about this institution's own fuel activity, so it must match
     * on whichever field reflects that - fromInstitution for special requests,
     * institution for every other transaction type.
     */
    private static final String RESPONSIBLE_INSTITUTION_MATCH
            = "((ft.transactionType = :specialType and ft.fromInstitution = :inst) "
            + "or (ft.transactionType <> :specialType and ft.institution = :inst))";

    public synchronized InstitutionDashboardSummary getSummary(Institution institution) {
        if (institution == null || institution.getId() == null) {
            return new InstitutionDashboardSummary();
        }
        Long id = institution.getId();
        Long cachedAt = summaryCachedAt.get(id);
        if (cachedAt == null || System.currentTimeMillis() - cachedAt > CACHE_DURATION_MILLIS) {
            summaryCache.put(id, buildSummary(institution));
            summaryCachedAt.put(id, System.currentTimeMillis());
        }
        return summaryCache.get(id);
    }

    private InstitutionDashboardSummary buildSummary(Institution institution) {
        InstitutionDashboardSummary summary = new InstitutionDashboardSummary();

        Date now = new Date();
        Date thisMonthStart = CommonController.startOfTheMonth();
        Date lastMonthStart = CommonController.startOfTheLastMonth();
        Date lastMonthEnd = CommonController.endOfTheLastMonth();

        summary.setRequestedThisMonth(sumRequestQuantity(institution, thisMonthStart, now));
        summary.setRequestedLastMonth(sumRequestQuantity(institution, lastMonthStart, lastMonthEnd));
        summary.setIssuedThisMonth(sumIssuedQuantity(institution, thisMonthStart, now));
        summary.setIssuedLastMonth(sumIssuedQuantity(institution, lastMonthStart, lastMonthEnd));
        Date pendingIssueSince = pendingIssueLookbackStart();
        summary.setPendingIssueQuantity(sumPendingIssueQuantity(institution, pendingIssueSince));
        summary.setPendingIssueCount(countPendingIssue(institution, pendingIssueSince));

        List<FuelTransaction> notSubmitted = findNotSubmittedForPayment(institution, lastMonthStart, lastMonthEnd);
        summary.setNotSubmittedForPaymentCount((long) notSubmitted.size());
        summary.setNotSubmittedForPaymentQuantity(notSubmitted.stream()
                .mapToDouble(ft -> ft.getIssuedQuantity() == null ? 0.0 : ft.getIssuedQuantity())
                .sum());

        List<Bill> lastMonthBills = findBills(institution, lastMonthStart, lastMonthEnd);
        summary.setRejectedCpcBillCount(countByStatus(lastMonthBills, BillAcceptanceStatus.RESUBMIT_REQUESTED));
        summary.setAcceptedCpcBillCount(countByStatus(lastMonthBills, BillAcceptanceStatus.ACCEPTED));

        List<InstitutionCount> usageThisMonth = findTop10VehiclesByUsage(institution, thisMonthStart, now);
        applyKmDriven(usageThisMonth, computeVehicleDistances(institution, thisMonthStart, now));
        summary.setTop10VehiclesByUsage(usageThisMonth);

        List<InstitutionCount> usageLastMonth = findTop10VehiclesByUsage(institution, lastMonthStart, lastMonthEnd);
        applyKmDriven(usageLastMonth, computeVehicleDistances(institution, lastMonthStart, lastMonthEnd));
        summary.setTop10VehiclesByUsageLastMonth(usageLastMonth);

        List<VehicleFuelEfficiency> efficiency = computeTop10VehicleEfficiency(institution, lastMonthStart, lastMonthEnd, summary);
        summary.setTop10VehiclesByLitersPerKm(efficiency);

        List<InstitutionCount> distanceLastMonth = computeTop10VehiclesByDistance(institution, lastMonthStart, lastMonthEnd);
        summary.setTop10VehiclesByDistanceLastMonth(distanceLastMonth);

        return summary;
    }

    private Double sumRequestQuantity(Institution institution, Date from, Date to) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("specialType", FuelTransactionType.SpecialVehicleFuelRequest);
        params.put("from", from);
        params.put("to", to);
        String jpql = "select sum(ft.requestQuantity) from FuelTransaction ft "
                + "where " + RESPONSIBLE_INSTITUTION_MATCH + " and ft.retired = false "
                + "and ft.requestedDate between :from and :to";
        return firstDoubleResult(fuelTransactionFacade.findLightsByJpql(jpql, params, TemporalType.DATE));
    }

    private Double sumIssuedQuantity(Institution institution, Date from, Date to) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("specialType", FuelTransactionType.SpecialVehicleFuelRequest);
        params.put("from", from);
        params.put("to", to);
        String jpql = "select sum(ft.issuedQuantity) from FuelTransaction ft "
                + "where " + RESPONSIBLE_INSTITUTION_MATCH + " and ft.retired = false and ft.issued = true "
                + "and ft.issuedDate between :from and :to";
        return firstDoubleResult(fuelTransactionFacade.findLightsByJpql(jpql, params, TemporalType.DATE));
    }

    private Date pendingIssueLookbackStart() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -PENDING_ISSUE_LOOKBACK_DAYS);
        return c.getTime();
    }

    private Double sumPendingIssueQuantity(Institution institution, Date since) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("specialType", FuelTransactionType.SpecialVehicleFuelRequest);
        params.put("since", since);
        String jpql = "select sum(ft.requestQuantity) from FuelTransaction ft "
                + "where " + RESPONSIBLE_INSTITUTION_MATCH + " and ft.retired = false "
                + "and ft.issued = false and ft.cancelled = false and ft.rejected = false "
                + "and ft.requestedDate >= :since";
        return firstDoubleResult(fuelTransactionFacade.findLightsByJpql(jpql, params, TemporalType.DATE));
    }

    private Long countPendingIssue(Institution institution, Date since) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("specialType", FuelTransactionType.SpecialVehicleFuelRequest);
        params.put("since", since);
        String jpql = "select count(ft) from FuelTransaction ft "
                + "where " + RESPONSIBLE_INSTITUTION_MATCH + " and ft.retired = false "
                + "and ft.issued = false and ft.cancelled = false and ft.rejected = false "
                + "and ft.requestedDate >= :since";
        return firstLongResult(fuelTransactionFacade.findLightsByJpql(jpql, params, TemporalType.DATE));
    }

    private Double firstDoubleResult(List<?> result) {
        if (result == null || result.isEmpty() || result.get(0) == null) {
            return 0.0;
        }
        return (Double) result.get(0);
    }

    private Long firstLongResult(List<?> result) {
        if (result == null || result.isEmpty() || result.get(0) == null) {
            return 0L;
        }
        return (Long) result.get(0);
    }

    private List<FuelTransaction> findNotSubmittedForPayment(Institution institution, Date from, Date to) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("specialType", FuelTransactionType.SpecialVehicleFuelRequest);
        params.put("from", from);
        params.put("to", to);
        String jpql = "select ft from FuelTransaction ft "
                + "where " + RESPONSIBLE_INSTITUTION_MATCH + " and ft.retired = false and ft.issued = true "
                + "and ft.submittedToPayment = false "
                + "and ft.issuedDate between :from and :to";
        return fuelTransactionFacade.findByJpql(jpql, params, TemporalType.DATE);
    }

    private List<Bill> findBills(Institution institution, Date from, Date to) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("from", from);
        params.put("to", to);
        String jpql = "select b from Bill b "
                + "where b.fromInstitution = :inst and b.retired = false "
                + "and b.billDate between :from and :to";
        return billFacade.findByJpql(jpql, params, TemporalType.DATE);
    }

    private Long countByStatus(List<Bill> bills, BillAcceptanceStatus status) {
        return bills.stream().filter(b -> b.getAcceptanceStatus() == status).count();
    }

    private List<InstitutionCount> findTop10VehiclesByUsage(Institution institution, Date from, Date to) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("specialType", FuelTransactionType.SpecialVehicleFuelRequest);
        params.put("excludedTypes", NON_VEHICLE_TYPES);
        params.put("from", from);
        params.put("to", to);
        String jpql = "select new lk.gov.health.phsp.pojcs.InstitutionCount(ft.vehicle, sum(ft.requestQuantity), sum(ft.issuedQuantity)) "
                + "from FuelTransaction ft "
                + "where " + RESPONSIBLE_INSTITUTION_MATCH + " and ft.retired = false and ft.vehicle is not null "
                + "and ft.vehicle.vehicleType not in :excludedTypes "
                + "and ft.requestedDate between :from and :to "
                + "group by ft.vehicle "
                + "order by sum(ft.issuedQuantity) desc";
        List<InstitutionCount> result = fuelTransactionFacade.findLightsByJpql(jpql, params, TemporalType.DATE, 10)
                .stream().map(o -> (InstitutionCount) o).collect(Collectors.toList());
        return result;
    }

    private void applyKmDriven(List<InstitutionCount> rows, Map<Long, Double> distancesByVehicleId) {
        for (InstitutionCount ic : rows) {
            ic.setKmDriven(distancesByVehicleId.get(ic.getVehicle().getId()));
        }
    }

    private Map<Long, Double> computeVehicleDistances(Institution institution, Date from, Date to) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("specialType", FuelTransactionType.SpecialVehicleFuelRequest);
        params.put("excludedTypes", NON_VEHICLE_TYPES);
        params.put("from", from);
        params.put("to", to);
        String jpql = "select ft from FuelTransaction ft "
                + "where " + RESPONSIBLE_INSTITUTION_MATCH + " and ft.retired = false and ft.vehicle is not null "
                + "and ft.vehicle.vehicleType not in :excludedTypes "
                + "and ft.odoMeterReading is not null and ft.issued = true "
                + "and ft.requestedDate between :from and :to "
                + "order by ft.vehicle.id asc, ft.requestedDate asc";
        List<FuelTransaction> readings = fuelTransactionFacade.findByJpql(jpql, params, TemporalType.DATE);

        Map<Vehicle, List<FuelTransaction>> byVehicle = new LinkedHashMap<>();
        for (FuelTransaction ft : readings) {
            byVehicle.computeIfAbsent(ft.getVehicle(), v -> new ArrayList<>()).add(ft);
        }

        Map<Long, Double> distances = new HashMap<>();
        for (Map.Entry<Vehicle, List<FuelTransaction>> entry : byVehicle.entrySet()) {
            List<FuelTransaction> txs = entry.getValue();
            if (txs.size() < 2) {
                continue;
            }
            double distance = txs.get(txs.size() - 1).getOdoMeterReading() - txs.get(0).getOdoMeterReading();
            if (distance > 0) {
                distances.put(entry.getKey().getId(), distance);
            }
        }
        return distances;
    }

    private List<VehicleFuelEfficiency> computeTop10VehicleEfficiency(Institution institution, Date from, Date to, InstitutionDashboardSummary summary) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("specialType", FuelTransactionType.SpecialVehicleFuelRequest);
        params.put("excludedTypes", NON_VEHICLE_TYPES);
        params.put("from", from);
        params.put("to", to);
        String jpql = "select ft from FuelTransaction ft "
                + "where " + RESPONSIBLE_INSTITUTION_MATCH + " and ft.retired = false and ft.vehicle is not null "
                + "and ft.vehicle.vehicleType not in :excludedTypes "
                + "and ft.odoMeterReading is not null and ft.issued = true "
                + "and ft.requestedDate between :from and :to "
                + "order by ft.vehicle.id asc, ft.requestedDate asc";
        List<FuelTransaction> readings = fuelTransactionFacade.findByJpql(jpql, params, TemporalType.DATE);

        Map<Vehicle, List<FuelTransaction>> byVehicle = new LinkedHashMap<>();
        for (FuelTransaction ft : readings) {
            byVehicle.computeIfAbsent(ft.getVehicle(), v -> new ArrayList<>()).add(ft);
        }

        List<VehicleFuelEfficiency> efficiencies = new ArrayList<>();
        int excluded = 0;
        for (Map.Entry<Vehicle, List<FuelTransaction>> entry : byVehicle.entrySet()) {
            List<FuelTransaction> txs = entry.getValue();
            if (txs.size() < 2) {
                excluded++;
                continue;
            }
            double distance = txs.get(txs.size() - 1).getOdoMeterReading() - txs.get(0).getOdoMeterReading();
            if (distance <= 0) {
                excluded++;
                continue;
            }
            double totalLiters = txs.stream()
                    .mapToDouble(ft -> ft.getIssuedQuantity() == null ? 0.0 : ft.getIssuedQuantity())
                    .sum();
            efficiencies.add(new VehicleFuelEfficiency(entry.getKey(), totalLiters, distance));
        }
        summary.setVehiclesExcludedFromEfficiencyChart(excluded);

        return efficiencies.stream()
                .sorted((a, b) -> Double.compare(b.getLitersPerKm(), a.getLitersPerKm()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<InstitutionCount> computeTop10VehiclesByDistance(Institution institution, Date from, Date to) {
        Map<String, Object> params = new HashMap<>();
        params.put("inst", institution);
        params.put("specialType", FuelTransactionType.SpecialVehicleFuelRequest);
        params.put("excludedTypes", NON_VEHICLE_TYPES);
        params.put("from", from);
        params.put("to", to);
        String jpql = "select ft from FuelTransaction ft "
                + "where " + RESPONSIBLE_INSTITUTION_MATCH + " and ft.retired = false and ft.vehicle is not null "
                + "and ft.vehicle.vehicleType not in :excludedTypes "
                + "and ft.odoMeterReading is not null and ft.issued = true "
                + "and ft.requestedDate between :from and :to "
                + "order by ft.vehicle.id asc, ft.requestedDate asc";
        List<FuelTransaction> readings = fuelTransactionFacade.findByJpql(jpql, params, TemporalType.DATE);

        Map<Vehicle, List<FuelTransaction>> byVehicle = new LinkedHashMap<>();
        for (FuelTransaction ft : readings) {
            byVehicle.computeIfAbsent(ft.getVehicle(), v -> new ArrayList<>()).add(ft);
        }

        List<InstitutionCount> rows = new ArrayList<>();
        for (Map.Entry<Vehicle, List<FuelTransaction>> entry : byVehicle.entrySet()) {
            List<FuelTransaction> txs = entry.getValue();
            if (txs.size() < 2) {
                continue;
            }
            double distance = txs.get(txs.size() - 1).getOdoMeterReading() - txs.get(0).getOdoMeterReading();
            if (distance <= 0) {
                continue;
            }
            InstitutionCount row = new InstitutionCount();
            row.setVehicle(entry.getKey());
            row.setKmDriven(distance);
            rows.add(row);
        }

        return rows.stream()
                .sorted((a, b) -> Double.compare(b.getKmDriven(), a.getKmDriven()))
                .limit(10)
                .collect(Collectors.toList());
    }
}
