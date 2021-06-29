#include "individual.h"
#include "random.h"
#include <stdlib.h>

const char* operator = "overlapping";

/*decode a permutation into an individual
 *the function allocate individual's genotype
 *use the arg or function*/
static
individual_t decode_no_alloc(int* gene) {
    int n = network_size();
    community_t * c = calloc(n,sizeof(community_t));
    int cn=0; //community number

    c[cn++] = new_community(gene[0]);

    int i,j;
    int flag;
    double bp,bn;
    
    //insert vertice into communities
    for (i=1; i!=n; ++i) {
	flag = 0;
	c[cn] = new_community(gene[i]);
	for (j=0; j!=cn; ++j) {
	    bp = between_p(c[j],c[cn]);
	    bn = between_n(c[j],c[cn]);
	    if (tightness_inc(c[j],c[cn],bp,bn)>0) {
		//merge but not stop, so overlapping
		merge(c[j],c[cn],bp,bn);

		//as c[cn] is merged into c[j]
		//so make c[cn] a single vertice
		//community for merge again
		c[cn] = new_community(gene[i]);
		flag = 1; //added flag
	    }
	}
        //no added, vertice stand alone
	if (flag==0) cn++; 
	//added but a standalone community remains
	else free_community(c[cn]);
    }

    int size1,size2,size_joint;
    do {

	flag = 0; //joint flag

	//for each two different communities
	for (i=0; i!=cn; ++i) {
	    for (j=i+1; j!=cn; ++j) {
		size1 = c[i]->size;
		size2 = c[j]->size;
		size_joint = joint_size(c[i],c[j]);

		if (size_joint * 2 > size1 ||
			size_joint * 2> size2) {
		    flag = 1; //joint happened
		    joint(c[i],c[j]);
		    //number of communitis -1
		    //put the last community
		    //to position j as joint(c[i],c[j])
		    c[j] = c[--cn];
		    break; //joint a pair once
		}
	    }
	    if (flag) break; //joint a pair once
	}
    } while (flag);

    individual_t ind = 
	malloc(sizeof(struct individual));

    ind->gene = gene; //use gene directly with out copy
    ind->comm = realloc(c,cn*sizeof(community_t));
    ind->comm_n = cn;
    ind->refcount = 1;

    eval_individual(ind);
    return ind;
}

/* call decode_no_alloc after copy gene
 * */
individual_t decode(int* gene) {
    int n = network_size();
    //copy first
    int* g = calloc(n,sizeof(int));
    while (n--) g[n] = gene[n];

    return decode_no_alloc(g);
}

/*copy n elements for s to d
 * only if an element is NOT marked by ignore
 * returns the number of elements handled in s
 * when copy is over
 * used in PMX crossover*/
static int copy_with_ignore(int* d,int* s,char* ignore,int n) {
    int i=0,j=0; //i for s, j for d
    while (j<n) { 
	if (ignore[s[i]]==0)
	    d[j++] = s[i];
	i++;
    }
    return i;
}

extern double pc;
individual_t crossover(individual_t p1,
	individual_t p2) {

    //if not cross over, child is
    //as same as p1
    if (unirand() > pc) {
	(p1->refcount)++;
	return p1;
    } 

    int n = network_size();
    int* gene = calloc(n,sizeof(int));

    int p,q,t;
    
    //find beginning and ending point
    //for crossover randomly
    //crossover at [p,q)
    p = rand()%n; //beginning
    q = rand()%(n-p) + p +1; //endding

    int i;

    //ignore flags
    //set p2[p,q) to be ignore
    char* ignore = calloc(n,sizeof(char));
    for (i=p; i!=q; ++i) {
	ignore[p2->gene[i]] = 1;
    }
    
    //copy the left part  : p1[0,p)
    i = copy_with_ignore(gene,p1->gene,ignore,p);
    //copy the right part : p1[q,n)
    copy_with_ignore(gene+q,(p1->gene+i),ignore,n-q);

    //copy the middle part : p2[p,q)
    for (i=p; i!=q; ++i) gene[i] = p2->gene[i];

    free(ignore);

    //make child of using gene
    //use decode_no_alloc to avoid copy gene again
    individual_t child = decode_no_alloc(gene);

    return child;
}

extern double pm;

individual_t mutation(individual_t p) {
    int i,j,t;
    int n = network_size();
    int* gene = NULL;

    //for each position in gene
    for (i=0; i!=n; ++i) {
	if (unirand()>pm/n) continue;

	//do mutation, copy gene first
	if (gene ==NULL) {
	    gene = calloc(n,sizeof(int));
	    for (j=0; j!=n; ++j)
		gene[j] = p->gene[j];
	}

	//find another position j, swap(i,j) 
	j = rand() % n;
	if (i!=j) {
	    t=gene[i]; gene[i]=gene[j]; gene[j]=t;
	}
    }

    if (gene!=NULL) { //mutation happend
	set_individual(&p,decode_no_alloc(gene));
    }
    return p;
}

