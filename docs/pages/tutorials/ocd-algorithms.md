---
layout: page
title: OCD Algorithms
---

# OCD Algorithms

To execute an OCD algorithm, you first need to choose a network. For this, you can click on the [_Networks_](/REST-OCD-Services/pages/tutorials/networks-covers-view#networks) or [_Community Detection_](/REST-OCD-Services/pages/tutorials/networks-covers-view#community-detection) buttons from the navbar and then on the desired network name. From the [page](/REST-OCD-Services/pages/tutorials/networks-covers-view#specific-network) of the specific network, click on the _Run OCD Algorithm_ tab as shown below.

<p align="center">
    <img  src="/REST-OCD-Services/assets/img/run_ocd_algorithm_btn.png">
</p>

Here you need to choose the name of the cover that will be generated as a result of the algorithm execution and the algorithm you want to execute. 

Below you can see the execution of __Detect Overlapping Community Detection__ algorithm on the [network](/REST-OCD-Services/pages/tutorials/benchmarks#demo) generated from an LFR benchmark.

![ocd-aglrothm-execution](/REST-OCD-Services/assets/gifs/ocd_algorithm_execution.gif "OCD Algorithm Execution")

__Description:__ fter selecting the algorithm, we need to choose the input parameters for the algorithm. In this case, we choose to set the only input parameter __overlappingThrehold__ to 0.5. We also choose a descriptive name, for the cover, based on the input parameter for later reference. Clicking the &nbsp; ![run-btn](/REST-OCD-Services/assets/img/run_btn.png "Run Button") &nbsp; button executes the algorithm. As always, this leads us to the _Network Analysis_ [page](/REST-OCD-Services/pages/tutorials/login#network-analysis-page), where all the executing algorithms are displayed. Once we refresh and see that the algorithm execution is complete, we go to the _Community Detection_ [page](/REST-OCD-Services/pages/tutorials/networks-covers-view#community-detection). Here we select our freshly created cover and display it in a [2D interactive format](/REST-OCD-Services/pages/tutorials/visualization#2d-interactive).