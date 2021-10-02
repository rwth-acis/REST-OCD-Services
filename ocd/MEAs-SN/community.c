#include "community.h"
#include <stdlib.h>
#include <math.h>

extern double alpha;

/*make a community with a vertice i
 * community_t use a linklist to store its vertices
 * */
community_t new_community(int i) {
    community_t c = malloc(sizeof(struct community));

    /*insert node to community*/
    links_t t = malloc(sizeof(struct link));
    t->to = i;
    t->next = NULL;
    c->head = c->rear = t;

    /*init pin,pout,nin,nout
     * as only 1 vertices in community
     * there is no edges (both + and -)
     * in the community, and the edges
     * intercommunity is all the edge of the
     * vertice */
    c->pin = c->nin = 0.0;
    c->pout = degree_p(i);
    c->nout = degree_n(i);

    c->size = 1;
    return c;
}

/*calculate the sum of similarity between
 * two communities c1 and c2
 * between_p returns the sum of all the
 * positive similarities between c1,c2
 * the value is useful in merge two communities*/
double between_p(community_t c1,community_t c2) {
    double b=0;
    int i,j;
    links_t p1,p2;
    for (p1=c1->head; p1!=NULL; p1=p1->next) 
    	for (p2=c2->head; p2!=NULL; p2=p2->next) {
	    i = p1->to; j = p2->to;
	    if (similarity(i,j)>0) //+
		b += similarity(i,j);
	}
    return b;
}

double between_n(community_t c1,community_t c2) {
    double b=0;
    int i,j;
    links_t p1,p2;
    for (p1=c1->head; p1!=NULL; p1=p1->next) 
    	for (p2=c2->head; p2!=NULL; p2=p2->next) {
	    i = p1->to; j = p2->to;
	    if (similarity(i,j)<0)//-
		b += similarity(i,j);
	}
    return b;
}

/*merge two communities c1,c2
 * the sum similarity between c1,c2
 * is often calculated before, so
 * we use it as bp(+) bn(-) directly
 * */
double merge(community_t c1,community_t c2,
	double bp,double bn) {
    /*conect linklist*/
    c1->rear->next = c2->head;
    c1->rear = c2->rear;

    /*update pin,pout,nin,nout*/
    c1->pin += c2->pin + 2*bp;
    c1->nin += c2->nin + 2*bn;
    c1->pout += c2->pout - 2*bp;
    c1->nout += c2->nout - 2*bn;

    c1->size += c2->size;

    /*make c2 a empty community*/
    c2->head = c2->rear = NULL;
    free(c2);
}

/*calculate tightness of community*/
double tightness(community_t c) {
    return (c->pin - c->nin) /
	pow((c->pin)-(c->nin)+(c->pout),alpha);
}

/*release a community*/
void free_community(community_t c) {
    links_t t;
    while (c->head) {
	t = c->head;
	c->head = t->next;
	free(t);
    }
    c->rear = NULL;
    free(c);
}

/*calculate Tightness(c1 | c2) - Tightess(c1)
 *it is used before merge
 *as bp,bn is calculated before
 *judging merge or not,
 * and merge operation is *fast.
 */
double tightness_inc(community_t c1,community_t c2,
	double bp,double bn) {

    double t1 = tightness(c1);

    /*pin,pout,nin,nout if merged*/
    double pin = c1->pin + c2->pin + 2*bp;
    double nin = c1->nin + c2->nin + 2*bn;
    double pout = c1->pout + c2->pout - 2*bp;
    double nout = c1->nout + c2->nout - 2*bn;

    return (pin-nin)/pow(pin-nin+pout,alpha) - t1;
}

int community_size(community_t c) {
    return c->size;
}

/*number of joint vertices in two communities
 * c1 and c2. It is used in overlapping
 * community detection*/
int joint_size(community_t c1,community_t c2) {
    char* label = calloc(network_size(),sizeof(char));
    links_t p;
    int n=0;
    //mark label if a vertice in c1
    for (p=c1->head; p!=NULL; p=p->next)
	label[p->to] = 1;

    //check label of vertices in c2
    //if labeled, it is a joint vertices
    for (p=c2->head; p!=NULL; p=p->next) {
	if (label[p->to]) n++;
    }
    free(label);
    return n;
}

void joint(community_t c1,community_t c2) {
    char* label = calloc(network_size(),sizeof(char));
    double bp,bn;
    int i;
    community_t t;
    links_t p;

    //mark label
    for (p=c1->head; p!=NULL; p=p->next)
	label[p->to] = 1;

    while (c2->head) {
	p = c2->head;
	c2->head = p->next;
	i = p->to;
	if (label[i]==0) { //vertice i not in c1

	    /*add vertice i to community c1.
	     *using new_community here makes
	     a vertice into a single node community
	     so we can use between and merge
	     to merge 2 communities with
	     pin,pout,nin,nout updating*/

	    t = new_community(i);
	    bp = between_p(c1,t);
	    bn = between_n(c1,t);
	    merge(c1,t,bp,bn);
	}
	free(p);
	/*after walk through c2, c2 will be empty*/
    }
    free(c2);
    free(label);
}

/*convert a label array p into communities c
 *and return the number of communitis */
int label_to_community(int* p,community_t* c) {
    int n = network_size();

    /*int* l records the communities 
     * the vertice with specific label used to belong*/
    int* l = calloc(n,sizeof(int));
    int cn = 0;//number of communities
    int label;
    double bp,bn;
    int i;
    community_t comm;

    for (i=0; i!=n; ++i) {
	label = p[i];

	/*new community
	 * if will merge to other community, we
	 * can use between and merge function
	 * if will standalone, 
	 * it is alread done by this*/
	c[cn] = new_community(i);

	/*l[label]==0 means no vertice with the
	 * same label(same community) has been
	 * handled. standalone and mark this
	 * label as the (cn+1)th community*/
	if (l[label]==0) l[label] = ++cn;
       	else {
	    /*there is a community for the
	     * vertices with the same label.
	     * merge to that community*/
	    comm = c[l[label]-1];
	    bp = between_p(c[cn],comm);
	    bn = between_n(c[cn],comm);
	    merge(comm,c[cn],bp,bn);
	}
    }
    free(l);
    return cn;
}

/*find a community contains the vertice node
 * in cn communities c.
 * for overlapping communitis, the return community
 * is the first community contains the vertice*/
community_t find_community(int node,
	community_t* c,int cn) {
    int i;
    links_t p;
    for (i=0; i!=cn; ++i) {
	for (p=c[i]->head; p!=NULL; p=p->next)
	    if (p->to == node) return c[i];
    }
    return NULL;
}

/*convert community structure into labels
 *it is only used in separated community detection*/
void community_to_label(community_t* c,int* l,int n) {
    links_t p;
    int i;
    for (i=0; i!=n; ++i) {
	for (p=c[i]->head; p!=NULL; p=p->next)
	    l[p->to] = i;
    }
}
