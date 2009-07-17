to become a generic library for graph algorithms

design
======
* look at LEDA
* look at BOOST
* look at jgrapht

interface for each type of graph

graph hierarchy
  weights
  planarity
  directionality
  simple graph
  
  
not a total ordering. keep properties separate




??? should vertices and edges be exposed? 


graph.setWeight(e, 50);  <--- weight, what type? Number?
vs
e.setWeight(50);
vs
graph.setWeight(v1,v2,5);



graph.addEdge(new DefaultEdge(....))
vs
e=graph.addEdge(v1,v2);
graph.setWeight(v1,v2,5);
e.::::



===== is the focus on edges or vertices
might be either!

addEdge("foo","bar")
vs addEdge(new DefaultNode("foo"), ...);


DefaultSimpleGraph can hide the embedding above. partially.
String s=getEdge
s.setWeight <--- does not work. need to expose embedding!


there might not be an edge object internally - adjacency array. 
is this why jgrapht looks like it does?
can still be created temporarily when needed.
or: can do a non-abstract Edge that sends calls to Graph. just for simplicity



=========================

http://portal.acm.org/citation.cfm?id=320229
my algorithm



        Computing Minimum-Weight Perfect Matchings
WILLIAM COOK Computational and Applied Mathematics, Rice University, 6100 Main Street, Houston, TX 77005-1892,
  ANDRE ROHE  Forschungsinstitut fur Diskrete Mathematik, Universitat Bonn, Lennestr. 2, 53113 Bonn, Germany,
						<<<<<alternative algorithm



implementation of O(nmlog n) weighted matchings the power of data structures
Mehlhorn