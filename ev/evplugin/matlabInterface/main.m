%[M,X,J] = inmem

%classpath.txt   path to basedir goes here

%matlab code:

frame = java.awt.Frame('Frame A');
%J = javaObject('class_name',x1,...,xn);

%setSize(newFrameRef, 1000, 800);

frame.setTitle('Sample Frame')

title = frame.getTitle
title = 
Sample Frame



so: getJImage need a friend that returns raw data. I take this would be the next-gen interface.

