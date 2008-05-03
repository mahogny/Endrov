%WATERSHEDMEX  watershed segmentation, flooding from selected sources.
%   IMOUT=WATERSHEDMEX(GIM,MARKERIM) forms the watershed transform of the
%   gradient image, GIM. MARKERIM consists of zeros to mark pixels
%   of unknown regions, and positive integers to mark pixels of
%   known region. These positive integers are used as the flood
%   source positions. You must have at least two different positive
%   integers in the marker image so that IMOUT will consist of at
%   least two different regions.
%
%   A good method of automatically determining marker images is to use
%   thresholding for a first approximation to the segmentation. Use
%   binary erosion and dilation to form the marker images from the
%   thresholded image.
%   
%   The algorithm is described in the book "Mathematical Morphology in
%   Image Processing" edited by E. Dougherty. The relevant chapter by
%   F. Meyer and S Beucher is titled "The Morphological approach of
%   segmentation: the watershed transformation".
%   
%   (c) Keith Forbes 2000 keith@umpire.com

function im=watershedmex(g,markerm); 
disp('Try running the mexKFtools script') 
error('You must compile the mex file watershedmex.c to use this function.')
