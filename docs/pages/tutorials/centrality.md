---
layout: page
title: Centrality
---
# Centrality

In WebOCD you have an option to calculate various centrality values on the available networks. To do so, you need to navigate to the [page](/REST-OCD-Services/pages/tutorials/networks-covers-view#specific-network) of a specific network. Here you need to click on the _Run Centrality Calculation_ tab. As you can see in the image below, here you can select from a large variety of centrality measures.

![centrality-measure-selection](/REST-OCD-Services/assets/img/centrality_measure_selection.png "Centrality Measure Selection"){: width="900"; height="auto"}

After you finish the selection, clicking the &nbsp; ![run-btn](/REST-OCD-Services/assets/img/run_btn.png "Run Button") &nbsp; button calculates the centrality. To view the centralities, you need to click on the _Centrality_ button on the navbar. This will redirect you to a page that lists all the available centrality calculation results. In the example below, we only have two such result.

![centrality-page](/REST-OCD-Services/assets/img/centrality_page.png "Centrality Page"){: width="900"; height="auto"}

By clicking on the desired centrality result, you go to the page of that specific centrality, where you can acquire more detailed information. 

# Display Centrality Values

In order to view centrality measurements for each node, you can click on the _Centrality Values_ tab.

![centrality-values-tab](/REST-OCD-Services/assets/img/centrality_values_tab.png "Centrality Values Tab"){: width="900"; height="auto"}

Here you have a choice to display values for all nodes, or values for top K nodes. It can be practical not to display all values when the network is large. 

<p align="center">
    <img width="50%" height="auto" src="/REST-OCD-Services/assets/img/show_all_nodes.png">
</p>

This process is shown in the gif below. Here we select to display _Degree Centrality_ values of the top 10 nodes. We also tick the _High Precision_ box to increase the decimal precision of displayed values. As you can see on the gif below, node 99 has the highest value for a given centrality.

![centrality-values](/REST-OCD-Services/assets/gifs/centrality_values.gif "Centrality Values")

# Visualize Centrality Values

Instead of plain data, you might want to visualize centrality values. You can do so by clicking on the _Visualization_ tab, which is similar to [visualization for covers](/REST-OCD-Services/pages/tutorials/visualization). This will display a network, the nodes of which will be affected by centrality values. By default, centrality values affect the node size. This can be seen in the gif below. As you see, node 99 that has the highest centrality value is also the largest node.

![centrality-default-visualization](/REST-OCD-Services/assets/gifs/centrality_default_visualization.gif "Centrality Default Visualization")

You can also display the centrality network in a different way. For example, instead of node sizes, node colors can be changed depending on the centrality values. This is visualized below.

![centrality-different-visualizations](/REST-OCD-Services/assets/gifs/centrality_different_visualizations.gif "Centrality Different Visualizations")

# Evaluation Mode

WebOCD gives you the option to compare and evaluate different centrality measures. To do so, go to the centrality [page](/REST-OCD-Services/pages/tutorials/centrality) and click on the &nbsp; ![evaluation-mode-btn](/REST-OCD-Services/assets/img/evaluation_mode_btn.png "Evaluation Mode Button") &nbsp; button. This will open a page where the available centrality results are displayed. Here you need to select the centralities you want to evaluate (click on them) and then the desired type of evaluation (__Average, Compare, Correlation, Precision__). 

<p align="center">
    <img width="70%" height="auto" src="/REST-OCD-Services/assets/img/evaluation_mode.png">
</p>

Below you can see a small demo of calculating the _Pearson correlation_ matrix of  _Degree Centrality_ and _In-Degree_ centrality values of a network.


![evaluation-mode](/REST-OCD-Services/assets/gifs/evaluation_mode.gif "Evaluation Mode")