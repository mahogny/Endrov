imtrue=load('/home/mahogny/imtrue.txt');
[pc, zscores, pcvars] = princomp(imtrue);

plot(pcvars(1:10))
imfalse=load('/home/mahogny/imfalse.txt');


[pc, zscores, pcvars] = princomp([imfalse,imtrue]);
scatter(zscores(:,1),zscores(:,2));

scatter(zscores(1:500,1),zscores(1:500,2),5,'r');
hold on
scatter(zscores(501:end,1),zscores(501:end,2),5,'b');
hold off


a=[-ones(size(imfalse,1),1), imfalse];
b=[+ones(size(imtrue,1),1), imtrue];

c=[a;b];
