package com.abt.domain;

import com.google.gson.annotations.SerializedName;

public class CTCIndexContactsTestingFollowup extends CTCIndexContact {

    @SerializedName("ineligibility_reason")
    private String ineligibilityReason;

    @SerializedName("not_testing_reason")
    private String notTestingReason;

    @SerializedName("test_results")
    private String testResults;

    @SerializedName("place_for_testing")
    private String placeForTesting;

    @SerializedName("test_date")
    private String testDate;

    public String getIneligibilityReason() {
        return ineligibilityReason;
    }

    public void setIneligibilityReason(String ineligibilityReason) {
        this.ineligibilityReason = ineligibilityReason;
    }

    public String getNotTestingReason() {
        return notTestingReason;
    }

    public void setNotTestingReason(String notTestingReason) {
        this.notTestingReason = notTestingReason;
    }

    public String getTestResults() {
        return testResults;
    }

    public void setTestResults(String testResults) {
        this.testResults = testResults;
    }

    public String getPlaceForTesting() {
        return placeForTesting;
    }

    public void setPlaceForTesting(String placeForTesting) {
        this.placeForTesting = placeForTesting;
    }

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }
}
