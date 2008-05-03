%
%GETBOUNADRYMEX returns boundary coordinates for an image containing a binary object (4-connected foreground region).
%   P=GETBOUNADRYMEX(IM) returns a list of pixel corner coordinates of
%   the object in binary image (zeros and ones) IM. P contains 
%   two columns, the first containing the x-coordinates and the 
%   second containing the y-coordinates. Use PLOT(P(:,2),P(:,1)) to
%   see the boundary. The file getboundarymex.c must be compiled to create
%   a mex file.
%   If there is more than one object in the image, then the first one that
%   is come across will be traversed.
%
%   Keith Forbes 2000 keith@umpire.com

function val=getboundarymex(im);
disp('Try running the mexKFtools script')
error('You must compile the mex file getboundarymex.c to use this function.')

