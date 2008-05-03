%PALETTEMEX create 3-band RGB image from marker image
%   IM=PALETTEMEX(M) takes a single band marker image and creates a
%   colourful RGB image with a different colour for each number.
%
%   Keith Forbes 2000 keith@umpire.com

function z=palettemex(bw);
disp('Try running the mexKFtools script')
error('You must compile the mex file palettemex.c to use this function.')
