package com.abt.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CTCPatient {

    @SerializedName("id")
    private long id;


    @SerializedName("health_facility_code")
    private String healthFacilityCode;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("surname")
    private String surname;

    @SerializedName("middle_name")
    private String middleName;

    @SerializedName("ctc_number")
    private String ctcNumber;

    @SerializedName("date_of_birth")
    private Date dateOfBirth;

    @SerializedName("gender")
    private String gender;

    @SerializedName("date_of_death")
    private long dateOfDeath;

    @SerializedName("hiv_status")
    private boolean hivStatus;

    @SerializedName("map_cue")
    private String mapCue;

    @SerializedName("hamlet")
    private String hamlet;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("care_taker_name")
    private String careTakerName;

    @SerializedName("care_taker_phone_number")
    private String careTakerPhoneNumber;

    @SerializedName("provider_id")
    private String providerId;

    @SerializedName("team")
    private String team;

    @SerializedName("team_id")
    private String teamId;

    @SerializedName("location_id")
    private String locationId;

    @SerializedName("unique_id")
    private String uniqueId;

    @SerializedName("base_entity_id")
    private String baseEntityId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public void setHealthFacilityCode(String healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCtcNumber() {
        return ctcNumber;
    }

    public void setCtcNumber(String ctcNumber) {
        this.ctcNumber = ctcNumber;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public long getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(long dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public boolean isHivStatus() {
        return hivStatus;
    }

    public void setHivStatus(boolean hivStatus) {
        this.hivStatus = hivStatus;
    }

    public String getMapCue() {
        return mapCue;
    }

    public void setMapCue(String mapCue) {
        this.mapCue = mapCue;
    }

    public String getHamlet() {
        return hamlet;
    }

    public void setHamlet(String hamlet) {
        this.hamlet = hamlet;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCareTakerName() {
        return careTakerName;
    }

    public void setCareTakerName(String careTakerName) {
        this.careTakerName = careTakerName;
    }

    public String getCareTakerPhoneNumber() {
        return careTakerPhoneNumber;
    }

    public void setCareTakerPhoneNumber(String careTakerPhoneNumber) {
        this.careTakerPhoneNumber = careTakerPhoneNumber;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }
}
