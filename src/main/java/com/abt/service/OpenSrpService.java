package com.abt.service;

import com.abt.domain.*;
import com.abt.util.DateTimeTypeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.joda.time.DateTime;

import java.util.*;

/**
 * Service class for OpenSRP operations.
 */
public class OpenSrpService {

    private static final int clientDatabaseVersion = 17;
    private static final int clientApplicationVersion = 2;

    /**
     * Creates and returns an observation for the start event.
     *
     * @return Obs object for the start event.
     */
    private static Obs getStartOb() {
        return new Obs("concept", "start",
            "163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "",
            Arrays.asList(new Object[]{new Date()}), null, null, "start");
    }

    /**
     * Creates and returns an observation for the end event.
     *
     * @return Obs object for the end event.
     */
    private static Obs getEndOb() {
        return new Obs("concept", "end",
            "163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "",
            Arrays.asList(new Object[]{new Date()}), null, null, "end");
    }

    /**
     * Creates a Client object for a given CTCPatient.
     *
     * @param patient The CTCPatient object.
     * @return Client object for the family.
     */
    public static Client getClientEvent(CTCIndexClient patient) {
        Client familyClient = new Client(UUID.randomUUID().toString());
        familyClient.setFirstName(patient.getSurname());
        familyClient.setLastName("Family");
        familyClient.setBirthdate(new Date(0));
        familyClient.setBirthdateApprox(false);
        familyClient.setDeathdateApprox(false);
        familyClient.setGender(patient.getGender());
        familyClient.setClientApplicationVersion(clientApplicationVersion);
        familyClient.setClientDatabaseVersion(clientDatabaseVersion);
        familyClient.setType("Client");
        familyClient.setId(UUID.randomUUID().toString());
        familyClient.setDateCreated(new Date());
        familyClient.setAttributes(new HashMap<>());
        setAddress(familyClient, patient.getHamlet(), patient.getMapCue());


        return familyClient;
    }

    /**
     * Sets the address for the given Client object.
     *
     * @param client  The Client object.
     * @param village The village information.
     * @param mapCue  The mapCue information.
     */
    public static void setAddress(Client client, String village,
                                  String mapCue) {
        List<Address> addresses = new ArrayList<>();

        if (village != null && !village.isEmpty()) {
            Address villageAddress = new Address();
            villageAddress.setAddressType("village");
            villageAddress.setCityVillage(village);

            if (mapCue != null && !mapCue.isEmpty()) {
                HashMap<String, String> addressFields = new HashMap<>();
                addressFields.put("landmark", mapCue);
                villageAddress.setAddressFields(addressFields);
            } else {
                villageAddress.setAddressFields(new HashMap<>());
            }
            addresses.add(villageAddress);
        }

        client.setAddresses(addresses);
    }

    /**
     * Creates a Client object for the family head based on a given CTCPatient.
     *
     * @param patient The CTCPatient object.
     * @return Client object for the family head.
     */
    public static Client getFamilyHeadClientEvent(CTCIndexClient patient) {
        Client ctcClient = new Client(UUID.randomUUID().toString());
        try {
            ctcClient.setFirstName(patient.getFirstName());
            ctcClient.setMiddleName(patient.getMiddleName());
        } catch (Exception e) {
            ctcClient.setMiddleName("");
            e.printStackTrace();
        }

        ctcClient.setLastName(patient.getSurname());
        ctcClient.setGender(patient.getGender());
        ctcClient.setBirthdate(patient.getDateOfBirth());
        ctcClient.setBirthdateApprox(false);
        ctcClient.setType("Client");
        ctcClient.setDeathdateApprox(false);
        ctcClient.setClientApplicationVersion(clientApplicationVersion);
        ctcClient.setClientDatabaseVersion(clientDatabaseVersion);

        Map<String, Object> attributes = new HashMap<>();
        List<String> id_available = new ArrayList<>();
        id_available.add("chk_none");
        attributes.put("id_avail", new Gson().toJson(id_available));
        attributes.put("Community_Leader", new Gson().toJson(id_available));
        attributes.put("Health_Insurance_Type", "None");

        ctcClient.setAttributes(attributes);
        setAddress(ctcClient, patient.getHamlet(), patient.getMapCue());
        return ctcClient;
    }

    /**
     * Creates a Family Registration Event for a given Client and CTCPatient.
     *
     * @param client  The Client object.
     * @param patient The CTCPatient object.
     * @return Family Registration Event.
     */
    public static Event getFamilyRegistrationEvent(Client client,
                                                   CTCIndexClient patient) {
        Event familyRegistrationEvent = new Event();
        familyRegistrationEvent.setBaseEntityId(client.getBaseEntityId());
        familyRegistrationEvent.setEventType("Family Registration");
        familyRegistrationEvent.setEntityType("ec_independent_client");
        setMetaData(familyRegistrationEvent, patient);
        familyRegistrationEvent.addObs(new Obs("formsubmissionField", "text",
            "last_interacted_with", "",
            Arrays.asList(new Object[]{String.valueOf(Calendar.getInstance().getTimeInMillis())}), null, null, "last_interacted_with"));
        return familyRegistrationEvent;
    }


    /**
     * Creates a Family Member Registration Event for a given Client and
     * CTCPatient.
     *
     * @param client  The Client object.
     * @param patient The CTCPatient object.
     * @return Family Member Registration Event.
     */
    public static Event getFamilyMemberRegistrationEvent(Client client,
                                                         CTCIndexClient patient) {
        Event familyMemberRegistrationEvent = new Event();
        familyMemberRegistrationEvent.setBaseEntityId(client.getBaseEntityId());
        familyMemberRegistrationEvent.setEventType("Family Member " +
            "Registration");
        familyMemberRegistrationEvent.setEntityType("ec_independent_client");
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField",
            "text", "id_avail", "", Arrays.asList(new Object[]{"None"}), null
            , null, "id_avail"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField",
            "text", "leader", "", Arrays.asList(new Object[]{"None"}), null,
            null, "leader"));
        familyMemberRegistrationEvent.addObs(new Obs("formsubmissionField",
            "text", "last_interacted_with", "",
            Arrays.asList(new Object[]{String.valueOf(Calendar.getInstance().getTimeInMillis())}), null, null, "last_interacted_with"));
        familyMemberRegistrationEvent.addObs(new Obs("concept", "text", "",
            "", Arrays.asList(new Object[]{client.getLastName()}), null, null
            , "surname"));

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber tzPhoneNumber =
                phoneUtil.parse(patient.getPhoneNumber(), "TZ");

            familyMemberRegistrationEvent.addObs(new Obs("concept", "text",
                "phone_number", "",
                Arrays.asList(new Object[]{
                    phoneUtil.format(
                        tzPhoneNumber,
                        PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                        .replace(" ","")
                }), null,
                    null
                , "phone_number"));
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }

        if (patient.getCareTakerName() != null && !patient.getCareTakerName().isEmpty()) {
            familyMemberRegistrationEvent.addObs(new Obs("concept", "text",
                "Has_Primary_Caregiver", "",
                Arrays.asList(new Object[]{"Yes"}),
                Arrays.asList(new Object[]{"Yes"}), null,
                "has_primary_caregiver"));
            familyMemberRegistrationEvent.addObs(new Obs("concept", "text",
                "Primary_Caregiver_Name", "",
                Arrays.asList(new Object[]{patient.getCareTakerName()}), null
                , null, "primary_caregiver_name"));

            try {
                Phonenumber.PhoneNumber cateTakerPhoneNumber =
                    phoneUtil.parse(patient.getCareTakerPhoneNumber(), "TZ");

                familyMemberRegistrationEvent.addObs(new Obs("concept", "text",
                    "other_phone_number",
                    "",
                    Arrays.asList(new Object[]{
                        phoneUtil.format(
                            cateTakerPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL
                        ).replace(" ","")
                    }),
                    null, null, "other_phone_number"));
            } catch (NumberParseException e) {
                System.err.println("NumberParseException was thrown: " + e.toString());
            }

            familyMemberRegistrationEvent.addObs(new Obs("concept", "text",
                "data_source", "",
                Arrays.asList(new Object[]{"ctc_import"}), null, null,
                "data_source"));
        }


        setMetaData(familyMemberRegistrationEvent, patient);
        return familyMemberRegistrationEvent;
    }

    /**
     * Creates an HIV Registration Event for a given Client and CTCPatient.
     *
     * @param client  The Client object.
     * @param patient The CTCPatient object.
     * @return HIV Registration Event.
     */
    public static Event getHIVRegistrationEvent(Client client,
                                                CTCIndexClient patient) {
        Event hivFollowupEvent = new Event();
        hivFollowupEvent.setBaseEntityId(client.getBaseEntityId());
        hivFollowupEvent.setEventType("HIV Registration");
        hivFollowupEvent.setEntityType("ec_hiv_register");
        hivFollowupEvent.addObs(new Obs("concept", "text",
            "new_or_current_hiv_client", "", Arrays.asList(new Object[]{
            "existing"}), null, null, "new_or_current_hiv_client"));

        hivFollowupEvent.addObs(new Obs("concept", "text",
            "client_hiv_status_during_registration", "",
            Arrays.asList(new Object[]{"Positive"}), null, null,
            "client_hiv_status_during_registration"));

        hivFollowupEvent.addObs(new Obs("concept", "text", "test_results", ""
            , Arrays.asList(new Object[]{"Positive"}), null, null,
            "test_results"));

        hivFollowupEvent.addObs(new Obs("concept", "text",
            "place_where_test_was_conducted", "", Arrays.asList(new Object[]{
            "ctc"}), null, null, "place_where_test_was_conducted"));

        hivFollowupEvent.addObs(new Obs("concept", "text", "ctc_number", "",
            Arrays.asList(new Object[]{patient.getCtcNumber()}), null, null,
            "ctc_number"));

        hivFollowupEvent.addObs(new Obs("concept", "text",
            "hiv_registration_date", "",
            Arrays.asList(new Object[]{Calendar.getInstance().getTimeInMillis()}), null, null, "hiv_registration_date"));

        setMetaData(hivFollowupEvent, patient);
        return hivFollowupEvent;
    }


    /**
     * Creates an Index Contact  Elicitation Event for a given Client
     *
     * @param client  The Client object.
     * @param indexContact The CTCIndexContact object.
     * @return Index Contact Elicitation Event.
     */
    public static Event getIndexContactElicitationEvent(Client client,
                                                CTCIndexContact indexContact) {
        Event hivFollowupEvent = new Event();
        hivFollowupEvent.setBaseEntityId(client.getBaseEntityId());
        hivFollowupEvent.setEventType("Hiv Index Contact Registration");
        hivFollowupEvent.setEntityType("ec_hiv_index_hf");

        hivFollowupEvent.addObs(new Obs("concept", "text",
            "index_client_base_entity_id", "", Arrays.asList(new Object[]{
            indexContact.getIndexClientBaseEntityId()}), null, null, "index_client_base_entity_id"));

        hivFollowupEvent.addObs(new Obs("concept", "text",
            "relationship", "",
            Arrays.asList(new Object[]{indexContact.getRelationship()}), null
            , null,
            "relationship"));

        hivFollowupEvent.addObs(new Obs("concept", "text", "how_to_notify_the_contact_client", ""
            , Arrays.asList(new Object[]{indexContact.getNotificationType()}), null, null,
            "how_to_notify_the_contact_client"));

        setMetaData(hivFollowupEvent, indexContact);
        return hivFollowupEvent;
    }

    /**
     * Sets metadata for the given Event based on a CTCPatient.
     *
     * @param event   The Event object.
     * @param patient The CTCPatient object.
     */
    private static void setMetaData(Event event, CTCIndexClient patient) {
        event.setLocationId(patient.getLocationId());
        event.setProviderId(patient.getProviderId());
        event.setTeamId(patient.getTeamId());
        event.setTeam(patient.getTeam());
        event.setType("Event");
        event.setFormSubmissionId(UUID.randomUUID().toString());
        event.setEventDate(new Date());
        event.setDateCreated(new Date());
        event.addObs(OpenSrpService.getStartOb());
        event.addObs(OpenSrpService.getEndOb());
        event.setClientApplicationVersion(clientApplicationVersion);
        event.setClientDatabaseVersion(clientDatabaseVersion);
        event.setDuration(0);
        event.setIdentifiers(new HashMap<>());
    }


    /**
     * Generates Client events for a list of CTCPatients.
     *
     * @param ctcIndexClients List of CTCPatient objects.
     * @return JSON representation of ClientEvents.
     */
    public static String generateClientEvent(List<CTCIndexClient> ctcIndexClients) {

        List<Client> clients = new ArrayList<>();
        List<Event> events = new ArrayList<>();

        for (CTCIndexClient patient : ctcIndexClients) {
            Client familyClient = getClientEvent(patient);
            Client ctcClient = getFamilyHeadClientEvent(patient);

            patient.setBaseEntityId(ctcClient.getBaseEntityId());

            Map<String, List<String>> familyRelationships = new HashMap<>();
            familyRelationships.put("family_head",
                Collections.singletonList(ctcClient.getBaseEntityId()));
            familyRelationships.put("primary_caregiver",
                Collections.singletonList(ctcClient.getBaseEntityId()));
            familyClient.setRelationships(familyRelationships);

            Map<String, String> familyIdentifier = new HashMap<>();
            familyIdentifier.put("opensrp_id", patient.getUniqueId() +
                "_family");
            familyClient.setIdentifiers(familyIdentifier);


            Map<String, List<String>> ctcClientRelations = new HashMap<>();
            ctcClientRelations.put("family",
                Collections.singletonList(familyClient.getBaseEntityId()));
            ctcClient.setRelationships(ctcClientRelations);

            Map<String, String> clientIdentifier = new HashMap<>();
            clientIdentifier.put("opensrp_id", patient.getUniqueId());
            ctcClient.setIdentifiers(clientIdentifier);


            //Generate family registration event
            Event familyRegistrationEvent =
                getFamilyRegistrationEvent(familyClient, patient);

            //Generate family Member registration event
            Event familyMemberRegistrationEvent =
                getFamilyMemberRegistrationEvent(ctcClient, patient);

            //Generate HIV Registration event
            Event hivRegistrationEvent = getHIVRegistrationEvent(ctcClient,
                patient);


            clients.add(familyClient);
            clients.add(ctcClient);
            events.add(familyRegistrationEvent);
            events.add(familyMemberRegistrationEvent);
            events.add(hivRegistrationEvent);
        }

        ClientEvents clientEvents = new ClientEvents();
        clientEvents.setClients(clients);
        clientEvents.setEvents(events);
        clientEvents.setNoOfEvents(events.size());

        Gson gson
            = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
            .create();


        return gson.toJson(clientEvents);

    }


    /**
     * Generates Client events for a list of CTCPatients.
     *
     * @param ctcIndexContacts List of CTCPatient objects.
     * @return JSON representation of ClientEvents.
     */
    public static String generateContactsEvent(List<CTCIndexContact> ctcIndexContacts) {

        List<Client> clients = new ArrayList<>();
        List<Event> events = new ArrayList<>();

        for (CTCIndexContact patient : ctcIndexContacts) {
            Client familyClient = getClientEvent(patient);
            Client ctcClient = getFamilyHeadClientEvent(patient);

            patient.setBaseEntityId(ctcClient.getBaseEntityId());

            Map<String, List<String>> familyRelationships = new HashMap<>();
            familyRelationships.put("family_head",
                Collections.singletonList(ctcClient.getBaseEntityId()));
            familyRelationships.put("primary_caregiver",
                Collections.singletonList(ctcClient.getBaseEntityId()));
            familyClient.setRelationships(familyRelationships);

            Map<String, String> familyIdentifier = new HashMap<>();
            familyIdentifier.put("opensrp_id", patient.getUniqueId() +
                "_family");
            familyClient.setIdentifiers(familyIdentifier);


            Map<String, List<String>> ctcClientRelations = new HashMap<>();
            ctcClientRelations.put("family",
                Collections.singletonList(familyClient.getBaseEntityId()));
            ctcClient.setRelationships(ctcClientRelations);

            Map<String, String> clientIdentifier = new HashMap<>();
            clientIdentifier.put("opensrp_id", patient.getUniqueId());
            ctcClient.setIdentifiers(clientIdentifier);


            //Generate family registration event
            Event familyRegistrationEvent =
                getFamilyRegistrationEvent(familyClient, patient);

            //Generate family Member registration event
            Event familyMemberRegistrationEvent =
                getFamilyMemberRegistrationEvent(ctcClient, patient);

            //Generate Index Contacts Elicitation event
            Event indexContactsElicitationEvent =
                getIndexContactElicitationEvent(ctcClient,
                patient);


            clients.add(familyClient);
            clients.add(ctcClient);
            events.add(familyRegistrationEvent);
            events.add(familyMemberRegistrationEvent);
            events.add(indexContactsElicitationEvent);
        }

        ClientEvents clientEvents = new ClientEvents();
        clientEvents.setClients(clients);
        clientEvents.setEvents(events);
        clientEvents.setNoOfEvents(events.size());

        Gson gson
            = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
            .create();


        return gson.toJson(clientEvents);

    }
}
