
fmap=zeros(size(ed));
fmap(sy,sx)=1;

added=1;
while added
    added=0;
    
    for y=2:(size(ed,1)-1)
        for x=2:(size(ed,2)-1)
            if fmap(y,x)
                if fmap(y-1,x)==0 && ed(y-1,x)>ed(y,x)
                    fmap(y-1,x)=1;
                    added=1;
                end
                if fmap(y+1,x)==0 && ed(y+1,x)>ed(y,x)
                    fmap(y+1,x)=1;
                    added=1;
                end
                if fmap(y,x-1)==0 && ed(y,x-1)>ed(y,x)
                    fmap(y,x-1)=1;
                    added=1;
                end
                if fmap(y,x+1)==0 && ed(y,x+1)>ed(y,x)
                    fmap(y,x+1)=1;
                    added=1;
                end
            end
        end
    end
end


% added=1;
% while added
%     added=0;
%     
%     for y=2:(size(ed,1)-1)
%         for x=2:(size(ed,2)-1)
%             if fmap(y,x)
%                 if fmap(y-1,x)==0 && ed(y-1,x)>ed(y,x) && ed(y-1,x)<=0
%                     fmap(y-1,x)=1;
%                     added=1;
%                 end
%                 if fmap(y+1,x)==0 && ed(y+1,x)>ed(y,x) && ed(y+1,x)<=0
%                     fmap(y+1,x)=1;
%                     added=1;
%                 end
%                 if fmap(y,x-1)==0 && ed(y,x-1)>ed(y,x) && ed(y,x-1)<=0
%                     fmap(y,x-1)=1;
%                     added=1;
%                 end
%                 if fmap(y,x+1)==0 && ed(y,x+1)>ed(y,x) && ed(y,x+2)<=0
%                     fmap(y,x+1)=1;
%                     added=1;
%                 end
%             end
%         end
%     end
% end



SE = strel('disk',2,4);
%fmap2=imerode(fmap,SE);
fmap2=fmap;
%im5=im3;
im5=ed;
im5(fmap2==1)=255;

image(im5*50)
axis equal

%SE = strel('disk',2,4);
%fmap2=imerode(fmap,SE);
%im5=im3;
%im5(fmap2==1)=255;

%image(im5)

