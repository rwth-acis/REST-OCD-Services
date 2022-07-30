package i5.las2peer.services.ocd.utils;


import i5.las2peer.api.logging.MonitoringEvent;

/**
 * This is an enum class that holds various user types that might interact with WebOCD and the information of whom can
 * be used by Mobsos. This class is used for monitoring event message generation and includes methods needed
 * for classifying different users into groups based on their usernames.
 */
public enum UserType {

    /**
     * Indicates that a user does not belong to a specific lecture group
     */
    GENERAL_USER("General User",
            0,
            "",
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_1, // Login messages.
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_2, // Import messages
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, // Running algorithm messages
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_4, // Deleting object messages
            MonitoringEvent.SERVICE_CUSTOM_ERROR_1 // Error messages.
    ),

    /**
     * Indicates that the user belongs to the OCDA proseminar
     */
    PROSEMINAR_STUDENT("Proseminar Student",
            1,
            "sem",
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_11,
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_12,
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_13,
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_14,
            MonitoringEvent.SERVICE_CUSTOM_ERROR_11
    ),


    /**
     * Indicates that a user belongs to the social computing lecture
     */
    SOCIAL_COMPUTING_STUDENT("Social Computing Student",
            2,
            "soccomp",
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_21,
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_22,
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_23,
            MonitoringEvent.SERVICE_CUSTOM_MESSAGE_24,
            MonitoringEvent.SERVICE_CUSTOM_ERROR_21
    );


    /**
     * Event types to be logged from the service class for mobsos.
     */
    public static final String EVENT_LOGIN = "login";
    public static final String EVENT_IMPORT = "import";
    public static final String EVENT_RUN = "run";
    public static final String EVENT_DELETE = "delete";
    public static final String EVENT_ERROR = "error";




    /**
     * Unique id representing user type.
     */
    private int id;

    /**
     * A display name for web frontends and more
     */
    private final String displayName;

    /**
     * Prefix used for usernames to differentiate between groups
     */
    private String prefix;

    /**
     * A monitoring event used to send login messages.
     */
    private MonitoringEvent loginEvent;

    /**
     * A monitoring event used to send import messages.
     * E.g. for graphs/covers/centralities
     */
    private MonitoringEvent importEvent;

    /**
     * A monitoring event used to send run (execution) messages.
     * E.g. ocd/benchmark/centrality/simulation/statistical_measure/knowledge_driven_measure
     */
    private MonitoringEvent runEvent;

    /**
     * A monitoring event used to send delete messages.
     *  E.g. graph/cover/centrality/metric
     */
    private MonitoringEvent deleteEvent;

    /**
     * A monitoring event used to send error messages
     */
    private MonitoringEvent ErrorEvent;


    UserType(String displayName, int id, String prefix, MonitoringEvent loginEvent, MonitoringEvent importEvent, MonitoringEvent runEvent, MonitoringEvent deleteEvent, MonitoringEvent errorEvent) {
        this.id = id;
        this.displayName = displayName;
        this.prefix = prefix;
        this.loginEvent = loginEvent;
        this.importEvent = importEvent;
        this.runEvent = runEvent;
        this.deleteEvent = deleteEvent;
        this.ErrorEvent = errorEvent;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public MonitoringEvent getLoginEvent() {
        return loginEvent;
    }

    public MonitoringEvent getImportEvent() {
        return importEvent;
    }

    public MonitoringEvent getRunEvent() {
        return runEvent;
    }

    public MonitoringEvent getDeleteEvent() {
        return deleteEvent;
    }

    public MonitoringEvent getErrorEvent() {
        return ErrorEvent;
    }

    /**
     * Returns the monitoring event for the event type specified.
     * This method will return the event type corresponding to
     * the instance that called it, e.g. proseminar student instance
     * @param eventType     Event type being monitored
     * @return              Monitoring event for mobsos logging
     */
    public MonitoringEvent getEvent(String eventType){
        if (eventType.equals(UserType.EVENT_LOGIN)){
            return this.getLoginEvent();
        } else if (eventType.equals(UserType.EVENT_IMPORT)){
            return this.getImportEvent();
        }
        else if (eventType.equals(UserType.EVENT_RUN)){
            return this.getRunEvent();
        }
        else if (eventType.equals(UserType.EVENT_DELETE)){
            return this.getDeleteEvent();
        }
        else {
            return this.getErrorEvent();
        }
    }

    /**
     * Returns monitoring event for a user type (derived from username) and event type
     * @param username        Username which is being monitored
     * @param eventType       Event type which is being monitored
     * @return                Monitoring event of the specified type fitting the user group
     */
    public static MonitoringEvent identifyEvent(String username, String eventType){

        if (username.split("_").length <= 1){ // general user
            return UserType.GENERAL_USER.getEvent(eventType);
        }
        else if(((String)username.split("_")[0]).equals(UserType.PROSEMINAR_STUDENT.prefix)){
            return UserType.PROSEMINAR_STUDENT.getEvent(eventType);
        }
        else{
            return UserType.SOCIAL_COMPUTING_STUDENT.getEvent(eventType);
        }


    }

    /**
     * Returns the user group corresponding to an id.
     * @param id The id.
     * @return The type.
     */
    public static UserType lookupType(int id) {
        for (UserType type : UserType.values()) {
            if (id == type.getId()) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "UserType{" +
                 displayName + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}
