models for fitting particles
===============================================================================

maximize probability, if same number of particles:

P(matching(u,v))
= MUL_i gaussian(u_i-v_i,sigma_i)
= e^[ SUM_i (u_i-v_i)^2 / sigma_i^2  ] * const


can easily add a mu above as well, for drift.
this is now a bipartite matching problem



next, possible to also compare features. here, makes most sense to let user provide a pm instead!
= e^[ SUM_i (u_i-v_i)^2 / sigma_i^2 + weight_j (f_i,j-g_i,j)^2 / sigma_i,j^2  ] * const


or provide another gui to select the features, with options for:
* drift for features, and weight, and variance

it would be great to have these auto-calculated given a trainingset (lineage). hm. here have to go backwards,
find segmented region using midpoint of lineage point. 
* use that the problem is very very convex. can solve for a single parameter at a time
* later, if enough data, can consider a multivariate model also using covariances. for now, totally overkill

* once sigma and mu fit for individual parameter (can probably be done one at a time), 
  solve for weights such that this is the best match. complicated!!!! how to?
  
  
can sample a couple of other solutions and insert weights. should try to maximize the first equation, and minimize the rest.
could take the sum of differences, and maximize this single equation? hm. this is going to be an unbounded linear equation.






option: greedy solver, optimal solver







options in common gui
========================================

frame, group
buttons for next,last,etc


flatten... useful actually, keep?



