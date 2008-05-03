%
%BNDWRITE writes boundary coordinates to a file.
%   ST=BNDREAD(P,FILENAME) writes a list of boundary corner coordinates to the 
%   file FILENAME. P contains two columns, 
%   the first containing the x-coordinates and the 
%   second containing the y-coordinates. P is usually
%   obtained using GETBOUNDARYMEX and must consist of two columns
%   of coordinates that are integers plus
%   a half since P indicates pixel corner coordinates.
%   
%   Keith Forbes 2000 keith@umpire.com

function val=bndwrite(p,filename);

pKeep=p;
p=floor(p);
p(:,1)=shift(p(:,1),1); %to commpensate for shift
p(:,2)=shift(p(:,2),1); %to commpensate for shift

x1=double(shift(p(:,1),-1))-double(p(:,1));
y1=double(shift(p(:,2),-1))-double(p(:,2));

x2=shift(x1,-1);
y2=shift(y1,-1);

lrf=(((2*(x1==0)-1).*(x1+y1).*(x2+y2))>0).*~((x2==x1)&(y2==y1))+((x2==x1)&(y2==y1)).*2;

pad=(5-(mod(length(lrf),5)));
ppp=lrf;
lrf(1+length(lrf):length(lrf)+pad)=0; %pad with zeros to make a multiple of 5;
bb=lrf;

lrf=reshape(lrf,[5 length(lrf)/5]);
lrf=[1 3 9 27 81]*lrf;

ccc=lrf;
outfil=fopen(filename,'w');
%disp(filename)
%disp(outfil)
fwrite(outfil,p(1,:),'integer*2'); %write first coordinate
fwrite(outfil,p(2,:),'integer*2'); %write scond coordinate
fwrite(outfil,pad,'integer*1'); %record number of padded zeros
fwrite(outfil,lrf,'uint8'); %write left. right,forward

val=fclose(outfil);

q=bndread(filename); %perform check by reading file
if ~(prod(prod(pKeep==q)))
   disp('error with bndread/bndwrite check source code')
   pause
end

