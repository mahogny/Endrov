function muldisk(im)

numrad=5;

for i=1:numrad
    hdisk=fspecial('disk',i);
    subplot(1,numrad,i);
    image(imfilter(im,hdisk))
    axis equal
    
end
    