function v=testscale(im)

%should scale range as well

scales=linspace(1,10,100);
for i=1:length(scales)
    rim=imresize(im,1/scales(i));
    
    
    v(i)=mean(mean(abs(rim(:,2:end)-rim(:,1:(size(rim,2)-1)))));
    

    n=2;
    orim=reshape(rim,prod(size(rim)),1);
    rim2=[rim(:,(n+1):end),rim(:,1:n)];
    rim2=reshape(rim2,prod(size(rim2)),1);
    v(i)=corr(orim,rim2);
    
end

plot(scales,v)