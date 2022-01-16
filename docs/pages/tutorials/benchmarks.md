---
layout: page
title: Benchmarks
---

# Benchmark Network Generation

WebOCD allows you to generate networks and corresponding ground-truth covers using __LFR__, __Signed LFR__, and __Newman__ benchmark algorithms. You can e.g. use these benchmarks to evaluate how different algorithms performed relative to the ground truth. You can get to the _Benchmarks_ page by clicking the _Benchmarks_ button on the navbar as shown below.

![benchmark-page-button](/REST-OCD-Services/assets/img/benchmarks_button.png "Benchmarks Page Button"){: width="900"; height="auto"}

Here you have the option to choose one of the available benchmark algorithms. You also need to provide the name of the graph (network) and its corresponding cover that will be generated. 

![benchmark-creation-page](/REST-OCD-Services/assets/img/benchmark_creation_page.png "Benchmark Creation Page"){: width="900"; height="auto"}

Once you select the algorithm, you'll be asked to enter the input parameters. These differ between the algorithms. You can also leave the input parameters with the default values. Once you finish the selection, click on the _Run Benchmark_ button at the bottom and this will generate the network and corresponding ground-truth cover.

![benchmark-parameter-selection](/REST-OCD-Services/assets/img/benchmark_parameter_selection.png "Benchmark Parameter Selection"){: width="900"; height="auto"}

The results can be viewed on the [_Networks_](/REST-OCD-Services/pages/tutorials/networks-covers-view#networks) and [_Community Detection_](/REST-OCD-Services/pages/tutorials/networks-covers-view#community-detection) pages respectively.


# Demo

Below you can see a gif of the whole process. 

Initially, we're on the _Network Analysis_ [page](/REST-OCD-Services/pages/tutorials/login#network-analysis-page). From here we click the _Benchmarks_ button in the navbar. This leads to the benchmark algorithm [page](/REST-OCD-Services/pages/tutorials/benchmarks#benchmark-network-generation).

Here we choose one of the available algorithms, in this case, __LFR__. This opens a page where we need to enter the desired input parameters for __LFR__. In this case, we choose to set __k__ to 10, __n__ to 500, and __on__ to 50. All other parameters are left with the default values. After selecting the parameters, we choose names for the network (graph) and the cover that will be generated. Clicking on &nbsp;  ![run-benchmark-btn](/REST-OCD-Services/assets/img/run_benchmark_btn.png "Run Benchmark Button") &nbsp; runs the algorithm. This redirects us to the _Network Analysis [page](/REST-OCD-Services/pages/tutorials/login#network-analysis-page). 

Here, under the _Running Benchmarks and Algorithms_ tab, we can see that the algorithm is running. After several seconds we refresh the page and see that the _Running Benchmarks and Algorithms_ tab is empty. This indicates that the algorithm execution is finished and there are no running algorithms. To view our freshly generated LFR network and cover, we click the _Community Detection_ button which displays the [page](/REST-OCD-Services/pages/tutorials/networks-covers-view#community-detection) with all the covers (in this case only one).


![benchmark-aglrothm-execution](/REST-OCD-Services/assets/gifs/benchmark.gif "Benchmark Algorithm Execution")