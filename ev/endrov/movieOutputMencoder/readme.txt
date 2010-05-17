Thanks a lot... at last a lossless x264 command that do work.

Here is the command I used

ffmpeg -y -r 24 -i png/big_buck_bunny_%05d.png \
 -pix_fmt yuv420p -vcodec ffv1 -coder ac str/BBB360-ffv1-cabac.mov   \
 -pix_fmt yuv420p -vcodec ffv1 str/BBB360-ffv1-vlc.mov    \
 -pix_fmt yuv420p -vcodec libx264 -cqp 0 -me_method dia -subq 1 -partitions -parti4x4-parti8x8-partp4x4-partp8x8-partb8x8 -f mp4 str/BBB360-dark1.mp4 \
 -pix_fmt yuv420p -vcodec libx264 -cqp 0 -subq 5 -coder ac -partitions +parti4x4+parti8x8+partp4x4+partp8x8+partb8x8 -f mp4 str/BBB360-dark2.mp4 \
 -pix_fmt yuv420p -vcodec libx264 -cqp 0 -subq 6 -coder ac -partitions +parti4x4+parti8x8+partp4x4+partp8x8+partb8x8 -refs 2 -flags2 +dct8x8 -f mp4 str/BBB360-dark3.mp4 \
 -pix_fmt yuv420p -vcodec libx264 -cqp 0 -me_method umh -subq 8 -coder ac -partitions  +parti4x4+parti8x8+partp4x4+partp8x8+partb8x8 -refs 4 -flags2 +dct8x8+mixed_refs -f mp4 str/BBB360-dark4.mp4 \
 -pix_fmt yuv420p -vcodec libx264 -cqp 0 -me_method esa -subq 8 -coder ac -partitions  +parti4x4+parti8x8+partp4x4+partp8x8+partb8x8 -refs 16 -flags2 +dct8x8+mixed_refs -f mp4 str/BBB360-dark5.mp4 




Resulted file sizes and encoding speeds in frames per second:

BBB360-ffv1-cabac.mov 1.5G @ 28 fps
BBB360-ffv1-vlc.mov   1.5G @ 28 fps
BBB360-dark1.mp4      914M @ 34 fps
BBB360-dark2.mp4      891M @ 20 fps
BBB360-dark3.mp4      876M @ 18 fps
BBB360-dark4.mp4      866M @ 14 fps
BBB360-dark5.mp4      861M @ 12 fps 


===>

ffmpeg -y -r 24 -i png/big_buck_bunny_%05d.png \
 -pix_fmt yuv420p -vcodec libx264 -cqp 0 -me_method esa -subq 8 -coder ac -partitions  +parti4x4+parti8x8+partp4x4+partp8x8+partb8x8 -refs 16 -flags2 +dct8x8+mixed_refs -f mp4 str/BBB360-dark5.mp4 

 ===>

-y overwrite output

ffmpeg -y -i png/big_buck_bunny_%05d.png \
 -pix_fmt yuv420p -vcodec libx264 -cqp 0 -me_method esa -subq 8 -coder ac -partitions  +parti4x4+parti8x8+partp4x4+partp8x8+partb8x8 -refs 16 -flags2 +dct8x8+mixed_refs -f mp4 str/BBB360-dark5.mp4 
 