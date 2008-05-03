function myautocorr(im)


s=size(im,1);
ha=fspecial('average',5);
%im=imfilter(im,ha);

n=50;

orim=reshape(im,prod(size(im)),1);

for i=0:n
    rim=im;
    
    rim=[rim(:,(i+1):end),rim(:,1:i)];
    
    
    rim=reshape(rim,prod(size(rim)),1);
    
    c(i+1)=corr(orim,rim);
    
end

plot(c,'r')