# Graph Formats

* Graph ML
* GML
* Weighted, Unweighted, Node Weighted Edgelist
* Adjacency Matrix (Currently only with positive values)
* XGMML
* Custom Formats
  * XML Content
  * Text Content

## XML Content

### For xml data the following parameters are mandatory for the post request:

* _inputFormat_: XML (The value XML will select the _XMLGraphInputAdapter_ to parse the xml data)
* _startDate_: 2004-01-01 (Starting date from which the data rows are considered)
* _endDate_: 2004-01-01 (Ending date till which the data rows are considered)
* _indexPath_: C:\indexes (set the index directory for faster computation)
* _filePath_: ocd/test/input/stackexAcademia.xml (The path of the xml file)

### Structure of xml data
The _XMLGraphInputAdapter_ will parse the xml data in a specific format. If the data is not arranged as it should be, the service won’t work. The xml data rows should have those mandatory attributes,

**Id**    **ParentId**    **CreationDate**    **Body**    **OwnerUserId**/**OwnerDisplayName**

Here,
* _Id_: The id of the post.
* _ParentId_: If the post is the reply of another post.
* _CreationDate_: This is the creation date of the post.
* _Body_: It is the main post of message of the post.
* _OwnerUserId_: The user id who creates this post. If this attribute is not available OwnerDisplayName will be considered.
* _OwnerDisplayName_: The display name of the user who creates this post.

_N.B._: Id and ParentId are mandatory for the structural method and creates eagles between nodes. Like someone asks a question inside another post. So the child post has its own id and also the parent post id. So the post node creates node with the parent node. To get an example of XML data which is supported by OCD service follow this file [stackexAcademia.xml](https://raw.githubusercontent.com/wiki/rwth-acis/REST-OCD-Services/demo/stackexAcademia.xml).


## Text Content / Node Content Edgelist
### For text files the following parameters are mandatory for the post request:

* _inputFormat_: NODE_CONTENT_EDGE_LIST (The value NODE_CONTENT_EDGE_LIST will select NodeContentEdgeListGraphInputAdapter to parse plain text file)
* _startDate_: 2004-01-01 (Starting date from which the data rows are considered)
* _endDate_: 2004-01-01 (Ending date till which the data rows are considered)
* _indexPath_: C:\indexes (set the index directory for faster computation)
* _body_: (This field will have the all text of the text file)

### Structure of text data
The _NodeContentEdgeListGraphInputAdapter_ will parse the text file in a specific format. If the data is not arranged as it should be, the service won’t work. The text data rows should have those mandatory attributes:

**SENT_BY**    **REPLIES_TO**    **CONTENT**    **DATE**
* _SENT_BY_: It is the id of the user who write the post
* _REPLIES_TO_: This is the id of the user in which post a user replies to
* _CONTENT_: The main message of the post.
* _DATE_: Creation date of the post.

_N.B._: The 1st line of the text file should be the headers of the column. If there is no line break between lines that would be ok and if there is line break that would also alright. To get example of text data which is supported by OCD service follow these files: [JMOL.txt](https://raw.githubusercontent.com/wiki/rwth-acis/REST-OCD-Services/demo/JMOL.txt), [BioJava.txt](https://raw.githubusercontent.com/wiki/rwth-acis/REST-OCD-Services/demo/BioJava.txt), [PGSQL_200.txt](https://raw.githubusercontent.com/wiki/rwth-acis/REST-OCD-Services/demo/PGSQL_200.txt). There are also another text files [UrchTest.txt](https://raw.githubusercontent.com/wiki/rwth-acis/REST-OCD-Services/demo/UrchTest.txt) and [URCH_POSTS.txt](https://raw.githubusercontent.com/wiki/rwth-acis/REST-OCD-Services/demo/URCH_POSTS.txt). But for those two files the attribute CREATION_TIMESTAMP should be changed to DATE.


# Big Graph
### Store: 

The API `POST ocd/storegraph` can be used to store big graph step by step.

Parameters:
* _name_: Name of the graph.
* _body_: The content of the graph which will append with the previous content.
For example, a graph of 360000 lines could be divided into any number of lines. For instance, that graph is divided into 36 parts and each part contains 10000 lines. Now, those parts could be sent step by step. Initially, the name and the content of the first part should send to the 'ocd/storegraph' API. For rest of the parts, the name should be same and the content should be the text of corresponding the pats.

### Process(Save in database): 

The API `POST /ocd/processgraph` can be used to insert the stored graph into the database. The processgraph API is same as uploading graph methods as described before in [this](https://github.com/rwth-acis/REST-OCD-Services/wiki/Integration-Tutorial#upload) page. But the graph name should be same as the name by which the graph was stored. And there is no _body_ parameter of the request. After sending this request, A graph id will be generated.

# Cover Formats

## Community Members Lists
---
layout: page
title: Uploading Graphs, Covers and Centralities
---

Each line of input contains first the name of a community (optional, if a community name is the same as a nodes name in the graph, it will also be considered as a node name) and then the names of the member nodes (Nodes of these names must exist in the actual graph), using the space character (' ') as a delimiter:

**Comm0 Javert Babet Brujon**\
**Comm1 Valjean Javert Labarre**

Nodes will be considered to have an equal membership degree for each community they are associated with.

## Node Community Lists

Each line of input contains first a node name (which must exist in the actual graph) and then an arbitrary number of community names, using the space character (' ') as a delimiter. There must be exactly one line for each node. An example can be seen below:

**Javert Comm0 Comm2**\
**Valjean Comm1 Comm2**

Nodes will be considered to have an equal membership degree for all communities they are associated with.

## Labeled Membership Matrix

Each line of input contains first a node name (which must exist in the actual graph) and then n double values (where n is some natural number), using the space character (' ') as a delimiter. The i-th double value of a row will define the node's membership degree for the i-th community. An example of this is seen below:

**Javert 0.4 0.6**\
**Valjean 0.3 0.7**

There must be exactly one line for each node of the graph and each line must have the same number ("n") of double values.

# Centrality Formats

## Node Value List

Each line of input contains first a node name (which must exist in the actual graph) and then **one** double value, using the space character (' ') as a delimiter:

**Javert 0.8**\
**Valjean 1.0**