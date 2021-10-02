#include "individual.h"
#include <stdlib.h>

extern int verbose;

/*get f_pos_in and f_neg_out of individual*/
void eval_individual(individual_t ind) {

    community_t* comm = ind->comm;
    int n = ind->comm_n;

    int i;

    double pin,pout,nin,nout;
    
    ind->obj[0] = 0.0;
    ind->obj[1] = 0.0;
    
    for (i=0; i!=n; ++i) {
	pin = comm[i]->pin;
	nin = comm[i]->nin;
	pout = comm[i]->pout;
	nout = comm[i]->nout;

	/*protected divide*/
	if (pin+pout==0) ind->obj[0] += 0.0;
	else ind->obj[0] += pin/(pin+pout);

	if (nin+nout==0) ind->obj[1] += 0.0;
	else ind->obj[1] += nout/(nin+nout);
    }
    ind->obj[0] /= n;
    ind->obj[1] /= n;
}

/*new a individual with genotype geno
 * actually it is only used in separate
 * as the genotype here is the label
 * the genotype array will NOT be copied */
individual_t new_individual(int* gene) {
    individual_t ind = 
	malloc(sizeof(struct individual));
    ind->gene = gene;
    ind->comm = calloc(network_size(),
	    sizeof(community_t));
    //convert label to communities
    ind->comm_n = label_to_community(gene,ind->comm);
    ind->comm = realloc(ind->comm,
	    ind->comm_n * sizeof(community_t));

    //evaluate individual
    eval_individual(ind);
    
    ind->refcount = 1;
    return ind;
}

/*release individual
 * first we decrease the refcount of an individual
 * if refcount==0 means no one use this individual
 * do real free*/
void free_individual(individual_t* iptr) {
    int i;
    if (iptr==NULL) return;

    individual_t ind = *iptr;
    if (ind==NULL) return;

    if (--(ind->refcount)) return;

    //real free
    free(ind->gene);
    for (i=0; i!=ind->comm_n; ++i) 
	free_community(ind->comm[i]);
    free(ind->comm);
    free(ind);
    *iptr = NULL;
}

/*set individual(d=s)
 * decrease refcount of d and increase refcount of s
 * if no one uses d do real free
 * so there is only one copy of the same individual
 * in memory, and copy an individual is fast*/
void set_individual(individual_t* d,individual_t s) {
    free_individual(d);
    (*d) = s;
    (s->refcount)++;
}

/*tchebycheff function
 * max (lambda_pos * |f_pos_in-f_pos_in_ref|,
 *      lambda_neg * |f_neg_in-f_neg_out_ref|) */
double tchebycheff(individual_t ind,
	double* ref,double lambda) {
    double lp = lambda; //lambda_pos
    double ln = 1-lambda; //lambda_neg as lp+ln===1
    double fp = ind->obj[0]; //f_pos_in
    double fn = ind->obj[1]; //f_neg_out
    double rp = ref[0]; //f_pos_in_ref
    double rn = ref[1]; //f_neg_out_ref

    /*as maximium problem, f* is always greater than f
     * so we need not use abs */
    double t1 = lp * (rp-fp);
    double t2 = ln * (rn-fn);

    if (t1>t2) return t1;
    else return t2;
}
