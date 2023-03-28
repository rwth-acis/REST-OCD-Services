
package i5.las2peer.services.ocd.utils;


import i5.las2peer.services.ocd.ServiceClass;
import i5.las2peer.services.ocd.graphs.CustomGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Manages removal of content of inactive users.
 */
public class InactivityHandler {

    /**
     * Database.
     */

    Database database;
    /**
     * The thread handler used for algorithm, benchmark and metric execution.
     */
    ThreadHandler threadHandler;

    /**
     * Executor for asynchronous tasks of checking when content should be removed and removing it.
     */
    ExecutorService executor;

    /**
     * Map to track last login date and data deletion date of a user
     */
    HashMap<String, Pair<LocalDate, LocalDate>> inactivityTracker = new HashMap<String, Pair<LocalDate, LocalDate>>();

    /**
     * ServiceClass instance from which to regularly fetch the max allowed inactivity data.
     */
    ServiceClass service;

    /**
     * Handler for user specific as well as default limits including content removal
     */
    UserLimitsHandler userLimitsHandler;


    /**
     * Constructor for InactivityHandler
     *
     * @param database		Database passed from ServiceClass
     * @param threadHandler ThreadHandler passed from ServiceClass
     * @param serviceClass  ServiceClass instance
     */
    public InactivityHandler(Database database, ThreadHandler threadHandler, ServiceClass serviceClass) {

        this.threadHandler = threadHandler;
        this.executor = ThreadHandler.getExecutor();
        this.service = serviceClass;
        this.userLimitsHandler = new UserLimitsHandler(database);
        this.database = database;

        // part of the code that will be executed regularly
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {

            try {
                /* get up-to-date user inactivity info */
                inactivityTracker = this.getDeletionData();
                /* Check if user data needs to be deleted. If yes, delete the data. */
                for (String user : inactivityTracker.keySet()) {
                    System.out.println("checking inactivity of " + user + " lastLoginDate: " + inactivityTracker.get(user).getFirst() + " DeletionDate: " + inactivityTracker.get(user).getSecond());
                    if (shouldDelete(inactivityTracker.get(user))) {
                        // get all graphs of a user to delete
                        List<CustomGraph> userGraphs = database.getGraphs(user);
                        System.out.println("need to delete " + user + " data. which has " + userGraphs.size() + " graphs.");
                        // check that user has graphs, to avoid unnecessary computations
                        if (userGraphs.size() > 0) {
                           // System.out.println("Deleting graphs of user " + user + " due to inactivity.");
                           // System.out.print("Deleted graph ids: ");
                            // delete all graphs of a user
                            for (CustomGraph graph : userGraphs) {
                                database.deleteGraph(user, graph.getKey(), threadHandler);
                                System.out.print(graph.getKey() + ", ");
                            }
                            System.out.println();
                        } else {
                           // System.out.println("nothing to delete for " + user);
                            // user has no graphs, so remove the user from known users.
                        	database.deleteUserInactivityData(user, threadHandler);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 0, 1, TimeUnit.DAYS);
    }


    /**
     * Checks whether user content should be removed. This is done by comparing
     * if the deletion date is greater or equal to the current date.
     *
     * @param userDeletionInfo Pair object that holds last login and content deletion dates of user.
     * @return true if content of the user should be removed at the time of calling this method.
     */
    public boolean shouldDelete(Pair<LocalDate, LocalDate> userDeletionInfo) {

        LocalDate currentDate = LocalDate.now();
        LocalDate deletionDate = userDeletionInfo.getSecond();
        return (currentDate.isAfter(deletionDate) || currentDate.isEqual(deletionDate));

    }

    /**
     * Refreshes user's last login and deletion dates and returns deletion date
     *
     * @param username user for which the refreshment should be done.
     * @param update   true if user inactivity data should be updated (used when user logs in).
     * @return         user content deletion date
     */
    public LocalDate getAndUpdateUserInactivityData(String username, boolean update) {

        // current day
        LocalDate currentDate = LocalDate.now();
        // date when user data should be deleted if user does not relogin
        LocalDate deletionDate = currentDate.plusDays(userLimitsHandler.getAllowedInactivityDays(username));

        // try to find deletion info for user with a given username
        List<InactivityData> queryResults = database.getInactivityData(username);

        if (update) {
            /* If user not known, add an entry for it. If user is known, update the entry. */
            if (queryResults.isEmpty()) {
                System.out.println("username " + username + " unknown. creating entry for it.");
                // user unknown, create user entry.
                Pair<LocalDate, LocalDate> userInactivityTracker = new Pair<LocalDate, LocalDate>(currentDate, deletionDate);
                InactivityData inData = new InactivityData(username, userInactivityTracker);
                System.out.println("Created entry for " + inData.getUsername() + ". Last login date: " + inData.getLastLoginDate() + ". Content deletion date: " + inData.getDeletionDate());
                database.storeInactivityData(inData);
            } else {
                // user known, update deletion info.
                System.out.println("User " + username + " is known. Last login date: " + currentDate + ". Content deletion date: " + deletionDate);
                InactivityData inData = queryResults.get(0);
                inData.setLastLoginDate(currentDate);
                inData.setDeletionDate(deletionDate);
                database.updateInactivityData(inData);
            }
        }

        return deletionDate;

    }


    /**
     * Gets content deletion data about all users.
     *
     * @return A map of usernames to the pair of last login and expiration dates.
     */
    public HashMap<String, Pair<LocalDate, LocalDate>> getDeletionData() {

        HashMap<String, Pair<LocalDate, LocalDate>> inactivityTracker = new HashMap<String, Pair<LocalDate, LocalDate>>();
        List<InactivityData> queryResults = database.getAllInactivityData();

        for (InactivityData data : queryResults) {
            /* recalculate deletion date in case it has changed. */
            LocalDate deletionDate = data.lastLoginDate.plusDays(userLimitsHandler.getAllowedInactivityDays(data.getUsername()));
            inactivityTracker.put(data.getUsername(), new Pair<LocalDate, LocalDate>(data.lastLoginDate, deletionDate));
        }

        return inactivityTracker;
    }

    /**
     * Reads in value of allowed inactivity days before user content gets removed. This value can be adjusted in
     * etc/userLimitInformation.json
     *
     * @return Number of days allowed for user to be inactive, before content deletion.
     */
    public int readDefaultAllowedInactivityDays() {

        /* if for some reason no default value exists, this value will be used.*/
        int allowed_inactivity_days = 30;

        /* read the default allowed inactivity value from the JSON file */
        int readDefaultMaxInactivityValue = userLimitsHandler.getAllowedInactivityDays("default");

        if (readDefaultMaxInactivityValue >= 0) {
            allowed_inactivity_days = readDefaultMaxInactivityValue;
        }

        return allowed_inactivity_days;
    }


}
