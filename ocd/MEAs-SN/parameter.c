#include<stdio.h>
#include<stdlib.h>
#include <time.h>

int popsize=100;
int T=20;
int generation = 50;
int verbose = 2;
double alpha = 1.0;
double pc = 0.7;
double pm = 1.0;
const char* network_file;

extern const char* operator;

void check_parameters() {
    if (T%2==0) T=T+1;
    if (pm>1) pm = 1.0;
    if (pc>1) pc = 1.0;
    if (T>popsize) {
	fprintf(stderr,"Parameter error: T(%d)>popsize(%d)\n",T,popsize);
	exit(-1);
    }
    if (popsize<=0) {
	fprintf(stderr,"Parameter error: popsize(%d)<=0\n",popsize);
	exit(-1);
    }
    if (popsize>10000) {
	fprintf(stderr,"Parameter error: popsize(%d)>10000\n",popsize);
	exit(-1);
    }
    if (T<=0) {
	fprintf(stderr,"Parameter error: T(%d)<=0\n",T);
	exit(-1);
    }
    if (generation<0) {
	fprintf(stderr,"Parameter error: generation(%d)<0\n",generation);
	exit(-1);
    }
}

void usage(const char* name) {
    fprintf(stderr,
	    "Usage: %s [parameter=value] network\n"
	    "paremeter can be:\n"
	    "\t-generation : max generation\n"
	    "\t-popsize    : population size\n"
	    "\t-t          : number of neighbors\n"
	    "\t-alpha      : exponent of tightness\n"
	    "\t-crossover  : probability of crossover\n"
	    "\t-mutaition  : probability of mutation\n"
	    "\t-seed       : random seed\n",
	    "\t-verbose    : verbose level\n",
	   name);
    exit(-1);
}

void get_parameters(int argc,char* argv[]) {
    int ai;
    double af;
    int i;
    unsigned int au,seed = time(NULL);
    for (i=1; i!=argc; ++i) {
	if (argv[i][0]=='-') {
	    switch (argv[i][1]) {
		case 'p':
		    if (sscanf(argv[i],"-popsize=%d",&ai)==1) popsize = ai;
		    else {
			fprintf(stderr,"Invalid options : %s\n",argv[i]);
			usage(argv[0]);
		    }
		    break;
		case 't':
		    if (sscanf(argv[i],"-t=%d",&ai)==1) T = ai;
		    else {
			fprintf(stderr,"Invalid options : %s\n",argv[i]);
			usage(argv[0]);
		    }
		    break;
		case 'g':
		    if (sscanf(argv[i],"-generation=%d",&ai)==1) generation = ai;
		    else {
			fprintf(stderr,"Invalid options : %s\n",argv[i]);
			usage(argv[0]);
		    }
		    break;
		case 'a':
		    if (sscanf(argv[i],"-alpha=%lf",&af)==1) alpha = af;
		    else {
			fprintf(stderr,"Invalid options : %s\n",argv[i]);
			usage(argv[0]);
		    }
		    break;
		case 'c':
		    if (sscanf(argv[i],"-crossover=%lf",&af)==1) pc = af;
		    else {
			fprintf(stderr,"Invalid options : %s\n",argv[i]);
			usage(argv[0]);
		    }
		    break;
		case 'm':
		    if (sscanf(argv[i],"-mutation=%lf",&af)==1) pm = af;
		    else {
			fprintf(stderr,"Invalid options : %s\n",argv[i]);
			usage(argv[0]);
		    }
		    break;
		case 's':
		    if (sscanf(argv[i],"-seed=%u",&au)==1) seed = au;
		    else {
			fprintf(stderr,"Invalid options : %s\n",argv[i]);
			usage(argv[0]);
		    }
		    break;
		case 'v':
		    if (sscanf(argv[i],"-verbose=%d",&ai)==1) verbose = ai;
		    else {
			fprintf(stderr,"Invalid options : %s\n",argv[i]);
			usage(argv[0]);
		    }
		    break;
		default:
		    fprintf(stderr,"Invalid options : %s\n",argv[i]);
		    exit(-1);
	    }
	} else {
	    if (network_file==NULL) {
		network_file = argv[i];
	    } else {
		fprintf(stderr,"Too many input files\n");
		exit(-1);
	    }
	}
    }
    check_parameters();

    if (network_file==NULL) usage(argv[0]);
    srand(seed);

    if (verbose==0) return;
    printf("%s:MEA_SN Algorithm %s,%u\n",argv[0],operator,seed);
    printf("generation : %d\n",generation);
    printf("popsize    : %d\n",popsize);
    printf("T          : %d\n",T);
    printf("alpha      : %lf\n",alpha);
    printf("pc         : %lf\n",pc);
    printf("pm         : %lf\n",pm);
    printf("network    : %s\n",network_file);
    printf("-----------------------------------\n");
}

