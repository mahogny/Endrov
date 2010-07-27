%Data gotten with jurgens stuff. Have to redo retrieval if it should be done properly

dat=load('rot_E.txt');
x=cos(dat);
y=sin(dat);
plot(x,y,'bo')

hold on

dat=load('rot_EMS.txt');
x=cos(dat)*0.9;
y=sin(dat)*0.9;
plot(x,y,'b+')

dat=load('rot_venc.txt');
x=cos(dat)*0.8;
y=sin(dat)*0.8;
plot(x,y,'b.')

hold off

legend('E','EMS','venc');

axis([-1.2 1.2 -1.2 1.2]);