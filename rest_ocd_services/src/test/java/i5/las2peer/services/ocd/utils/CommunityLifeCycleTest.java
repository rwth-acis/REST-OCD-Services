package i5.las2peer.services.ocd.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class CommunityLifeCycleTest {

    @Test
    public void testClcCleanUp(){
        CommunityLifeCycle clc = new CommunityLifeCycle();

        List<String> birth_nodes = new ArrayList<>();
        birth_nodes.add("A");
        birth_nodes.add("B");
        birth_nodes.add("C");

        clc.handleBirth("0", 0, birth_nodes);
        clc.handleBirth("1", 1, birth_nodes);
        clc.handleDeath("1", 1);

        assertEquals(1, clc.getEvents().size());
        System.out.println(clc.getEvents().toString());

        clc.handleBirth("2", 2, birth_nodes);
        clc.handleGrowth("2", 2, "D");
        clc.handleGrowth("2", 2, "E");
        clc.handleGrowth("2", 2, "F");

        assertEquals(3, clc.getEvents().size());
        System.out.println(clc.getEvents().toString());

        clc.handleContraction("3", 2, "D");
        clc.handleContraction("3", 2, "B");

        assertEquals(4, clc.getEvents().size());
        System.out.println(clc.getEvents().toString());
    }
}
