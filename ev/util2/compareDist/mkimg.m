img=[]
for n=1:10
    colormap('gray');
    img(n,1)=100;
end
imwrite(img,'testimg.gif','GIF')