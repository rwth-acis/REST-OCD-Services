#ifndef JRIVER_MEASN_INDIVIDUAL_H
#define JRIVER_MEASN_INDIVIDUAL_H

#include "community.h"

typedef struct individual {
    int* gene; //genotype
    community_t* comm; //communities
    int comm_n; //number of communities
    double obj[2]; //f_pos_in,f_neg_out
    int refcount;
}* individual_t;

void set_individual(individual_t*,individual_t);
void free_individual(individual_t*);

individual_t new_individual(int*);
void eval_individual(individual_t);

double tchebycheff(individual_t,double*,double);

individual_t decode(int*);
individual_t crossover(individual_t,individual_t);
individual_t mutation(individual_t);

#endif
