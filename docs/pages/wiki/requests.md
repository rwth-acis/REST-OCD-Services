---
layout: page
title: Possible Requests
---

# Possible Requests
### Algorithms
* `GET algorithms`
* `GET algorithms/{CoverCreationType}/graphTypes`
* `GET algorithms/{CoverCreationType}/parameters/default`

### Benchmarks
* `GET benchmarks`
* `GET benchmarks/{GraphCreationType}/parameters/default`

### Centralities
* `GET centralities`
* `GET centralities/creationtypes`
* `GET centralities/formats/output`
* `GET centralities/{CentralityMeasureType}/graphTypes`
* `GET centralities/{CentralityMeasureType}/parameters/default`
\
&nbsp;
* `POST centrality/graphs/{graphId}`
* `POST centrality/graphs/{graphId}/algorithms`
* `GET centrality/graphs/{graphId}/eigenvalue`
* `POST centrality/simulation/graphs/{graphId}`
* `GET centrality/{mapId}/graphs/{graphId}`
* `DELETE centrality/{mapId}/graphs/{graphId}`
\
&nbsp;
* `GET centralitysimulations`
* `GET centralitysimulations/{CentralitySimulationType}/graphTypes`
* `GET centralitysimulations/{SimulationType}/parameters/default`

### Covers
* `GET covers`
* `GET covers/creationtypes`
* `GET covers/formats/input`
* `GET covers/formats/output`
* `POST covers/graphs/{graphId}`
* `POST covers/graphs/{graphId}/algorithms`
* `GET covers/{coverid}/graphs/{graphId}`
* `DELETE covers/{coverid}/graphs/{graphId}`
* `POST covers/{coverid}/graphs/{graphId}/metrics/knowledgedriven/groundtruth/{groundTruthCoverId}`
* `POST covers/{coverid}/graphs/{graphId}/metrics/statistical`
* `DELETE covers/{coverid}/graphs/{graphId}/metrics/{metricId}`

### Evaluation
* `GET evaluation/average/graph/{graphId}/maps`
* `GET evaluation/correlation/{coefficient}/graph/{graphId}/maps`
* `GET evaluation/precision/{k}/graph/{graphId}/maps`

### Graphs
* `GET graphs`
* `POST graphs`
* `GET graphs/benchmarks`
* `GET graphs/creationtypes`
* `GET graphs/formats/input`
* `GET graphs/formats/output`
* `GET graphs/properties`
* `GET graphs/{graphId}`
* `DELETE graphs/{graphId}`

### Visualization
* `GET visualization/centralityMap/{centralityMapId}/graph/{graphId}/outputFormat/{VisualOutputFormat}/layout/{GraphLayoutType}/centralityVisualization/{CentralityVisualizationType}`
* `GET visualization/centralityVisualizationTypes/names`
* `GET visualization/formats/output/names`
* `GET visualization/cover/{coverId}/graph/{graphId}/outputFormat/{VisualOutputFormat}/layout/{GraphLayoutType}/paint/{CoverPaintingType}`
* `GET visualization/graph/{graphId}/outputFormat/{VisualOutputFormat}/layout/{GraphLayoutType}`
* `GET graphs/layout/names`
* `GET graphs/painting/names`

### Metrics
* `GET metrics`
* `GET metrics/knowledgedriven`
* `GET metrics/statistical`
* `GET metrics/{OcdMetricType}/parameters/default`

### Simulation
* `GET simulation`
* `POST simulation`
* `GET simulation`
* `GET simulation/conditions`
* `GET simulation/dynamics`
* `GET simulation/games`
* `GET simulation/group`
* `PUT simulation/group/mapping`
* `GET simulation/group/meta`
* `GET simulation/group/{groupId}`
* `DELETE simulation/group/{groupId}`
* `GET simulation/group/{groupId}/mapping`
* `GET simulation/group/{groupId}/table`
* `GET simulation/meta`
* `GET simulation/{seriesId}`
* `DELETE simulation/{seriesId}`
* `GET simulation/{seriesId}/parameters`
* `GET simulation/{seriesId}/table`

### Various
* `GET maps`
* `POST processgraph`
* `POST storegraph`
* `GET validate`