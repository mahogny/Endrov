function imhist2(im)

pixels=reshape(im,prod(size(im)),1);
hist(pixels,100)

prctile(pixels,80)