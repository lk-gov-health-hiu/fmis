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
package lk.gov.health.phsp.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import javax.enterprise.context.Dependent;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import lk.gov.health.phsp.bean.AnalysisController;

import lk.gov.health.phsp.bean.ApplicationController;
import lk.gov.health.phsp.bean.AreaApplicationController;

import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.bean.EncounterApplicationController;
import lk.gov.health.phsp.bean.InstitutionApplicationController;
import lk.gov.health.phsp.bean.ItemApplicationController;
import lk.gov.health.phsp.bean.SessionController;

import lk.gov.health.phsp.bean.WebUserApplicationController;
import lk.gov.health.phsp.entity.Area;

import lk.gov.health.phsp.entity.FuelTransaction;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;

import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.InstitutionType;

import org.json.JSONArray;
import org.json.JSONObject;

import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACSigner;
import io.fusionauth.jwt.hmac.HMACVerifier;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import lk.gov.health.phsp.bean.ApiKeyController;
import lk.gov.health.phsp.bean.DriverController;
import lk.gov.health.phsp.bean.FuelRequestAndIssueController;
import lk.gov.health.phsp.bean.VehicleController;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.entity.Driver;
import lk.gov.health.phsp.entity.Vehicle;
import lk.gov.health.phsp.enums.FuelTransactionType;
import lk.gov.health.phsp.enums.VehiclePurpose;
import lk.gov.health.phsp.enums.VehicleType;
import org.json.JSONException;

/**
 * REST Web Service
 *
 * @author buddhika
 */
@Path("api")
@Dependent
public class ApiResource {

    @Context
    private UriInfo context;

    @Inject
    AreaApplicationController areaApplicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    AnalysisController analysisController;
    @Inject
    ItemApplicationController itemApplicationController;
    @Inject
    ApplicationController applicationController;
    @Inject
    ApiKeyController apiKeyController;
    @Inject
    DriverController driverController;
    @Inject
    VehicleController vehicleController;

    @Inject
    WebUserApplicationController webUserApplicationController;

    @Inject
    EncounterApplicationController encounterApplicationController;
    @Inject
    SessionController sessionController;
    @Inject
    FuelRequestAndIssueController fuelRequestAndIssueController;

    WebUser webUser;

    /**
     * Creates a new instance of GenericResource
     */
    public ApiResource() {
    }

    @Path("/checkApi")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkApi() {
        return Response.ok("API is functioning properly.").build();
    }

    @GET
    @Path("/institution")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listInstitutions() {
        List<Institution> institutions = institutionApplicationController.getInstitutions();
        if (institutions == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Institutions not found").build();
        }
        JSONArray responseArray = new JSONArray();

        for (Institution institution : institutions) {
            JSONObject institutionData = new JSONObject();
            institutionData.put("name", institution.getName());
            institutionData.put("code", institution.getCode());
            if (institution.getInstitutionType() != null && institution.getInstitutionType().getLabel() != null) {
                institutionData.put("type", institution.getInstitutionType().getLabel());
            }
            responseArray.put(institutionData);
        }

        return Response.ok(responseArray.toString()).build();
    }

    @GET
    @Path("/fuel_station")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFuelStations() {
        List<Institution> institutions = institutionApplicationController.getFuelStations();
        if (institutions == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Fuel stations not found").build();
        }
        JSONArray responseArray = new JSONArray();

        for (Institution institution : institutions) {
            JSONObject institutionData = new JSONObject();
            institutionData.put("name", institution.getName());
            institutionData.put("code", institution.getCode());
            // Checking null for district and its name
            if (institution.getDistrict() != null && institution.getDistrict().getName() != null && !institution.getDistrict().getName().trim().equals("")) {
                institutionData.put("district", institution.getDistrict().getName());
            }
            responseArray.put(institutionData);
        }

        return Response.ok(responseArray.toString()).build();
    }

    @POST
    @Path("/diesel_received")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDieselReceivedRecord(@Context HttpServletRequest requestContext, String jsonRequest) {
        System.out.println("createDieselReceivedRecord");
        System.out.println("jsonRequest = " + jsonRequest);
        String apiKey = requestContext.getHeader("SUWASERIYA");
        System.out.println("apiKey = " + apiKey);
        if (!isValidKey(apiKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid or missing API key.").build();
        }
        try {
            JSONObject requestObject = new JSONObject(jsonRequest);
            Institution fuelStation;
            Institution suwaseriya;
            Driver driver;
            Vehicle vehicle;
            String fuelStationCode = requestObject.optString("fuelStationCode");

            if (fuelStationCode == null || fuelStationCode.trim().equals("")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No Fuel Station Code.").build();
            }

            fuelStation = institutionApplicationController.findInstitutionByCode(fuelStationCode);

            if (fuelStation == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No Fuel Station Code.").build();
            }

            suwaseriya = institutionApplicationController.findInstitutionByType(InstitutionType.Suwa_Sariya);
            if (suwaseriya == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No Suwa Seriya Instituion Found. Contact System Administrator").build();
            }

            String vehicleNumber = requestObject.optString("vehicleNumber");
            if (vehicleNumber == null || vehicleNumber.trim().equals("")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No Vehicle Number Found. Ex CAM 5872").build();
            }
            vehicle = vehicleController.findVehicleByNumber(vehicleNumber);
            if (vehicle == null) {
                vehicle = new Vehicle();
                vehicle.setInstitution(suwaseriya);
                vehicle.setCreater(webUser);
                vehicle.setVehicleNumber(vehicleNumber);
                vehicle.setVehicleType(VehicleType.Ambulance);
                vehicle.setVehiclePurpose(VehiclePurpose.Ambulance);
                vehicleController.save(vehicle);
            }

            String driverNic = requestObject.optString("driverNic");
            if (driverNic == null || driverNic.trim().equals("")) {
                return Response.status(Response.Status.BAD_REQUEST).entity("No Driver Code Found.").build();
            }

            driver = driverController.findDriverByNic(driverNic);
            if (driver == null) {
                driver = new Driver();
                driver.setAddress(suwaseriya.getAddress());
                driver.setCreatedAt(new Date());
                driver.setCreater(webUser);
                driver.setInstitution(suwaseriya);
                driver.setName(driverNic);
                driver.setPhone(driverNic);
                driverController.save(driver);
            }

            String comments = requestObject.optString("comments");

            Double requestQuantity = requestObject.optDouble("requestQuantity");
            if (requestQuantity < 1) {
                return Response.status(Response.Status.ACCEPTED).entity("Vehicle and Driver added. No fuel transaction found.").build();
            }

            Double issuedQuantity = requestObject.optDouble("issuedQuantity");
            Double odoMeterReading = requestObject.optDouble("odoMeterReading");

            String requestReferenceNumber = requestObject.optString("requestReferenceNumber");
            String issueReferenceNumber = requestObject.optString("issueReferenceNumber");

            String transactionId = createFuelTransaction(
                    fuelStation,
                    suwaseriya,
                    vehicle,
                    driver,
                    comments,
                    requestQuantity,
                    issuedQuantity,
                    odoMeterReading,
                    requestReferenceNumber,
                    issueReferenceNumber,
                    webUser);

            // Return a success response
            JSONObject responseObject = new JSONObject();
            responseObject.put("message", "Diesel receipt record created successfully");
            responseObject.put("transactionId", transactionId);
            return Response.ok(responseObject.toString()).build();

        } catch (JSONException e) {
            // Handle JSON parsing errors
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid JSON format").build();
        } catch (Exception e) {
            // Handle other exceptions
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error creating diesel receipt record: " + e.getMessage()).build();
        }
    }

    private boolean isValidKey(String key) {
        System.out.println("isValidKey method ");
        System.out.println("key = " + key);
        if (key == null || key.trim().equals("")) {
            return false;
        }
        ApiKey k = apiKeyController.findApiKey(key);
        System.out.println("k = " + k);
        if (k == null) {
            return false;
        }
        System.out.println("k.getWebUser() = " + k.getWebUser());
        if (k.getWebUser() == null) {
            return false;
        } else {
            webUser = k.getWebUser();
        }
        System.out.println("k.getWebUser().isRetired() = " + k.getWebUser().isRetired());
        if (k.getWebUser().isRetired()) {
            return false;
        }
        System.out.println("k.getDateOfExpiary() = " + k.getDateOfExpiary());
        if (k.getDateOfExpiary().before(new Date())) {
            return false;
        }
        return true;
    }

    private JSONObject authenticate(String username, String password) {

        if (username == null || username.trim().length() == 0) {
            return errorMessageLogin();
        }

        if (password == null || password.trim().length() == 0) {
            return errorMessageLogin();
        }

        WebUser wu = webUserApplicationController.getWebUser(username, password);

        if (wu == null) {
            return errorMessageLogin();
        }

        sessionController.setAppKey();
        UUID key = sessionController.getAppKey();
        Signer signer = HMACSigner.newSHA256Signer(key.toString());

        JWT jwt = new JWT();
        jwt.setIssuer("nchis.health.gov.lk");
        jwt.setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC));
        jwt.setSubject(username);
        // Token is set to be valid for 60 minutes
        jwt.setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(60));

        String encodedJwt = JWT.getEncoder().encode(jwt, signer);

        JSONObject json = new JSONObject();
        json.put("api_key", encodedJwt);
        json.put("status", 200);
        json.put("message", "success");
        return json;
    }

    private boolean authorize(String username, String jwt) {
        if (jwt == null) {
            return false;
        }

        if (username == null || username.trim().length() == 0) {
            return false;
        }

        if (sessionController.getAppKey() == null) {
            return false;
        }
        try {
            Verifier verifier = HMACVerifier.newVerifier(sessionController.getAppKey().toString());
            JWT decodeJwt = JWT.getDecoder().decode(jwt, verifier);

            if (username.equals(decodeJwt.subject)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }

    private JSONObject requestPcrResult(String ipadd,
            String username,
            String password,
            String request_id) {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();

        WebUser wu;

        wu = webUserApplicationController.getWebUser(username, password);

        if (wu == null) {
            return errorMessageLogin();
        }

        if (wu.getLoginIPs() == null || wu.getLoginIPs().equals("")) {
            return errorMessageNoIps();
        }

        if (ipadd != null && !wu.getLoginIPs().contains(ipadd)) {
            return errorMessageNotAnAutherizedIp();
        }

        if (request_id == null || request_id.trim().equals("")) {
            return errorMessageNoPcrRequestId();
        }

        Long rid = CommonController.getLongValue(request_id);

        FuelTransaction e = encounterApplicationController.getEncounter(rid);

        if (e == null) {
            return errorMessageNoSuchPcrRequestId();
        }

        JSONObject ja = new JSONObject();

        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject submitPcrResult() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();

        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject submitRatResult() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();

        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject districtList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.District);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("district_id", a.getId());
            ja.put("district_code", a.getCode());
            ja.put("district_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject instituteList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Institution> ds = institutionApplicationController.getHospitals();
        for (Institution a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("institute_id", a.getId());
            ja.put("institute_code", a.getCode());
            ja.put("name", a.getName());
            ja.put("hin", a.getPoiNumber());
            ja.put("address", a.getAddress());
            ja.put("type", a.getInstitutionType());
            ja.put("type_label", a.getInstitutionType().getLabel());
            if (a.getEditedAt() != null) {
                ja.put("edited_at", a.getEditedAt());
            } else {
                ja.put("edited_at", a.getCreatedAt());
            }
            if (a.getProvince() != null) {
                ja.put("province_id", a.getProvince().getId());
            }
            if (a.getDistrict() != null) {
                ja.put("district_id", a.getDistrict().getId());
            }
            ja.put("child_institutions", Get_Child_Institutions(a));
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject genderList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getSexes();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("gender_id", a.getId());
            ja.put("gender_name", a.getCode());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject vaccinationStatusesList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getMinutes();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("vaccination_status_id", a.getId());
            ja.put("vaccination_status_name", a.getCode());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject symptomaticStatusesList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getSymptomaticStatuses();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("symptomatic_status_id", a.getId());
            ja.put("symptomatic_status_name", a.getCode());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject gnAreaList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.GN);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("gn_id", a.getId());
            ja.put("gn_code", a.getCode());
            ja.put("gn_name", a.getCode());
            if (a.getDistrict() != null) {
                ja.put("district_name", a.getDistrict().getName());
                ja.put("district_id", a.getDistrict().getId());
            }
            if (a.getDsd() != null) {
                ja.put("dsd_name", a.getDsd().getName());
                ja.put("dsd_id", a.getDsd().getId());
            }
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject phiAreaList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.GN);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("phi_id", a.getId());
            ja.put("phi_name", a.getCode());
            if (a.getDistrict() != null) {
                ja.put("district_name", a.getDistrict().getName());
                ja.put("district_id", a.getDistrict().getId());
            }
            if (a.getMoh() != null) {
                ja.put("moh_name", a.getMoh().getName());
                ja.put("moh_id", a.getMoh().getId());
            }
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject citizenshipList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getCitizenships();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("citizenship_id", a.getId());
            ja.put("citizenship_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject provinceList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.Province);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("province_id", a.getId());
            ja.put("province_code", a.getCode());
            ja.put("province_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject labList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<InstitutionType> its = new ArrayList<>();
        List<Institution> ds = institutionApplicationController.findInstitutions(its);
        for (Institution a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("lab_id", a.getId());
            ja.put("lab_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject orderingCategoryList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Item> ds = itemApplicationController.getLetterReceiveCategories();
        for (Item a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("ordering_category_id", a.getId());
            ja.put("ordering_category_name", a.getName());
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject mohList() {
        JSONObject jSONObjectOut = new JSONObject();
        JSONArray array = new JSONArray();
        List<Area> ds = areaApplicationController.getAllAreas(AreaType.MOH);
        for (Area a : ds) {
            JSONObject ja = new JSONObject();
            ja.put("moh_id", a.getId());
            ja.put("moh_name", a.getName());
            if (a.getDistrict() != null) {
                ja.put("district_name", a.getDistrict().getName());
                ja.put("district_id", a.getDistrict().getName());
            }
            array.put(ja);
        }
        jSONObjectOut.put("data", array);
        jSONObjectOut.put("status", successMessage());
        return jSONObjectOut;
    }

    private JSONObject successMessage() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 200);
        jSONObjectOut.put("type", "success");
        return jSONObjectOut;
    }

    private JSONObject errorMessage() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "Parameter name is not recognized.";
        jSONObjectOut.put("message", "Parameter name is not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoClientName() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "A client name is required.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoTestNumber() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "A test number (reference number or barcode) is required.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoPcrRequestId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "PCR Request ID is required.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoSuchPcrRequestId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "PCR Request ID is wrong. Please recheck.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoAge() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        String e = "An age is required.";
        jSONObjectOut.put("message", e);
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoIndicator() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 411);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Indicator NOT recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageLogin() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 411);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Your username and password combination is wrong.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoIps() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 411);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Your have not setup IPs you can login.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNotAnAutherizedIp() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 411);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Your are using a non autherized IP. Add it to your API IDPs and retry.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoGender() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 402);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Gender is not provided.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoLab() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 402);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Lab is not provided.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoInstituteId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 402);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter institute_id is not provided or not recognized.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoInstituteFound() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 403);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "Parameter institute_id is not found.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageInstruction() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 400);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "You must provide a value for the parameter name.");
        return jSONObjectOut;
    }

    private JSONObject errorMessageNoId() {
        JSONObject jSONObjectOut = new JSONObject();
        jSONObjectOut.put("code", 410);
        jSONObjectOut.put("type", "error");
        jSONObjectOut.put("message", "The ID provided is not found.");
        return jSONObjectOut;
    }

    private String Get_Child_Institutions(Institution institution) {
        String childInstitions = null;

        if (institution != null) {
            List<Institution> instList = institutionApplicationController.findChildrenInstitutions(institution);

            for (Institution i_ : instList) {
                if (childInstitions == null) {
                    childInstitions = institution.getCode() + ":" + i_.getCode();
                } else {
                    childInstitions += "^" + institution.getCode() + ":" + i_.getCode();
                }
            }
        }
        return childInstitions;
    }

    private String createFuelTransaction(
            Institution fuelStation,
            Institution hospital,
            Vehicle vehicle,
            Driver driver,
            String comments,
            Double requestQuantity,
            Double isssuedQuantity,
            Double odoMeterReading,
            String requestReferenceNumber,
            String issueReferenceNumber,
            WebUser wu) {

        FuelTransaction ft = new FuelTransaction();

        ft.setFromInstitution(hospital);
        ft.setToInstitution(fuelStation);

        ft.setCreatedAt(new Date());
        ft.setCreatedBy(wu);
        ft.setDriver(driver);
        ft.setInstitution(hospital);
        ft.setIssuedAt(new Date());
        ft.setIssued(true);
        ft.setIssuedInstitution(fuelStation);
        ft.setVehicle(vehicle);

        ft.setRequestQuantity((requestQuantity));
        ft.setIssuedQuantity((isssuedQuantity));

        ft.setOdoMeterReading((odoMeterReading));

        ft.setTxTime(new Date());
        ft.setTxDate(new Date());
        ft.setTransactionType(FuelTransactionType.VehicleFuelRequest);

        ft.setInvoiceNo(comments);
        ft.setIssueReferenceNumber(issueReferenceNumber);
        ft.setRequestReferenceNumber(requestReferenceNumber);
        ft.setComments(comments);
        fuelRequestAndIssueController.save(ft);
        return ft.getIdString();
    }
}
