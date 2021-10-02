

output: main.o population.o random.o individual.o community.o network.o operator_overlap.o  operator_separate.o parameter.o
	gcc -o output main.o population.o random.o individual.o community.o network.o operator_overlap.o parameter.o 

main.o: main.c population.h 
	gcc -c main.c -o main.o

population.o: population.c population.h random.h
	gcc -c population.c -o population.o

random.o: random.c random.h
	gcc -c random.c -o random.o

individual.o: individual.c individual.h
	gcc -c individual.c -o individual.o

community.o: community.c community.h
	gcc -c community.c -o community.o

network.o: network.c network.h
	gcc -c network.c -o network.o

operator_overlap.o: operator_overlap.c individual.h random.h
	gcc -c operator_overlap.c -o operator_overlap.o

parameter.o: parameter.c
	gcc -c parameter.c -o parameter.o
	
operator_separate.o: operator_separate.c individual.h random.h 
	gcc -c operator_separate.c -o operator_separate.o