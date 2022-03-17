---
layout: page
title: Cooperation Simulations
---

# Cooperation & Defection 
The service is able to perform simulations of cooperation & defection on the graphs. This functionality is located in the _cooperation_ package. The _cooperation.simulation_ package contains the simulation logic and the _cooperation.data_ package contains the persistence entities and the evaluation logic.

## Simulation
Use the **SimulationBuilder** class to construct simulations. A valid Simulation needs a game, a dynamic, a break condition and a graph. The games, dynamics and break conditions are located in a distinct subpackage. Every of these comes with its own factory class and Enum identifier. To construct a simulation it is sufficient to initialize the SimulationBuilder with the enum types. 

For the simulation, every node of the graph is considered as an agent. Every agent has a strategy and a payoff. Every round the agent get a new strategy by the dynamic and a new payoff by the game. The break condition determines the end of the simulation. 

The end of simulations creates a **SimulationSeries** object that consists of multiple SimulationData objects. That's because a simulation is performed multiple times according to the iterations parameter. Every SimulationSeries have an embedded _Evaluation_ object that calculates statistical measures between the different results of the simulation datasets.

The _Mapping_ objects are used to create correlations between the cooperativity values and the network/cover properties. For every distinct property, a mapping holds a distinct _CorrelationDataset_ object. These objects are essential lists that hold the double values, that get associated with the _Correlation_ enum.

## Service

All resources for the cooperation simulations are accessible through the simulation resource. Note that in contrast to the graph and cover resource, the simulation resource uses **JSON** objects instead of XML objects. Also, it uses the standard serializer and does not need the adapter package as well as the requestHandler. The Simulation entities are accessed by its own SimulationEntityHandler.

# Development
Additional games, dynamics, and abort conditions can be added according to the same principle as in the rest of the service.

## Implement a new game
Currently, only normal form games are supported. Therefore, the game distinction is done by different game parameters, while the game logic remains the same. To create a new one you have to create a new entry in the _GameType_ enum.

## Implement a new dynamic
A new dynamic can be implemented in two steps.
* Create a new class that extends the abstract class _Dynamic_
* Create a new entry in the _DynamicType_ enum

Every dynamic subclass has to implement the function 
`public boolean getNewStrategy(Agent agent, Simulation simulation)`. This function is called once for all agents in every round. The function should return the new strategy of the agent for the next round (true-cooperate false-defect)

In the constructor, the associated class have to be specified. Further, every dynamic need a unique id. The shortcut and the description are displayed to the user.