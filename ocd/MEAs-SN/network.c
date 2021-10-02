#include "network.h"
#include <stdlib.h>
#include <stdio.h>
#include <math.h>

extern int verbose;

struct network{
    size_t n;
    double *w,*s;
    double *dp,*dn;
    links_t *e;
};

/*global network data*/
static struct network* net;

/*nubmer of vertices*/
int network_size() {
    return net->n;
}

/*neighbors, return a link list*/
links_t neighbor(int i) {
    return net->e[i];
}

/*get W(i,j) weight of edge from i to j*/
double weight(int i,int j) {
    return net->w[i*(net->n) + j];
}

/*similarity between two vertices i & j
 * This is NOT the calculation function
 * just a interface
 */
double similarity(int i,int j) {
    return net->s[i*(net->n) + j];
}

/*sum of all the vertices of i's neighbor
 * which has positive similarities to i
 * This is NOT the calculation function
 */ 
double degree_p(int i) {
    return net->dp[i];
}
/*same as above*/
double degree_n(int i) {
    return net->dn[i];
}

/*free a linklist*/
static void free_links(links_t l) {
    links_t t;
    while (l) {
	t = l;
	l = l->next;
	free(t);
    }
}

/*free all the network*/
void free_network() {
    free(net->s);
    free(net->w);
    free(net->dp);
    free(net->dn);
    int i;
    for (i=0; i!=net->n; ++i) 
	free_links(net->e[i]);
    free(net->e);
    free(net);
    net = NULL;
}

int is_network_ok() {
    return net!=NULL;
}

/*add a edge from i to j with weight w
 *to network, this function only add a directed
 *link from i to j, but not j to i.
 *if the network is undirected,
 *add_link(i,j,w), add_link(j,i,w);
 */
static void add_link(int i,int j,double w) {
    links_t t = malloc(sizeof(struct link));
    t->to = j;
    t->next = net->e[i];
    net->e[i] = t;
    net->w[i*(net->n) + j] = w;
}

/*this is the function calculates the
 * similarity between vertice i and j
 */
static double sim(int i,int j) {
    links_t l;
    double uu=0,uv=0,vv=0;
    double ux,vx;
    /*calculate sum(W(i,x)^2) for all x*/
    for (l=net->e[i]; l!=NULL; l=l->next)
	uu += weight(i,l->to)*weight(i,l->to);

    /*calculate sum(W(j,x)^2) for all x*/
    for (l=net->e[j]; l!=NULL; l=l->next)
	vv += weight(j,l->to)*weight(j,l->to);

    /*calculate sum(W(i,x)*W(j,x) for all x*/
    for (l=net->e[i]; l!=NULL; l=l->next) {
	ux = weight(i,l->to);
	vx = weight(j,l->to);
	if (ux<0 && vx<0) continue;
	uv += ux*vx;
    }
    return uv / sqrt(uu) / sqrt(vv);
}

/*generate all the similarities between each
 * pair of vertices
 * this function will aslo calculate degree_p
 * and degree_n at the same time*/
static double* gen_sim() {
    double *s = 
	calloc((net->n)*(net->n),sizeof(double));
    int i,j;
    double x;
    links_t l;
    for (i=0; i!=net->n; ++i)
	//only neighbors have similarity
	for (l=net->e[i]; l!=NULL; l=l->next) {
	    j = l->to;
	    x = sim(i,j);
	    s[i*(net->n) + j] = x;
	    //calculate degree_n and degree_p
	    if (x>0) net->dp[i] += x;
	    else net->dn[i] += x;
	}
    return s;
}

/* read a pajek file */
void read_pajek(const char* name) {
    FILE* f = fopen(name,"r");
    if (f==NULL) return;

    net = malloc(sizeof(struct network));

    int n;
    int i,j;
    double w;
    int en=0; //number of edges

    if(verbose>=1)
       	printf("reading network : %s\n",name);
	
    fscanf(f,"%*s%d",&n);//ignore '*Vertices'

    if(verbose>=2) printf("vertices : %d\n",n);

    net->n = n;
    net->w = calloc(n*n,sizeof(double));
    net->dp = calloc(n,sizeof(double));
    net->dn = calloc(n,sizeof(double));
    net->e = calloc(n,sizeof(links_t));

    fscanf(f,"%*s"); //ignore '*Edge'

    while (fscanf(f,"%d%d%lf",&i,&j,&w)==3) {
	en ++;
	/*in pejak network file
	 * vertices begin from 1
	 */
	add_link(i-1,j-1,w); 
	add_link(j-1,i-1,w); //undirect
    }

    if(verbose>=2) printf("edges : %d\n",en);

    //generate similarity
    if(verbose>=3) printf("generating similarities\n");
    net->s = gen_sim();

    if (verbose>=1) printf("\n");
}
