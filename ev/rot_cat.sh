rm /Volumes/TBU_xeon01_500GB02/userdata/embrot/*.coord
./dorot.sh
sleep 2
cat  /Volumes/TBU_xeon01_500GB02/userdata/embrot/*.coord >/Volumes/TBU_xeon01_500GB02/userdata/embrot/allnewcoord.txt
