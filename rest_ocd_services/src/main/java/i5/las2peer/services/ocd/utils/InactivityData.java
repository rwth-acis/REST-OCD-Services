package i5.las2peer.services.ocd.utils;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Represents user inactivity information. Instance of this class will store when the user logged in and when is the
 * expiration date of the user data. This information will be used to remove user's content once the expiration date is
 * reached.
 */
@Entity
public class InactivityData {

    //////////////////////// DATABASE COLUMN NAMES ////////////////////////

    /*
     * Database column name definitions.
     */
    public static final String USER_COLUMN_NAME = "USER_NAME";
    public static final String LAST_LOGIN_DATE_COLUMN_NAME = "LAST_LOGIN_DATE";
    public static final String DELETION_DATE_COLUMN_NAME = "DELETION_DATE";


    /*
     * Field name definitions for JPQL queries.
     */
    public static final String USER_NAME_FIELD_NAME = "username";
    public static final String ID_FIELD_NAME = "id";
    public static final String LAST_LOGIN_DATE_FIELD_NAME = "id";
    public static final String DELETION_DATE_FIELD_NAME = "id";

    ////////////////////////////// ATTRIBUTES //////////////////////////////
    /**
     * System generated persistence id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Username for which the info is stored.
     */
    @Column(name = USER_COLUMN_NAME)
    String username;


    @Column(name = "MINUTE")
    int min;

    /**
     * Date when the user last logged in.
     */
    @Column(name = LAST_LOGIN_DATE_COLUMN_NAME)
    @Convert(converter = DateConverter.class)
    LocalDate lastLoginDate;

    /**
     * Date when the user's content should be deleted due to inactivity.
     */
    @Column(name = DELETION_DATE_COLUMN_NAME)
    @Convert(converter = DateConverter.class)
    LocalDate deletionDate;


    /**
     * Getter for system-generated user  inactivity entry id.
     *
     * @return       return auto-generated unique id of InactivityData entry.
     */
    public Long getId() {

        return id;

    }

    /**
     * Setter for system-generated user inactivity entry id.
     *
     * @param id      id value to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Default InactivityData constructor required by jpa.
     */
    public InactivityData() {
    }

    /**
     * InactivityData constructor
     *
     * @param username username of the user for which the data is saved.
     */
    public InactivityData(String username) {

        this.username = username;

    }

    /**
     * InactivityData constructor.
     *
     * @param username          username of the user for which the data is saved.
     * @param inactivityTracker Pair holding last login and expiration dates of the user.
     */
    public InactivityData(String username, Pair<LocalDate, LocalDate> inactivityTracker) {

        this.username = username;
        this.lastLoginDate = inactivityTracker.getFirst();
        this.deletionDate = inactivityTracker.getSecond();


    }

    /**
     * Getter for username associated with this InactivityData.
     *
     * @return String representation of username
     */
    public String getUsername() {

        return username;

    }

    /**
     * Setter for username associated with this InactivityData.
     *
     * @param username      username to set.
     */
    public void setUsername(String username) {

        this.username = username;

    }


    /**
     * Getter for last login date associated with this InactivityData.
     *
     * @return LocalDate representing last login date of the user.
     */
    public LocalDate getLastLoginDate() {

        return lastLoginDate;

    }

    /**
     * Setter for last login date associated with this InactivityData.
     *
     * @param lastLoginDate      Date to set.
     */
    public void setLastLoginDate(LocalDate lastLoginDate) {

        this.lastLoginDate = lastLoginDate;

    }

    /**
     * Getter for content deletion date associated with this InactivityData.
     *
     * @return LocalDate representing content deletion date of the user.
     */
    public LocalDate getDeletionDate() {

        return deletionDate;

    }

    /**
     * Setter for content deletion date associated with this InactivityData.
     *
     * @param deletionDate      Date to set.
     */
    public void setDeletionDate(LocalDate deletionDate) {

        this.deletionDate = deletionDate;

    }
}
