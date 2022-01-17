---
layout: page
title: Metrics
---

# Metrics

WebOCD allows you to use various metrics on network covers to analyze them. To view metrics of a given cover, you need to navigate to the page of a [specific cover](/REST-OCD-Services/pages/tutorials/networks-covers-view#specific-cover). Here if you click on the _Metrics_ tab, you will see all the available metric values for the given cover. Initially, this will only include execution time (or be empty if cover was imported). In order to generate additional metrics, you need to click the _Run Metric_ tab.


<p align="center">
    <img  src="/REST-OCD-Services/assets/img/metrics_tab.png">
</p>

 Below you can see how we execute _Omega index_ metrics on a network generated through an [OCD algorithm](/REST-OCD-Services/pages/tutorials/ocd-algorithms). After clicking the _Run Metric_ tab, we need to choose the metric we want to execute. In this case, we choose _Omega Index_, which is a knowledge-driven metric (hence we select _Knowledge Driven Measure_ as metric type). We also need to choose another cover, relative to which the _Omega Index_ value will be calculated. For this, we choose the ground truth [cover](/REST-OCD-Services/pages/tutorials/benchmarks#demo), generated using the LFR benchmark. Clicking on the  &nbsp;  ![run-metric-btn](/REST-OCD-Services/assets/img/run_metric_btn.png "Run Metric Button") &nbsp; button executes the metric. This redirects us to the [_Network Analysis_](/REST-OCD-Services/pages/tutorials/login#network-analysis-page) page. 

Here, under the _Running Metrics_ tab, we can see that the algorithm is running. After several seconds we refresh the page and see that the _Running Metrics_ tab is empty. This indicates that the algorithm execution is finished and there are no running algorithms. To view our freshly calculated metric value, we click the _Community Detection_ button which displays the [page](/REST-OCD-Services/pages/tutorials/networks-covers-view#community-detection) with all the covers. Here we click on our cover and are redirected to the [page](/REST-OCD-Services/pages/tutorials/networks-covers-view#specific-cover) of that specific cover. Here we click on the _Metrics_ tab and see the value of _Omega Index_.

![benchmark-aglrothm-execution](/REST-OCD-Services/assets/gifs/metric_execution.gif "Benchmark Algorithm Execution")



