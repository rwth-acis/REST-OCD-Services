#include "population.h"
#include <string.h>
#include <stdio.h>

void get_parameters(int argc,char* argv[]);

extern const char* network_file;
extern int generation;
extern int verbose;

int main(int argc,char* argv[]) {

    //get parameters
    get_parameters(argc,argv);

    read_pajek(network_file);

    if (!is_network_ok()) {
	fprintf(stderr, 
		"File error :,"
		"invalid network file %s\n",
	       	network_file);
	return -1;
    } 

    if (verbose>=1) printf("initalizing population\n");

    population_t* pop = init_population();

    int i;
    for (i=1; i<=generation; ++i) {

	if (verbose>=1) 
	    printf("---------------"
		   "%3d/%-3d"
		   "---------------\n",
		   i,generation);

	evolve_population(pop);
    }

    if (verbose>=1) printf("dumping population\n");

    dump_population(pop);

    free_population(pop);
    free_network();

    return 0;
}
