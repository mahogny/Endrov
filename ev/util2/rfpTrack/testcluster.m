loader=newclassloader;
cl=loader.loadClass('util2.rfpTrack.Cluster').newInstance;

maximas=[...
    0,0,0;...
    10,0,0;...
    100,0,0;...
    110,0,0;...
    130,0,0;...
    131,0,0;...
    135,0,0;...
%    4,0,0;...
    %5,0,0;...
    %6,0,0;...
    %7,0,0;...
    %8,0,0;...
    ];

cl.cluster(maximas); %need to scale z