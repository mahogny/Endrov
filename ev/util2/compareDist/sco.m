fp=fopen('/Volumes/TBU_main02/ost4dgood/N2_071116/data/neighname.txt','r');
name={};
k=1;
while ~feof(fp)
    s=fgets(fp);
    name{k}=s(1:(length(s)-1));
    k=k+1;
end
fclose(fp);


arr1=loadone('/Volumes/TBU_main02/ost4dgood/N2_071116');
arr2=loadone('/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords');
arr3=loadone('/Volumes/TBU_main03/ost4dgood/TB2167_0804016');

%%
arr=arr1;
ns=[];
na={};

for n=1:length(arr)
    
    arrline=[[arr1((1:length(arr)))]',[1:length(arr)]'];
    neigh=sortrows(arrline,1);
    m=length(neigh);
    
    g=0;
    while m>0 && g<8 % 8 neighbors
        if neigh(m,1) > 0
            ns(g,n)= neigh(m,1);
            na(g,n)= name(int64(neigh(m,2)));
            g = g + 1;
        end
        m = m - 1;
    end
end