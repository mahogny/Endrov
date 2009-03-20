function [zerox, zeroy, collage]=imcombine(im,dx,dy,zerox,zeroy,collage)

%let collage=[] first time

[imh,imw]=size(im);
[ch,cw]=size(collage);

%Reference (dx,dy) to current image
dx=dx-zerox;
dy=dy-zeroy;

shiftx=0;
shifty=0;
collupdate=0;

%Move zero if needed
if dx<0
	collupdate=1;
	shiftx=-dx;  %Shift image later
	zerox=zerox-dx; %New zero
	cw=cw-dx;
	dx=0;
end
if dy<0
	collupdate=1;
	shifty=-dy;
	zeroy=zeroy-dy;
	ch=ch-dy;
	dy=0;
end



%Enlarge if needed
if imw+dx>cw
	collupdate=1;
	cw=imw+dx;
end
if imh+dy>ch
	collupdate=1;
	ch=imh+dy;
end

%Update collage size
if collupdate
	newcol=zeros(ch,cw,'uint8');
	spanh=1:size(collage,1);
	spanw=1:size(collage,2);
	newcol(spanh+shifty,spanw+shiftx)=collage(spanh,spanw);
	collage=newcol;
end

%Add new image
spanh=1:imh;
spanw=1:imw;
collage(spanh+dy,spanw+dx)=im;

