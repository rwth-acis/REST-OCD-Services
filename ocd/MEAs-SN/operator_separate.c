#include "individual.h"
#include "random.h"
#include <stdlib.h>
#include <stdio.h>


const char* operator = "separated";

/*convert a permutation into a individual*/
individual_t decode(int* gene) {
    int  n = network_size();
    community_t * c = calloc(n,sizeof(community_t));
    int cn=0;

    int i,j;
    double bp,bn;

    c[cn++] = new_community(gene[0]);
    for (i=1; i!=n; ++i) {
	//make vertice to a community
	//easy to calc between_p,between_n
	//and tightness_inc, and merge
	c[cn] = new_community(gene[i]);
	for (j=0; j!=cn; ++j) {
	    bp = between_p(c[j],c[cn]);
	    bn = between_n(c[j],c[cn]);
	    if (tightness_inc(c[j],c[cn],bp,bn) > 0) {
		merge(c[j],c[cn],bp,bn);
		break; //so separated
	    }
	}
	if (j==cn) cn++;
    }

    /*Merge if Tightness increase*/
    /* no use
    int max_i,max_j;
    double inc1,inc2,max_inc = 0;
    double max_bp,max_bn;
    do {
	max_inc = 0;
	for (i=0; i!=cn; ++i)
	    for (j=i+1; j!=cn; ++j) {
		bp = between_p(c[i],c[j]);
		bn = between_n(c[i],c[j]);
		inc1 = tightness_inc(c[i],c[j],bp,bn);
		inc2 = tightness_inc(c[j],c[i],bp,bn);
		if (inc1<=0 || inc2<=0) continue;
		if (inc1>max_inc) {
		    max_inc = inc1;
		    max_i = i; max_j = j;
		    max_bp = bp; max_bn = bn;
		}
		if (inc2>max_inc) {
		    max_inc = inc2;
		    max_i = j; max_j = i;
		    max_bp = bp; max_bn = bn;
		}
	    }
	if (max_inc > 0) {
	    merge(c[max_i],c[max_j],max_bp,max_bn);
	    c[max_j] = c[--cn];
	}
    } while (max_inc > 0);
    */


    individual_t ind = 
	malloc(sizeof(struct individual));

    ind->comm = realloc(c,cn*sizeof(community_t));
    ind->comm_n = cn;
    ind->refcount = 1;

    /*genotype of seperated communities is
     * the label of vertices*/
    ind->gene = calloc(n,sizeof(int));
    community_to_label(c,ind->gene,cn);

    //evalutate
    eval_individual(ind);
    return ind;
}

extern double pc;
/*crossoover
 * generate a new individual*/
individual_t crossover(individual_t p1,individual_t p2) {
    int* gene;
    int node,i;
    int n = network_size();

    community_t c;
    links_t l;

    if (unirand() > pc) { //will not crossover
	(p1->refcount)++; //child is as same as p1
	return p1;
    } else {
	//copy p1's gene
	gene = calloc(n,sizeof(int));
	for (i=0; i!=n; ++i) gene[i] = p1->gene[i];

	//random a point node
	node = rand() % n;
	c = find_community(node,p2->comm,p2->comm_n);

	//set the labels in p1 of 
	//all vertices in the same communities 
	//of node in p2 to equal to p2
	for (l=c->head; l!=NULL; l=l->next) {
	    gene[l->to] = p2->gene[l->to];
	}

	//use new genotype to get an individual
	return new_individual(gene);
    }
}

/*roulette selection
 * only positive similarities
 * participate in selection
 * if no positive similarity, choose 
 * the minimium negative similarity */
static
int roulette_neighbor(int i) {

    links_t l = neighbor(i);
    int    min_id= l->to;

    //roulette end value
    double end = degree_p(i)*unirand();
    double s;

    while (l) {
	s = similarity(i,l->to);

	//update mimimium vertice
	if (s<similarity(i,min_id))
	    min_id = l->to;

	//ignore negative similarities
	if (s<=0) l=l->next;
	else {
	    if (end<=s) return l->to;
	    else end -= s;
	    l = l->next;
	}
    }

    //not return in while,
    //means no positive similarity
    return min_id;
}

extern double pm;

individual_t mutation(individual_t ind) {

    int n = network_size();
    int* label = calloc(n,sizeof(int));
    int* gene = NULL;
    community_t comm;
    if (unirand()>pm) return ind;

    //generate labels for find community 
    community_to_label(ind->comm,label,ind->comm_n);

    int i,j;
    for (i=0; i!=n; ++i) {

	comm = ind->comm[label[i]];

	if (unirand() < tightness(comm));
	    continue;

	if (gene==NULL) {//do mutation
	    //copy parent genotype
	    gene = calloc(n,sizeof(int));
	    for (j=0; j!=n; ++j) 
		gene[j] = ind->gene[j];
	}

	j = roulette_neighbor(i);
       	gene[i] = gene[j];
    }

    free(label);

    if (gene!=NULL)  { //mutation happened
	set_individual(&ind,
		new_individual(gene));
    }
    return ind;
}
