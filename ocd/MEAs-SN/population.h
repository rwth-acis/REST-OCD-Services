#ifndef JRIVER_MEASN_POPULATION_H
#define JRIVER_MEASN_POPULATION_H

#include"individual.h"

typedef struct population {
    int id;
    individual_t ind;
    double lambda;
    struct population* neighbor;
} population_t;

population_t* init_population();
void free_population(population_t*);
void dump_population(population_t*);
void evolve_population(population_t*);

#endif
