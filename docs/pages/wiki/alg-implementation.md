---
layout: page
title: OCD Algorithm Implementation
---

# Implementing an OCD Algorithm for WebOCD

To add a new OCD algorithm to WebOCD Service, you should first clone the [repository](https://github.com/rwth-acis/REST-OCD-Services) and switch to the *develop* branch. From the *develop* branch, you should create your own branch. 
Then, you should create an algorithm class file under \\
```REST-OCD-Services/rest_ocd_services/src/main/java/i5/las2peer/services/ocd/algorithms/MyAlgorithm.java``` \
 where ```MyAlgorith.java``` will be the class that holds your algorithm implementation. You can also add various utility classes that your algorithm might use. For this, there is a ```utils``` folder as shown below:

![algorithms](/REST-OCD-Services/assets/img/algorithms.png "Algorithms"){: width="auto"; height="auto"}

You should also add your algorithm class to [```CoverCreationType.java```](https://github.com/rwth-acis/REST-OCD-Services/blob/develop/rest_ocd_services/src/main/java/i5/las2peer/services/ocd/graphs/CoverCreationType.java) which is an enum holding possible cover creation types and this also includes OCD algorithms. This can be done as shown below:

![coverCreationType](/REST-OCD-Services/assets/img/coverCreationType.png "Extending Cover Creation Type"){: width="900"; height="auto"}

WebOCD offers an interface called [```OcdAlgorithm.java```](https://github.com/rwth-acis/REST-OCD-Services/blob/develop/rest_ocd_services/src/main/java/i5/las2peer/services/ocd/algorithms/OcdAlgorithm.java) which your algorithm class (in this case ```MyAlgorithm.java```) should implement. When you implement this interface, you will be required to implement the methods that are part of this interface, as shown below:

![implementing-OcdAlgorithm](/REST-OCD-Services/assets/img/implementing_OcdAlgorithm.png "Implementing OcdAlgorithm Interface"){: width="900"; height="auto"} 


* ```compatibleGraphTypes``` should return a set of graph types your algorithm is compatible with, for example, directed and signed networks. 
* ``` getParameters ``` and ``` setParameters ``` are getter/setter methods for the algorithm parameters. 
* ``` getAlgorithmType ``` should return the type of your algorithm, which you added to the [```CoverCreationType```](https://github.com/rwth-acis/REST-OCD-Services/blob/develop/rest_ocd_services/src/main/java/i5/las2peer/services/ocd/graphs/CoverCreationType.java) class. 
* ``` detectOverlappingCommunities ``` should return the resulting cover created as a result of executing your algorithm.

How your algorithm is implemented/executed is up to you. However, you should use WebOCD's own [```CustomGraph```](https://github.com/rwth-acis/REST-OCD-Services/blob/develop/rest_ocd_services/src/main/java/i5/las2peer/services/ocd/graphs/CustomGraph.java) and [```Cover```](https://github.com/rwth-acis/REST-OCD-Services/blob/develop/rest_ocd_services/src/main/java/i5/las2peer/services/ocd/graphs/Cover.java) classes. The cover that ```detectOverlappingCommunities``` method should return can easily be built using a ``` Cover ``` class constructor that takes in a graph (on which the algorithm was executed) and a membership matrix. In the membership matrix each row should correspond to a node and each column to a community. An entry *i,j* should represent how much does node *i* belong to community *j*. 


After implementing the algorithm, don't forget to add the test classes. If your algorithm is at a path \\ 
```REST-OCD-Services/rest_ocd_services/src/main/java/i5/las2peer/services/ocd/algorithms/MyAlgorithm.java``` \\
then your algorithm test class should be located at a path  \\
```REST-OCD-Services/rest_ocd_services/src/test/java/i5/las2peer/services/ocd/algorithms/MyAlgorithmTest.java```

That's it! your algorithm is now ready to be used within WebOCD. 