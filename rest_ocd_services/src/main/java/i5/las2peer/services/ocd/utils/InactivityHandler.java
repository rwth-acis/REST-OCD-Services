package i5.las2peer.services.ocd.utils;


import i5.las2peer.services.ocd.ServiceClass;
import i5.las2peer.services.ocd.graphs.CustomGraph;


import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
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
     * Entity Handler for interacting with the database.
     */
    EntityHandler entityHandler;

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
     * @param entityHandler EntityHandler passed from ServiceClass
     * @param threadHandler ThreadHandler passed from ServiceClass
     * @param serviceClass  ServiceClass instance
     */
    public InactivityHandler(EntityHandler entityHandler, ThreadHandler threadHandler, ServiceClass serviceClass) {

        this.entityHandler = entityHandler;
        this.threadHandler = threadHandler;
        this.executor = ThreadHandler.getExecutor();
        this.service = serviceClass;
        this.userLimitsHandler = new UserLimitsHandler(entityHandler);

        // part of the code that will be executed regularly
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {

                /* get up-to-date user inactivity info */
                inactivityTracker = this.getDeletionData();

                /* Check if user data needs to be deleted. If yes, delete the data. */
                for (String user : inactivityTracker.keySet()) {
                    //System.out.println("Checking inactivity of " + user + " last login date: " + inactivityTracker.get(user).getFirst() + ". Deletion date: " + inactivityTracker.get(user).getSecond());

                    if (shouldDelete(inactivityTracker.get(user))) {
                        /* get all graphs of a user to delete */
                        List<CustomGraph> userGraphs = entityHandler.getGraphs(user);
                        System.out.println("Need to delete " + user + " data. which has " + userGraphs.size() + " graphs.");

                        /* check that user has graphs, to avoid unnecessary computations */
                        if (userGraphs.size() > 0) {
                            // System.out.println("Deleting graphs of user " + user + " due to inactivity.");
                            // System.out.print("Deleted graph ids: ");

                            /* delete all graphs of a user */
                            for (CustomGraph graph : userGraphs) {
                                entityHandler.deleteGraph(user, graph.getPersistenceId(), threadHandler);
                                System.out.print(graph.getId() + ", ");
                            }
                            System.out.println();

                        } else {
                            // System.out.println("nothing to delete for " + user);
                            /* user has no graphs, so remove the user from known users. */
                            entityHandler.deleteUserInactivityData(user, threadHandler);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.DAYS);
    }


    /**
     * Checks whether user content should be removed. This is done by comparing if the deletion date is greater or equal
     * to the current date.
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
     * Refreshes user's last login and deletion dates.
     *
     * @param username user for which the refreshment should be done.
     */
    public void refreshUserInactivityData(String username) {

        LocalDate currentDate = LocalDate.now(); // current day
        LocalDate deletionDate = currentDate.plusDays(userLimitsHandler.getAllowedInactivityDays(username)); // when user data should be deleted if user does not relog

        EntityManager em = this.entityHandler.getEntityManager();
        em.getTransaction().begin();

        /* try to find deletion info for user with a given username */
        String queryStr = "SELECT d FROM " + InactivityData.class.getName() + " d WHERE d." + InactivityData.USER_NAME_FIELD_NAME + " = :username";
        TypedQuery<InactivityData> query = em.createQuery(queryStr, InactivityData.class);
        query.setParameter("username", username);
        List<InactivityData> queryResults = query.getResultList();

        /* If user not known, add entry for it. If user is known, update the entry. */
        if (queryResults.isEmpty()) {
            System.out.println("User " + username + " unknown. creating entry for it.");
            /* user unknown, create user entry. */
            Pair<LocalDate, LocalDate> userInactivityTracker = new Pair<LocalDate, LocalDate>(currentDate, deletionDate);
            InactivityData inData = new InactivityData(username, userInactivityTracker);
            System.out.println("Created entry for " + inData.getUsername() + ". Last login date: " + inData.getLastLoginDate() + ". Content deletion date: " + inData.getDeletionDate());
            em.persist(inData);
        } else {
            /* user known, update deletion info. */
            System.out.println("User " + username + " is known. Last login date: " + currentDate + ". Content deletion date: " + deletionDate);
            queryResults.get(0).setLastLoginDate(currentDate);
            queryResults.get(0).setDeletionDate(deletionDate);
        }

        em.getTransaction().commit();
        em.close();

    }


    /**
     * Gets content deletion data about all users.
     *
     * @return A map of usernames to the pair of last login and expiration dates.
     */
    public HashMap<String, Pair<LocalDate, LocalDate>> getDeletionData() {

        HashMap<String, Pair<LocalDate, LocalDate>> inactivityTracker = new HashMap<String, Pair<LocalDate, LocalDate>>();
        EntityManager em = entityHandler.getEntityManager();

        List<InactivityData> queryResults;
        String queryStr = "SELECT d FROM " + InactivityData.class.getName() + " d";
        TypedQuery<InactivityData> query = em.createQuery(queryStr, InactivityData.class);
        queryResults = query.getResultList();

        em.close();

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