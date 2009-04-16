%fit images for prasad. this is an anti-shake algorithm with the same base
%as for stitching

evminit

%ost=endrov.data.EvData.loadFile('/Volumes/TBU_main03/customer/prasad/090326 timelapse/20090326-DYF17timelapse3.tiff');
ost=endrov.data.EvData.loadFile('/Volumes/TBU_main04/customer/prasad/SP2101-2.tiff');
channel=ost.getChild('im0').channelImages.get('ch0');


%%

decZero=EvDecimal(0);




itF=channel.imageLoader.keySet.iterator;

%Get zero-reference
curFrame=itF.next
evim=channel.getImageLoader(curFrame,decZero);
refim=evim.getArrayImage; 
[h,w]=size(refim);
 
dists=[0,0];

while itF.hasNext
    disp(curFrame.toString)
    curFrame=itF.next;
    evim=channel.getImageLoader(curFrame,decZero);
    curim=evim.getArrayImage;
    
    [err,curdy,curdx]=fit2(refim,curim);
    
    %Interesting fit-problem: should choose the location with least
    %movement, or largest overlap
    
    if curdx>w/2
        curdx=curdx-w;
    end
    if curdx<-w/2
        curdx=curdx+w;
    end
    
    if curdy>h/2
        curdy=curdy-h;
    end
    if curdy<-h/2
        curdy=curdy+h;
    end
    
    
    [curdy,curdx]
    dists=[dists;curdy,curdx];
    
end


%%
plot(dists(:,1),dists(:,2))

%%

mind=min(dists);
maxd=max(dists);

%mov = avifile('/home/tbudev3/prasad.avi')

i=1;
itF=channel.imageLoader.keySet.iterator;
while itF.hasNext
    curFrame=itF.next;
    disp(curFrame.toString)
    evim=channel.getImageLoader(curFrame,decZero);
    curim=evim.getArrayImage;
    
    d=dists(i,:);
    collage=uint8(zeros(h+500,w+300));
   [zerox,zeroy,collage]=imcombine(curim,d(2),d(1),mind(2),mind(1),collage);
%       [zerox,zeroy,collage]=imcombine(curim,0,0,mind(2),mind(1),collage);
       
    %imshow(collage(1:2:end,1:2:end))
    colormap('gray');
    %image(collage(1:2:end,1:2:end))
    image(collage)
    axis image
    drawnow
    
    %mov=addframe(mov,getframe);
    
%    break
    
 %   M(i) = getframe;

    
    i=i+1;
end

%mov=close(mov);



