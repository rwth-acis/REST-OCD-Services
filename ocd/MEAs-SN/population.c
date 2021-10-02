#include "population.h"
#include "random.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

/*compare lambda of two pop,used for qsort*/
static int lambda_cmp(const void* a,const void* b) {
    const double* p1 = a;
    const double* p2 = b;
    if (*p1 > *p2) return  1;
    if (*p1 < *p2) return -1;
    return 0;
}

extern int popsize;
extern int T; //neighbor of a pop
extern int verbose;

static double ref[2]; //f_*_ref ideal point

/*show a infomation of a pop
 * format:
 * [id](lambda) f=(f1,f2) #(number of communities)
 * */
void population_info(population_t *p) {
    printf("[%04d]",p->id);
    printf("(%.5f) ",p->lambda);
    printf("f=(%.2lf,%.2lf) ",p->ind->obj[0],p->ind->obj[1]);
    printf("#%-5d\n",p->ind->comm_n);
}

/*initialize populations
 *generate popsize population*/
population_t* init_population() {

    population_t* pop = 
	calloc(popsize,sizeof(population_t));

    int n = network_size();
    int i,j;

    int*    perm = calloc(n,sizeof(int));
    double* lambda = calloc(n,sizeof(double)); 

    /*initalize permutation and lambda*/
    for (i=0; i!=n; ++i) {
	perm[i] = i;
	lambda[i] = unirand();
    }

    /*sort lambda
     *as neighbor of a pop is T nearest pops
     *to it in Eculid distance which
     *is sqrt((l1p-l2p)^2 + (l1n-l2n)^2)
     *and l1p+l1n=1,l2p+l2n=1,so it is
     *|l1p-l2p| it means the nearer lambda,
     * the nearer distance, so sort*/
    qsort(lambda,n,sizeof(double),lambda_cmp);

    for (i=0; i!=popsize; ++i) {
	//random order of the permutation
	shuffle(perm,n);

	pop[i].id = i;
	//decode the permutation to individaul
	pop[i].ind = decode(perm);
	pop[i].lambda = lambda[i];

	if (verbose>=2) population_info(pop+i);
    }

    free(perm); free(lambda);

    int l,h;

    /*set neighbors of pop*/
    for (i=0; i!=popsize; ++i) {
	/*as lambda is generated in a uniform
	 * so the nearest T pops to pop[i]
	 * is approximately pop[i-T/2]~pop[i+T/2]*/

	//neighor [l,h]
	l = i - T/2;
	h = i + T/2;

	//set l,h if one is out of range
	if (l<0) {l=0; h=T-1;}
	if (h>=popsize) {h=popsize-1; l = popsize-T;}

	//try to move [l,h] if better
       	while (l>0 &&  //move left
		(pop[i].lambda - pop[l-1].lambda
		 < pop[h].lambda - pop[i].lambda)) {
		l--; h--;
	    }
       	while (h<popsize-1 &&  //move right
		(pop[i].lambda - pop[l].lambda
		 < pop[h+1].lambda - pop[i].lambda)) {
		l++; h++;
	    }

       	pop[i].neighbor = pop + l; 
    }
    
    /*update ideal point*/
    ref[0] = pop[0].ind->obj[0];
    ref[1] = pop[0].ind->obj[1];
    
    for (i=1; i!=popsize; ++i) {
	if (pop[i].ind->obj[0] > ref[0])
	    ref[0] = pop[i].ind->obj[0];
	if (pop[i].ind->obj[1] > ref[1])
	    ref[1] = pop[i].ind->obj[1];
    }

    return pop;
}

/*use an individual ind to udpate
 * the neighbors of a pop*/
static void update_pop_neighbor(population_t pop,
	individual_t ind) {

    double f_old,f_new;
    int i;
    double lambda;

    population_t* nb = pop.neighbor;

    for (i=0; i!=T; ++i) {

	lambda = nb[i].lambda;

	/*compare tchebycheff of two ind*/
	f_old = tchebycheff(nb[i].ind,ref,lambda);
	f_new = tchebycheff(ind,ref,lambda);

	if (f_new>f_old) {

	    set_individual(&(nb[i].ind),ind);

	    if (verbose>=2) population_info(nb+i);
	}
    }
}

/*evolution population*/
void evolve_population(population_t* pop) {
    int i,j;
    individual_t child;

    for (i=0; i!=popsize; ++i) {

	//choose one to crossvoer with
	do j=rand()%popsize; while (j==i);

	child = crossover(pop[i].ind,pop[j].ind);
	child = mutation(child);

	/*update ideal point*/
	if (child->obj[0]>ref[0])
	    ref[0] = child->obj[0];
	if (child->obj[1]>ref[1])
	    ref[1] = child->obj[1];

	//update neighbors
	update_pop_neighbor(pop[i],child);

	/*child is no longer used
	 * (maybe used by other pops
	 * but not used by child)*/
	free_individual(&child);
    }
}

/*release population*/
void free_population(population_t* pop) {
    int i;
    for (i=0; i!=popsize; ++i) {
	free_individual(&(pop[i].ind));
    }
    free(pop);
}

/*dump a pop into a file*/
static void dump_pop(
	const char* name,population_t* pop) {
    FILE* f = fopen(name,"w");
    fprintf(f,"#id      %d\n",pop->id);
    fprintf(f,"#lambda  %lf\n",pop->lambda);
    fprintf(f,"#pos_in  %lf\n",pop->ind->obj[0]);
    fprintf(f,"#neg_out %lf\n",pop->ind->obj[1]);
    fprintf(f,"#number  %d\n",pop->ind->comm_n);
    int i;
    links_t p;
    for (i=0; i!=pop->ind->comm_n; ++i) {
	fprintf(f,"#community %d size %d\n",
		i,pop->ind->comm[i]->size);

	p = pop->ind->comm[i]->head;
	while (p) {
	    fprintf(f,"%d\n",p->to);
	    p = p->next;
	}
    }
    fclose(f);
}

extern const char* network_file;
/*dump all pops in population*/
void dump_population(population_t* pop) {
    int i;
    char *of = 
	calloc(strlen(network_file)+10,sizeof(char));

    for (i=0; i!=popsize; ++i) {
	if (verbose>=1) population_info(pop+i);
	//generate output filename
	sprintf(of,"%s.%04d.pop",network_file,i);
	//dump
	dump_pop(of,pop+i);
    }

    free(of);
}
