p=' /Volumes/TBU_main02/ost4dgood/N2_071116/data/';
s=strcat('cut -f1 ',p,'traveldist.txt >t_names.txt');
system (s);
%%
s=strcat('cut -f2 ',p,/Volumes/TBU_main02/ost4dgood/N2_071116/data/traveldist.txt >t_start.txt');
s=strcat('cut -f3 ',p,/Volumes/TBU_main02/ost4dgood/N2_071116/data/traveldist.txt >t_end.txt');
s=strcat('cut -f4 ',p,/Volumes/TBU_main02/ost4dgood/N2_071116/data/traveldist.txt >t_air.txt');
s=strcat('cut -f5 ',p,/Volumes/TBU_main02/ost4dgood/N2_071116/data/traveldist.txt >t_ground.txt');
%%