package i5.las2peer.services.ocd.utils;

import i5.las2peer.services.ocd.graphs.CustomGraph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.List;

/**
 * This class handles limits both to specific users. The limits are read from
 * etc/userLimitInformation.json file. If no user specific limits are given,
 * then the default limits apply. Limits relate to how long the user can
 * refrain from logging in before user content gets removed, as well as
 * the number and size of graphs/covers etc.
 */
public class UserLimitsHandler {

    /**
     * Parser for user limit data from the input json file
     */
    private JSONParser jsonParser = new JSONParser();

    /**
     * JSON Object to hold default limit information used for users who don't have custom limits set
     */
    private JSONObject defaultUserLimits;

    /**
     * Database.
     */

    private Database database;


    public UserLimitsHandler(Database database) {
        this.database = database;
    }

    /**
     * This method returns json object with information about various limits applied to the given user. Limits are set
     * in etc/userLimitInformation.json
     *
     * @param username username the limits of which should be returned
     * @return json object with user limit info
     */
    public JSONObject getUserLimits(String username) {

        // limits set for user in userLimitInformation.json file
        JSONObject userLimits;
        try {
            /* try to find specified user limits */
            JSONArray userLimitsJson = (JSONArray) jsonParser.parse(new FileReader("etc/userLimitInformation.json"));
            for (Object o : userLimitsJson) {
                userLimits = (JSONObject) o;
                String limitedUser = (String) userLimits.get("username");
                if (limitedUser.equals(username)) {
                    return userLimits;
                }
                if (limitedUser.equals("default")) {
                    defaultUserLimits = userLimits;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* if user limit was not found, apply default limits */
        return defaultUserLimits;
    }

    /**
     * Returns number of days a given user can be inactive before user content is removed
     *
     * @param username User for which the inactivity should be checked
     * @return Number of inactive days allowed
     */
    public int getAllowedInactivityDays(String username) {

        // limits set for user in userLimitInformation.json file
        JSONObject userLimits = getUserLimits(username);
        if (userLimits != null) {
            // Graph count limit check
            if (userLimits.get("allowedInactiveDays") != null) {
                /* If user has inactivity limit set then return it */
                return Integer.parseInt((String) userLimits.get("allowedInactiveDays"));
            }
        }
        /* If code came here then no user limit is set. Return default inactivity limit */
        userLimits = getUserLimits("default");
        return Integer.parseInt((String) userLimits.get("allowedInactiveDays"));
    }

    /**
     * Check whether user has limit on number of graphs it can generate.
     *
     * @param username user to check the limits for
     * @return true if the limit is reached
     */
    public Boolean reachedGraphCountLimit(String username) {
        // limits set for user in userLimitInformation.json file
        JSONObject userLimits = getUserLimits(username);
        if (userLimits != null) {
            // Graph count limit check
            try {
                List<CustomGraph> userGraphs = database.getGraphs(username);
                if (userLimits.get("graphCount") != null
                        && userGraphs.size() >= Integer.parseInt((String) userLimits.get("graphCount"))) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Check whether user has limit on number of covers it can generate.
     *
     * @param username user to check the limits for
     * @return true if the limit is reached
     */
    public Boolean reachedCoverCountLimit(String username) {
        // limits set for user in userLimitInformation.json file
        JSONObject userLimits = getUserLimits(username);
        if (userLimits != null) {
            // Graph count limit check
            try {
                List<CustomGraph> userGraphs = database.getGraphs(username);
                // Cover count limit check
                int numberOfUserCovers = 0;
                for (CustomGraph userGraph : userGraphs) {
                    numberOfUserCovers += database.getCovers(username, userGraph.getKey()).size();
                }
                if (userLimits.get("coverCount") != null
                        && numberOfUserCovers >= Integer.parseInt((String) userLimits.get("coverCount"))) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}
