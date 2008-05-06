initev

loader=newclassloader;

%javatrue=java.lang.Boolean.TRUE

%main = loader.loadClass('EvMatlab').newInstance;
cl=loader.loadClass('util2.rfpTrack.Cluster').newInstance;
cl.cluster(rand(10,3))

%evmatlab.get


distlist=cl.distances.keySet.toArray;
for i=1:length(distlist)
    distlist2(i)=distlist(i);
end

