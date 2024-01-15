package com.abt.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CTCIndexContact extends CTCIndexClient {

    @SerializedName("index_client_base_entity_id")
    private String indexClientBaseEntityId;

    @SerializedName("relationship")
    private String relationship;

    @SerializedName("how_to_notify_the_contact_client")
    private String how_to_notify_the_contact_client;

    public String getIndexClientBaseEntityId() {
        return indexClientBaseEntityId;
    }

    public void setIndexClientBaseEntityId(String indexClientBaseEntityId) {
        this.indexClientBaseEntityId = indexClientBaseEntityId;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getHow_to_notify_the_contact_client() {
        return how_to_notify_the_contact_client;
    }

    public void setHow_to_notify_the_contact_client(String how_to_notify_the_contact_client) {
        this.how_to_notify_the_contact_client = how_to_notify_the_contact_client;
    }
}
