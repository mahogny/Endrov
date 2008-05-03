%SELECTOBJECTMEX selects objects from a binary image.
%   BWOUT=SELECTOBJECTMEX(BWIN,N) selects the Nth largest object
%   (ones) from the binary image BWIN (ones and zeros).
%   Four-connectivity of objects is assumed.
%
%   (c) Keith Forbes 2000 keith@umpire.com

function bw=selectobjectmex(bwin,n);
disp('Try running the mexKFtools script')
error('You must compile the mex file selectobjectmex.c to use this function.')

