#include<stdlib.h>

int* shuffle(int* p,int n) {
    int i,j,t;
    for (i=0; i!=n; ++i) {
	j = rand()%(n-i)+i;
	if (i!=j) {
	    t=p[i]; p[i]=p[j]; p[j]=t;
	}
    }
    return p;
}

double unirand() {
    return (double)rand()/RAND_MAX;
}

