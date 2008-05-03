%input image: ed


%Find seed points

list=[];
i=1;
for y=1:size(ed,1)
    for x=1:size(ed,2)
        list(i,:)=[x,y,ed(y,x)];
        i=i+1;
    end
end

list=sortrows(list,3);


fmaptot=zeros(size(ed));
regioni=1;
seeds=[];
for curlist=1:size(list,1)
    
    
    sx=list(curlist,1);
    sy=list(curlist,2);

    if fmaptot(sy,sx)==0
        seeds=[seeds;sx,sy];

        subplot(3,4,regioni)
        ffillone
        
        fmaptot=fmaptot+fmap;
        regioni=regioni+1;
    end
    
    if regioni>10
        break;
    end
    
end




%%

ffillone

image(fmap*10)

%%
im5=im3;
im5(fmap==1)=255;

image(im5)


%%
image(ed*50)
%%
image(fmap*10)
%%
SE = strel('disk',2,4);
fmap2=imerode(fmap,SE);
im5=im3;
im5(fmap2==1)=255;

image(im5)

