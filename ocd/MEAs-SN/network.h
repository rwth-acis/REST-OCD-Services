#ifndef JRIVER_MEASN_NETWORK_H
#define JRIVER_MEASN_NETWORK_H

/*a linklist*/
typedef struct link{
    int to;
    struct link* next;
} *links_t;

void read_pajek(const char*);
int  network_size();
double weight(int,int);
double similarity(int,int);
links_t neighbor(int);
void  free_network();
double degree_p(int);
double degree_n(int);
int    is_network_ok();

#endif
