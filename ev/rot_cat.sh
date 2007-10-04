rm /Volumes/TBU_xeon01_500GB01/embrot/*.coord
./dorot.sh
sleep 2
cat  /Volumes/TBU_xeon01_500GB01/embrot/*.coord >/Volumes/TBU_xeon01_500GB01/embrot/allnewcoord.txt
