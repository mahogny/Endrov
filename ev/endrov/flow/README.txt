==semantics==

each unit has a map lastOutput. this list is updated whenever
1. an observer triggers an update. bottom-top
2. on evaluate, top-bottom

to support both strategies, there is one entirely stupid function
	evaluate()
which simply reads lastOutput and does something with it. if the dependent
output is not set, it should throw an exception.


== Semantic 1 ==

the observer will call updateBottomUp() which updates the output,
then calls recursively on all the components it outputs to. 


== Semantic 2 ==

updateTopBottom() will update dependencies if needed


== lazyness ==


== maps ==

possible to use the observer system as the input value changes.
as it is asynchronous, need to add synch again once the map output is
updated


== static typing ==

whenever possible, the should be something like
xxx run(xxx)
later allowing real-time code generation with compile-time typing. evaluate should call this
function. this is not strictly needed if the typing sucks because it can easily be detected
when no such function exists and evaluate will be used as a slow alternative.

