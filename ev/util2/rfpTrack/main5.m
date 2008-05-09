% d=imfilter(vox99(:,:,13),fspecial('disk',12));
% 
% pixels=reshape(d,prod(size(d)),1);
% %hist(pixels,100)
% sub=prctile(pixels,80);
% 
% d=d-sub;
% d(d<0)=0;
% 
% mesh(d)
% image(d)

%threshold: decided by sub which in turn is a percentile.
%3d: will 2d-based percentile affect 3d capability? will it exclude planes
%on this value alone? take percentile over windowed 3d planes at a time?


%%


initev


ost=evplugin.imagesetOST.OstImageset('/Volumes/TBU_main03/ost4dgood/TB2167_0804016');
channel=ost.channelImages.get('RFP');

frames=channel.imageLoader.keySet


%[1013, 1027, 1041, 1055, 1069, 1083, 1097, 1111, 1125, 1139, 1153, 1167, 1181, 1195, 1209, 1223, 1237, 1251, 1265, 1279, 1293, 1307, 1321, 1335, 1349, 1363, 1377, 1391, 1405, 1419, 1433, 1447, 1461, 1475, 1489, 1503, 1517, 1531, 1545, 1559, 1573, 1587, 1601, 1615, 1629, 1643, 1657, 1671, 1685, 1699, 1713, 1727, 1741, 1755, 1769, 1783, 1797, 1811, 1825, 1839, 1853, 1867, 1881, 1895, 1909, 1923, 1937, 1951, 1965, 1979, 1993, 2007, 2021, 2035, 2049, 2063, 2077, 2091, 2105, 2119, 2133, 2147, 2161, 2175, 2189, 2203, 2217, 2231, 2245, 2259, 2273, 2287, 2301, 2315, 2329, 2343, 2357, 2371, 2385, 2399, 2413, 2427, 2441, 2455, 2469, 2483, 2497, 2511, 2525, 2539, 2553, 2567, 2581, 2595, 2609, 2623, 2637, 2651, 2665, 2679, 2693, 2707, 2721, 2735, 2749, 2763, 2777, 2791, 2805, 2819, 2833, 2847, 2861, 2875, 2889, 2903, 2917, 2931, 2945, 2959, 2973, 2987, 3001, 3015, 3029, 3043, 3057, 3071, 3085, 3099, 3113, 3127, 3141, 3155, 3169, 3183, 3197, 3211, 3225, 3239, 3253, 3267, 3281, 3295, 3309, 3323, 3337, 3351, 3365, 3379, 3393, 3407, 3421, 3435, 3449, 3463, 3477, 3491, 3505, 3519, 3533, 3547, 3561, 3575, 3589, 3603, 3617, 3631, 3645, 3659, 3673, 3687, 3701, 3715, 3729, 3743, 3757, 3771, 3785, 3799, 3813, 3827, 3841, 3855, 3869, 3883, 3897, 3911, 3925, 3939, 3953, 3967, 3981, 3995, 4009, 4023, 4037, 4051, 4065, 4079, 4093, 4107, 4121, 4135, 4149, 4163, 4177, 4191, 4205, 4219, 4233, 4247, 4261, 4275, 4289, 4303, 4317, 4331, 4345, 4359, 4373, 4387, 4401, 4415, 4429, 4443, 4457, 4471, 4485, 4499, 4513, 4527, 4541, 4555, 4569, 4583, 4597, 4611, 4625, 4639, 4653, 4667, 4681, 4695, 4709, 4723, 4737, 4751, 4765, 4779, 4793, 4807, 4821, 4835, 4849, 4863, 4877, 4891, 4905, 4919, 4933, 4947, 4961, 4975, 4989, 5003, 5017, 5031, 5045, 5059, 5073, 5087, 5101, 5115, 5129, 5143, 5157, 5171, 5185, 5199, 5213, 5227, 5241, 5255, 5269, 5283, 5297, 5311, 5325, 5339, 5353, 5367, 5381, 5395, 5409, 5423, 5437, 5451, 5465, 5479, 5493, 5507, 5521, 5535, 5549, 5563, 5577, 5591, 5605, 5619, 5633, 5647, 5661, 5675, 5689, 5703, 5717, 5731, 5745, 5759, 5773, 5787, 5801, 5815, 5829, 5843, 5857, 5871, 5885, 5899, 5913, 5927, 5941, 5955, 5969, 5983, 5997, 6011, 6025, 6039, 6053, 6067, 6081, 6095, 6109, 6123, 6137, 6151, 6165, 6179, 6193, 6207, 6221, 6235, 6249, 6263, 6277, 6291, 6305, 6319, 6333, 6347, 6361, 6375, 6389]



%[voxin,zs]=getVoxStack(channel,1013); %too early
%diskrad=12; 

[voxin,zs]=getVoxStack(channel,1055);
diskrad=16; 
diskrad=25; 
%diskrad=40;

%[voxin,zs]=getVoxStack(channel,1405);
%diskrad=12; 


%[voxin,zs]=getVoxStack(channel,2413);
%diskrad=6; 

colormap('gray');


%%


hdisk=fspecial('disk',diskrad); %late


am=squeeze(mean(shiftdim(voxin,2)));
amin=squeeze(min(shiftdim(voxin,2)));
for z=1:length(zs)
    vox(:,:,z)=((voxin(:,:,z)-am)*2);
end
clear im
for z=1:length(zs)
    v=vox(:,:,z);
    z
    
    d=imfilter(vox(:,:,z),hdisk);
    pixels=reshape(d,prod(size(d)),1);
    sub=prctile(pixels,80);
    ptile(z)=sub;

%    d=d-sub;
 %   d(d<0)=0; %only needed for visualization
    
    im(:,:,z)=d;
    
end

%Windowed filter z-direction
%im2(:,:,1)=im(:,:,1);
%im2(:,:,length(zs)+2)=im(:,:,length(zs));
for z=1:length(zs)
    imA=im(:,:,max(z-1,1));
    imC=im(:,:,min(z+1,length(zs)));
    imB=im(:,:,z);
    imny(:,:,z)=(imA+imB+imC)/3;
end
im=imny;
clear imny;


%smooth ptile for better z-control. could fit a line!
theA=[(1:length(ptile))', (1:length(ptile))'.*0+1];
theX=theA\ptile';
ptile2=(1:length(zs))*theX(1)+theX(2);
%ptile2=ptile; %to disable smoothing
for z=1:length(zs)
    z
    
    d=im(:,:,z);

    sub=ptile2(z);
    
    d=d-sub;
    d(d<0)=0; %only needed for visualization
    
    im(:,:,z)=d;
    
end




%%

thresholdcolor=0;

%find local maximas
maximas=[];
[h,w,d]=size(im);
anyabovethres=0;
for z=2:(d-1)
    for y=2:(h-1)
        for x=2:(w-1)
            if im(y,x,z)>=thresholdcolor
                anyabovethres=1;
                if im(y,x,z)>im(y,x-1,z) && im(y,x,z)>im(y,x+1,z) && ...
                   im(y,x,z)>im(y-1,x,z) && im(y,x,z)>im(y+1,x,z) && ...
                   im(y,x,z)>im(y,x,z-1) && im(y,x,z)>im(y,x,z+1)
                    maximas=[maximas;x,y,z,im(y,x,z)];
                end
            end
        end
    end
end
disp('has maximas');
disp(size(maximas,1))


maximas=sortrows(maximas,4);
disp('sorting');



%%





%st=round(size(tmaximas,1)*0.9);
st=1;
numexp=1000; 
numexp=500; 
numexp=300; 
%numexp=30;    %must be candidates, not expected

st=max(1,size(maximas,1)-numexp)
tokeep=size(maximas,1)-st;
plot3(maximas(st:end,1),maximas(st:end,2),maximas(st:end,3),'o')


%Clustering: number of expected, better to remove candidates after
%clustering enough
maximas=maximas(st:end,:);



disp('annotating image');


fim=vox;
fims=im;
for i=1:size(maximas,1)
    ty=maximas(i,2);
    tx=maximas(i,1);
    tz=maximas(i,3);
    fim(ty,tx,tz)=256;
    fims(ty,tx,tz)=256;
end

%%

%for i=1:numcand
%    image(fims(:,:,i));    pause(1/5);    drawnow
%end


%%
% 
% 
% disp('make dist');
% 
% zrelscale=10;
% 
% %Distance matrix
% numcand=size(maximas,1);
% dist=zeros(numcand);
% for i=1:numcand
%     for j=1:numcand
%         va=maximas(i,1:3)-maximas(j,1:3);
%         va(3)=zrelscale*va(3);
%         dist(i,j)=sqrt(sum(va.^2));
%     end
% end
% listdist=reshape(dist,prod(size(dist)),1);
% plot(sort(listdist))

%%


loader=newclassloader;
cl=loader.loadClass('util2.rfpTrack.Cluster').newInstance;
zscale=10;
%maximas2=maximas(st:end,:);
maximas2=maximas;
maximas2(:,3)=maximas2(:,3)*zscale;
maximas2=cl.cluster(maximas2) %need to scale z

distlist=cl.distances.keySet.toArray;
for i=1:length(distlist)
    distlist2(i)=distlist(i);
end
maximas2(:,3)=maximas2(:,3)/zscale;

plot3(maximas2(:,1),maximas2(:,2),maximas2(:,3),'o')

fims2=im;
for i=1:size(maximas2,1)
    ty=round(maximas2(i,2));
    tx=round(maximas2(i,1));
    tz=round(maximas2(i,3));
    fims2(ty,tx,tz)=256;
end
