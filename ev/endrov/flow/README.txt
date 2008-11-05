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