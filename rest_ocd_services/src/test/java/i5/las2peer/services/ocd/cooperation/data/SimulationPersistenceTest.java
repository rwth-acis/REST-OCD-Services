package i5.las2peer.services.ocd.cooperation.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import i5.las2peer.services.ocd.cooperation.data.simulation.*;
import i5.las2peer.services.ocd.graphs.CustomGraph;
import i5.las2peer.services.ocd.utils.Database;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.List;


public class SimulationPersistenceTest {
    private static CustomGraph graph;
    private static Node n[];
    private static Database database;
    private String map = "MAP 11111";


    @BeforeAll
    public static void clearDatabase() {
        database = new Database(true);
    }

    @AfterAll
    public static void deleteDatabase() {
        database.deleteDatabase();
    }


    /////////////////////// Simulation Series //////////////////////////

    @Test
    public void storeSimulationSeries() {
        System.out.println("store sim series");
        SimulationSeries series = new SimulationSeries();
        series.setCooperationEvaluation(new Evaluation(new double[]{1.0,2.0,3.0}));
        long userId = 7;
        series.setUserId(Long.toString(userId));

        String seriesKey = "";
        try {
            seriesKey = database.storeSimulationSeries(series, Long.toString(userId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        SimulationSeries resultSeries;
        resultSeries = database.getSimulationSeries(seriesKey);

        assertNotNull(resultSeries);
        assertEquals(Long.toString(userId), resultSeries.getUserId());
        assertEquals(seriesKey, resultSeries.getKey());

    }


    @Test
    public void storeSimulationSeriesWithDatasets() {

        SimulationSeries series = new SimulationSeries();
        SimulationDataset d1 = new SimulationDataset();
        d1.setName("da1");

        SimulationDataset d2 = new SimulationDataset();
        d2.setName("da2");
        List<SimulationDataset> list = new ArrayList<>();
        list.add(d1); // add key of SimulationDataset returned when it is stored in db
        list.add(d2);
        series.setSimulationDatasets(list);
        long userId = 7;
        series.setUserId(Long.toString(userId));

        String seriesKey = "";
        try {
            seriesKey = database.storeSimulationSeries(series, Long.toString(userId));
        } catch (Exception e) {
            e.printStackTrace();
        }


        SimulationSeries resultSeries;
        resultSeries = database.getSimulationSeries(seriesKey);

        assertNotNull(resultSeries);
        assertNotNull(resultSeries.getSimulationDatasets());
        assertEquals(2, resultSeries.size());
        assertEquals("da1", resultSeries.getSimulationDatasets().get(0).getName());

    }

    @Test
    public void getSimulationSeries() {

        SimulationSeries series = new SimulationSeries();
        long userId = 7;
        series.setUserId(Integer.toString(7));
        String seriesKey = database.storeSimulationSeries(series);
        SimulationSeries resultSeries = null;
        resultSeries = database.getSimulationSeries(seriesKey);

        assertNotNull(resultSeries);
        assertEquals(Long.toString(userId), resultSeries.getUserId());
        assertEquals(seriesKey, resultSeries.getKey());

    }

    @Test
    public void getSimulationSeriesWithDatasets() {

        SimulationSeries series = new SimulationSeries();
        long userId = 7;
        series.setUserId(Integer.toString(7));

        SimulationDataset d1 = new SimulationDataset();
        d1.setName("da1");
        SimulationDataset d2 = new SimulationDataset();
        d2.setName("da2");
        List<SimulationDataset> list = new ArrayList<>();
        list.add(d1);
        list.add(d2);

        series.setSimulationDatasets(list);


        String seriesKey = database.storeSimulationSeries(series);
        SimulationSeries resultSeries = null;
        resultSeries = database.getSimulationSeries(seriesKey);

        assertNotNull(resultSeries);
        assertNotNull(resultSeries.getSimulationDatasets());
        assertEquals(2, resultSeries.size());
        assertEquals("da1", resultSeries.getSimulationDatasets().get(0).getName());

    }

    @Test
    public void getSimulationSeriesByGraphId() {

        SimulationSeries series = new SimulationSeries();
        long userId = 7;
        series.setUserId(Integer.toString(7));

        SimulationSeriesParameters parameters = new SimulationSeriesParameters();
        parameters.setGraphKey("23");
        parameters.setGraphName("testGraphName123");
        series.setSimulationSeriesParameters(parameters);

        String seriesKey = database.storeSimulationSeries(series);
        List<SimulationSeries> resultSeries = null;

        resultSeries = database.getSimulationSeriesByUser(Integer.toString(7), "23", 0, 10);
        System.out.println("size of resultSeries is " + resultSeries.size());
        assertNotNull(resultSeries);
        assertEquals(1, resultSeries.size() );
        assertNotNull(resultSeries.get(0).getSimulationSeriesParameters());
        assertEquals("23", resultSeries.get(0).getSimulationSeriesParameters().getGraphKey());

    }

    @Test
    public void getSimulationSeriesWithGraph() {

        SimulationSeries series = new SimulationSeries();
        CustomGraph graph = new CustomGraph();
        graph.setName("testGraphName");
        long userId = 7;
        database.storeGraph(graph);
        series.setUserId(Integer.toString(7));
        series.setNetwork(graph);

        String seriesKey = database.storeSimulationSeries(series);
        System.out.println("seriesKey is "+ seriesKey);

        SimulationSeries resultSeries = null;

        resultSeries = database.getSimulationSeries(seriesKey);

        assertNotNull(resultSeries);
        assertEquals(Long.toString(userId), resultSeries.getUserId());
        assertEquals(seriesKey, resultSeries.getKey());
        assertNotNull(resultSeries.getNetwork());
        assertEquals("testGraphName", resultSeries.getNetwork().getName());
        assertEquals(graph.getKey(), resultSeries.getNetwork().getKey());
    }

    @Test
    public void deleteSimulationSeries() {

        SimulationSeries series = new SimulationSeries();
        String seriesKey = database.storeSimulationSeries(series);
        database.deleteSimulationSeries(seriesKey);
        SimulationSeries resultSeries = null;
        resultSeries = database.getSimulationSeries(seriesKey);

        assertNotNull(resultSeries);
        assertEquals(null, resultSeries.getKey());

    }

    @Test
    public void getSimulationSeriesByUser() {

        long userId = 23;

        SimulationSeries series1 = new SimulationSeries();
        series1.setUserId(Long.toString(userId));

        SimulationSeries series2 = new SimulationSeries();
        series2.setUserId(Long.toString(userId));

        SimulationSeries series3 = new SimulationSeries();
        series3.setUserId(Integer.toString(22));

        SimulationSeries series4 = new SimulationSeries();
        series4.setUserId(Long.toString(userId));


        database.storeSimulationSeries(series1);
        database.storeSimulationSeries(series2);
        database.storeSimulationSeries(series3);
        database.storeSimulationSeries(series4);

        List<SimulationSeries> resultSeries = null;
        resultSeries = database.getSimulationSeriesByUser(Long.toString(userId));


        assertNotNull(resultSeries);
        assertEquals(3, resultSeries.size());

    }

    /////////////////////// Simulation Series Group //////////////////////////

    @Test
    public void storeSimulationSeriesGroup() {

        SimulationSeriesGroup simulation = new SimulationSeriesGroup();
        String userId = "testUser";
        simulation.setUserId(userId);
        String ssgKey = database.storeSimulationSeriesGroup(simulation);

        SimulationSeriesGroup result = database.getSimulationSeriesGroup(ssgKey);

        assertNotNull(result);
        assertEquals(ssgKey, result.getKey());
    }

    @Test
    public void getSimulationSeriesGroup() {

        SimulationSeriesGroup simulation = new SimulationSeriesGroup();
        String ssgKey = database.storeSimulationSeriesGroup(simulation);
        SimulationSeriesGroup result = database.getSimulationSeriesGroup(ssgKey);
        assertNotNull(result);
        assertEquals(ssgKey, result.getKey());

    }

    @Test
    public void deleteSimulationSeriesGroup() {

        SimulationSeriesGroup simulation = new SimulationSeriesGroup();
        String ssgKey = database.storeSimulationSeriesGroup(simulation);
        database.deleteSimulationSeriesGroup(ssgKey);
        SimulationSeriesGroup result = database.getSimulationSeriesGroup(ssgKey);
        assertNotNull(result);
        assertEquals(null, result.getKey());

    }

    @Test
    public void getSimulationSeriesGroupByUser() {

        String userId = "25";

        SimulationSeriesGroup s1 = new SimulationSeriesGroup();
        s1.setUserId(userId);

        SimulationSeriesGroup s2 = new SimulationSeriesGroup();
        s2.setUserId("12");

        SimulationSeriesGroup s3 = new SimulationSeriesGroup();
        s3.setUserId(userId);

        SimulationSeriesGroup s4 = new SimulationSeriesGroup();
        s4.setUserId("15");


        database.storeSimulationSeriesGroup(s1);
        database.storeSimulationSeriesGroup(s2);
        database.storeSimulationSeriesGroup(s3);
        database.storeSimulationSeriesGroup(s4);

        List<SimulationSeriesGroup>  resultList = database.getSimulationSeriesGroups(userId);

        for (SimulationSeriesGroup ssg : resultList){
            System.out.println(ssg.getKey());
        }

        assertNotNull(resultList);
        assertEquals(2, resultList.size());

    }



}
