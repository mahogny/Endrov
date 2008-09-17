format bank

pos1=[8.444908940568046, 1.1560686620571012, 0.6326506614840102]; %AB
pos2=[28.74435572139479, -0.11294065700760034, 1.5554415531048473]; %P1'
dist=norm(pos1-pos2)

%[um^3]
totalvol=4*pi*(dist/2)^3/3 + dist*pi*(dist/2)^2



%manual calc with lines
len=47
r=25/2;

totalvol=4*pi*r^3/3 + (len-2*r)*pi*(r)^2
%19000 um^3


dat=load('/Volumes/TBU_main02/ost4dgood/stdcelegansNew.ost/data/henriksson/volstats.txt');
curframe=dat(:,1);
numcell=dat(:,2);
totnucvol=dat(:,3);

%curframe=curframe-min(curframe);
%curframe=curframe.*(10/60); %timestep I think



%cut off at 150 cells
cutoff=find(numcell==150)
cutoff=cutoff(1);
curframe=curframe(1:cutoff);
numcell=numcell(1:cutoff);
totnucvol=totnucvol(1:cutoff);


volpernuc=totnucvol./numcell;
volpercell=totalvol./numcell;

semilogy(curframe,volpercell, 'r',curframe, volpercell,'b')


plot(curframe,volpernuc./volpercell)