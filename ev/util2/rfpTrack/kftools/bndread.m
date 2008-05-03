%
%BNDREAD read boundary coordinates from a file.
%   P=BNDREAD(FILENAME) loads a list of boundary corner coordinates from the 
%   file FILENAME (that is created using BNDWRITE). P contains 
%   two columns, the first containing the x-coordinates and the 
%   second containing the y-coordinates.
%
%   Keith Forbes 2000 keith@umpire.com

function val=bndread(filename);

infil=fopen(filename,'r');

p1=fread(infil,2,'integer*2'); % first coordinate

p2=fread(infil,2,'integer*2'); % scond coordinate

padding=fread(infil,1,'integer*1'); % number of padded zeros


lrf=fread(infil,'uint8'); % left. right,forward



fclose(infil);



sss=lrf;

qqq(:,1)=floor(lrf/81);
lrf=lrf-qqq(:,1)*81;

qqq(:,2)=floor(lrf/27);
lrf=lrf-qqq(:,2)*27;

qqq(:,3)=floor(lrf/9);
lrf=lrf-qqq(:,3)*9;

qqq(:,4)=floor(lrf/3);
lrf=lrf-qqq(:,4)*3;

qqq(:,5)=lrf;


bbb=reshape(fliplr(qqq)',[1 length(flipud(qqq))*5]);

bbb=bbb(1:length(bbb)-padding-1); %remove trailing zeros + last direction is redundant

nnn=(bbb==0).*-1+(bbb==1);
mmm=mod(cumsum(nnn),4);


drctn=p1-p2;


if (drctn(1)==1)
   y=(mmm==3).*-1+(mmm==1);
   x=(mmm==0).*-1+(mmm==2);
end

if (drctn(1)==-1)
   y=(mmm==1).*-1+(mmm==3);
   x=(mmm==2).*-1+(mmm==0);
end

if (drctn(2)==1)
   x=(mmm==1).*-1+(mmm==3);
   y=(mmm==0).*-1+(mmm==2);
end

if (drctn(2)==-1)
   y=(mmm==2).*-1+(mmm==0);
   x=(mmm==3).*-1+(mmm==1);
end


po(:,1)=p1(1)+[0 ;cumsum(x)']-drctn(1)+0.5;
po(:,2)=p1(2)+[0 ;cumsum(y)']-drctn(2)+0.5;


val=po;
