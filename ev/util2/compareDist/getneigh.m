function getneigh(arr)
arr=arr1;
ns=[];
na={};


for n=1:length(arr)
    [d,i]=sort(arr(:,n));
    i=i(d>0);
    d=d(d>0);
    q=min([8,length(i)]);
    i=i(1:q)
    d(1:q)
    name{i(1:q);}
end