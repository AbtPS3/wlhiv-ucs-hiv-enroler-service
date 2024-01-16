package com.abt.domain;

import com.google.gson.annotations.SerializedName;

public class CTCIndexContact extends CTCIndexClient {

    @SerializedName("index_client_base_entity_id")
    private String indexClientBaseEntityId;

    @SerializedName("elicitation_number")
    private String elicitationNumber;

    @SerializedName("relationship")
    private String relationship;

    @SerializedName("notification_type")
    private String notificationType;

    public String getIndexClientBaseEntityId() {
        return indexClientBaseEntityId;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getElicitationNumber() {
        return elicitationNumber;
    }
}
