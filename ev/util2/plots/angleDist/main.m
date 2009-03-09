%Data gotten with jurgens stuff. Have to redo retrieval if it should be done properly

dat=load('rot_E.txt');
x=cos(dat);
y=sin(dat);
plot([x';x'.*0.82],[y';y'.*0.82],'r')

hold on

dat=load('rot_EMS.txt');
x=cos(dat);
y=sin(dat);
plot([x'*0.8;x'.*0.62],[y'*0.8;y'.*0.62],'r')

dat=load('rot_venc.txt');
x=cos(dat);
y=sin(dat);
plot([x'*0.6;x'.*0.4],[y'*0.6;y'.*0.4],'r')



hold off
