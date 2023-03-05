---
layout: page
title: Integration
---

# Introduction
This tutorial will give you a brief explanation about how to handle the OCD Service. It explains a few basic requests for e.g. uploading a graph, retrieving the saved graphs, creating a cover from a graph etc.

However, this is only a fraction of what the service actually allows. Other functionalities include for instance the execution of metrics to determine the quality of the calculated covers or the computation of benchmark graphs and covers for the generation of standardized testing data.
At the bottom, an overview of all possible requests is given. For a more detailed description of the requests you should refer to the OCD Service's source code.

All responses other than actual graph and cover data (which has the specified output format), as well as JSON visualization data are sent in XML format.

For more detailed information about using WebOCD Service, refer to the [tutorials](/REST-OCD-Services/pages/tutorials/login), which explains usage of WebOCD Service with the [WebClient](https://github.com/rwth-acis/OCD-Web-Client)

# Graphs

## List Available Graphs

`GET graphs`

Returns the ids (or meta information) of multiple graphs.

Optional query parameters are:
+ _firstIndex_: The result list index of the first id to return. Defaults to 0.
+ _length_: The (maximum) number of ids to return. Defaults to _Long.MAX_VALUE_.
+ _includeMeta_: If TRUE, instead of the ids the META XML of each graph is returned. Defaults to FALSE.
+ _executionStatuses_: If set only those graphs are returned whose creation method has one of the given ExecutionStatus names. This is only relevant with respect to graphs created by benchmark models. Allowed values are _WAITING_ (graph computation has not started yet), _RUNNING_ (graph computation is in progress), _COMPLETED_ (graph computation has finished) and _ERROR_ (An error has occured during graph computation). Uploaded graphs will always have the execution status _COMPLETED_. Multiple status names are separated with the "-" delimiter. Defaults to the empty string which is equivalent to all statuses.


## Upload

`POST graphs`

Uploads a graph. The data describing the graph is sent in the request body in accordance with the input format. Returns the graph id.

Optional query parameters are:
+ _name_: Any arbitrary String to name the graph. Defaults to _unnamed_.
+ _creationType_: Meta information describing based on what the graph was created. Allowed values are _REAL_WORLD_ (based on real world data), NEWMAN (based on the Newman Benchmark Model), LFR (based on the LFR Benchmark Model) and _UNDEFINED_. Defaults to _UNDEFINED_.
+ _inputFormat_: The name of the graph input format. Allowed values are _GRAPH_ML_, _WEIGHTED_EDGE_LIST_, _UNWEIGHTED_EDGE_LIST_, _NODE_WEIGHTED_EDGE_LIST_, _GML_, _NODE_CONTENT_EDGE_LIST_ and _XML_ as well as _XGMML_. Note that only some of these format allow to pass edge weights. Defaults to _GRAHP_ML_.
+ _doMakeUndirected_: Optional query parameter. Defines whether directed edges shall be turned into undirected edges (TRUE) or not (FALSE). Defaults to _FALSE_.
+ _startDate_: The start date of the data rows which will take as a graph input.
+ _endDate_: The end date of the data rows which will take as a graph input.
+ _indexPath_: This is a path of the index for making graph faster.
+ _filePath_: This parameter is only for Stack Exchange XML data. This is mandatory for xml data file.
+ _body_: This will be the text of open source developer or learning forums data which would be a plain text file. This is mandatory for Node Content Edge List Graph Input.


## Retrieve

`GET graphs/{graphId}`

Returns the graph with id `{graphId}` in accordance with the specified output format.

Optional query parameters are:
+ _ouputFormat_: The name of the graph output format. Allowed values are _GRAPH_ML_, _WEIGHTED_EDGE_LIST_ and _META_XML_. Defaults to _GRAPH_ML_.

## Delete

`DELETE graphs/{graphId}`

Deletes the graph with id `{graphId}` and all covers belonging to it. Returns a confirmation.

# Covers

## List Available Covers

`GET covers`

Returns the ids (or meta information) of multiple covers.

Optional query parameters are:
+ _firstIndex_: The result list index of the first id to return. Defaults to 0.
+ _length_: The number of ids to return. Defaults to _Long.MAX_VALUE_.
+ _includeMeta_: If TRUE, instead of the ids the META XML of each graph is returned. Defaults to FALSE.
+ _executionStatuses_: If set only those covers are returned whose creation method status corresponds to one of the given ExecutionStatus names. Allowed values are _WAITING_ (cover computation has not started yet), _RUNNING_ (cover computation is in progress), _COMPLETED_ (cover computation has finished) and _ERROR_ (An error has occured during cover computation). Uploaded covers will always have the execution status _COMPLETED_. Multiple status names are separated using the "-" delimiter. Defaults to the empty string which is equivalent to all statuses.
+ _metricExecutionStatuses_: If set only those covers are returned that have a corresponding metric log with a status corresponding to one of the given ExecutionStatus names. Allowed values are _WAITING_ (metric computation has not started yet), _RUNNING_ (metric computation is in progress), _COMPLETED_ (metric computation has finished) and _ERROR_ (An error has occured during metric computation). Multiple status names are separated using the "-" delimiter. Defaults to the empty string which results in not filtering out any covers.
+ _graphId_: If set only those covers are returned that are based on the corresponding graph.

## Retrieve

`GET covers/{coverId}/graphs/{graphId}`

Returns the cover with id `{coverId}` belonging to the graph with id `{graphId}` in a specified format.

Optional query parameters are:
+ _outputFormat_: The cover output format. Allowed values are _META_XML_, _DEFAULT_XML_ and _LABELED_MEMBERSHIP_MATRIX_. Detaults to _LABELED_MEMBERSHIP_MATRIX_.

## Delete

`DELETE covers/{coverId}/graphs/{graphId}`

Deletes the cover with id `{coverId}` belonging to the graph with id `{graphId}`. If the cover is still being created by an algorithm, the algorithm is terminated. If the cover is still being created by a ground truth benchmark, the benchmark is terminated and the corresponding graph is deleted as well. If metrics are running on the cover, they are terminated.

## Compute

`POST covers/graphs/{graphId}/algorithms`

Creates a new cover by running an overlapping community detection algorithm on the graph with id `{graphId}`. The graph must have the creation method status completed. The request body must have the format
`<parameters>`
`</parameters>`
where optionally more information may be included to set the algorithm's execution parameters different from the default settings.

Optional query parameters are:
+ _graphId_: The id of graph with which the algorithm would creates cover.
+ _name_: The name for the cover. Defaults to _unnamed_.
+ _algorithm_: The algorithm to execute. Allowed values are _RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM_, _SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM_, _EXTENDED_SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM_, _SSK_ALGORITHM_, _LINK_COMMUNITIES_ALGORITHM_, _WEIGHTED_LINK_COMMUNITIES_ALGORITHM, CLIZZ_ALGORITHM_, _MERGING_OF_OVERLAPPING_COMMUNITIES_ALGORITHM_ and _BINARY_SEARCH_RANDOM_WALK_LABEL_PROPAGATION_ALGORITHM_. Defaults to _SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM_.
+ _body_: The body is the default parameter for the algorithm selected. If a get request is sent to the running ocd service like this, http://<hostname>:8080/ocd/algorithms/<Name of the algorithm>/parameters/default, a xml will be returned. Just put the xml data in the _body_ perimeter.
+ _contentWeighting_: The default value is false, it can be true when we use content based algorithms.
+ _componentNodeCountFilter_: May be set to a natural number to consider each connected component of a size smaller than the filter as one separate single community (independent of the algorithm).

For example, we upload a graph and we get the graph id 7. Now, we want to run _SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM_ on it. Before running the algorithm, the default parameters for the algorithm in xml format from `GET /ocd/algorithms/{CoverCreationType}/parameters/default` API should be taken. Diffrent algorithms might have different parameters.

After that, make a post request to run the algorithm with the following parameters:

+ _graphId_: 7
+ _name_: Email_Cover
+ _algorithm_: _SPEAKER_LISTENER_LABEL_PROPAGATION_ALGORITHM_
+ _body_: `<?xml version="1.0" encoding="UTF-16"?>`
  `<Parameters><Parameter><Name>memorySize</Name><Value>100</Value></Parameter><Parameter><Name>probabilityThreshold</Name><Value>0.15</Value></Parameter></Parameters>`
+ _contentWeighting_: false
+ _componentNodeCountFilter_: 0

Then a cover is generated with a cover id 103.
