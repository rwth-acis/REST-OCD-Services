package i5.las2peer.services.ocd.algorithms.mea;

/**
 * This class serves as the entry point into mea algorithm
 */
public class MeaAlgorithm {

    /**
     * Execute MEA algorithm
     * @param networkFile     Input file that holds network
     * @param graphName       Name of the network
     */
    public static void executeMEA(String networkFile, String graphName){

        Parameter.network_file = graphName;
        Network.read_pajek(networkFile);//TODO:is this ok?

        Population[] pop = Population.init_population();


        for (int i = 0; i < Parameter.generation; ++i){

            Population.evolve_population(pop);

        }

        Population.dump_population(pop);
    }
}
