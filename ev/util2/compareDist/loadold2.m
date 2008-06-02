w=waitbar(0,'loadold2 running');

load ('/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords/data/newneigh.mat')
names1=names;
ncount1=ncount;

load ('/Volumes/TBU_main02/ost4dgood/N2_071116/data/newneigh.mat')
names2=names;
ncount2=ncount;

load ('/Volumes/TBU_main03/ost4dgood/TB2167_0804016/data/newneigh.mat')
names3=names;
ncount3=ncount;

load ('/Volumes/TBU_main02/ost4dgood/N2_071114/data/newneigh.mat')
names4=names;
ncount4=ncount;

load ('/Volumes/TBU_main02/ost4dgood/N2greenLED080206/data/newneigh.mat')
names5=names;
ncount5=ncount;

load ('/Volumes/TBU_main02/ost4dgood/TB2142_071129/data/newneigh.mat')
names6=names;
ncount6=ncount;

load ('/Volumes/TBU_main02/ost4dgood/TB2164_080118/data/newneigh.mat')
names7=names;
ncount7=ncount;

load ('/Volumes/TBU_main02/ost4dgood/stdcelegansNew/data/newneigh.mat')
names8=names;
ncount8=ncount;

mf=fopen('main.htm','w');
fprintf(mf,'<html><head>\n');
fprintf(mf,'   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">\n');
fprintf(mf,'   <meta name="Author" content="J. Hench">\n');
fprintf(mf,'   <meta name="GENERATOR" content="Mozilla/4.5 [de] (Macintosh; I; PPC) [Netscape]">\n');
fprintf(mf,'   <title>Neighbours</title>\n');
fprintf(mf,'   <base target="frame1">\n');
fprintf(mf,'<script>\n');
fprintf(mf,'\n');
fprintf(mf,'if(window == window.top) \n');
fprintf(mf,'{\n');
fprintf(mf,'        var address=window.location;\n');
fprintf(mf,'        var s=\''<html><head><title>Neighbours</title></head>\''+\n');
fprintf(mf,'        \''<frameset cols="15%%,85%%" frameborder="4" onload="return true;" onunload="return true;">\''+\n');
fprintf(mf,'        \''<frame src="\''+address+\''?" name="indexframe">\''+\n');
fprintf(mf,'		\''<frame src="AB_neigh.htm" name="frame1">\''+\n');
fprintf(mf,'        \''</frameset>\''+\n');
fprintf(mf,'        \''</html>\'';\n');
fprintf(mf,'\n');
fprintf(mf,'        document.write(s);      \n');
fprintf(mf,'}\n');
fprintf(mf,'</script>\n');

le=length(names);

for i=1:le
    lname(i)=length(names(i));
end

[s,in]=sort(lname);

for i=1:le
    fprintf(mf,'<a href="%s_neigh.htm">%s</a><br>\n',names(in(i)),names(in(i)));
end

endname=6;

tncount1=[];
tncount2=[];
tncount3=[];
tncount23=[];


for i=1:le
    waitbar(i/le,w);
    tncount23(i)=0;
    fprintf(mf,'<a href="%s_neigh.htm">%s</a><br>\n',names(i),names(i));
    fn=strcat(names1(i),'_neigh.htm');
    f=fopen(fn,'w');
    fprintf(f,'<html><body><h1>%s</h1><table border="1">\n',names1(i));
    fprintf(f,'<tr><td colspan="2">AnglerUnixCoords</td><td colspan="2">N2_071116</td><td colspan="2">TB2167_0804016</td><td colspan="2">N2_071114</td><td colspan="2">N2greenLED080206</td><td colspan="2">TB2142_071129</td><td colspan="2">TB2164_080118/</td><td colspan="2">stdcelegansNew</td></tr>\n');
  
    for j=1:length(names1)
        if ncount1(i,j)>0 || ncount2(i,j)>0 || ncount3(i,j)>0 || ncount4(i,j)>0 || ncount5(i,j)>0 || ncount6(i,j)>0 || ncount7(i,j)>0 || ncount8(i,j)>0
            

            if ncount2(i,j)>0 && ncount3(i,j)>0 && length(names1(i))<=endname
                tncount23(i)=tncount23(i)+mean([ncount2(i,j),ncount3(i,j)]);
            end

            notannotated=0;
            if sum(ncount1(:,j)) == 0 %cell not annotated
                notannotated=notannotated+1;
            end
            if sum(ncount2(:,j)) == 0 %cell not annotated
                notannotated=notannotated+1;
            end
            if sum(ncount3(:,j)) == 0 %cell not annotated
                notannotated=notannotated+1;
            end
            if sum(ncount4(:,j)) == 0 %cell not annotated
                notannotated=notannotated+1;
            end
            if sum(ncount5(:,j)) == 0 %cell not annotated
                notannotated=notannotated+1;
            end
            if sum(ncount6(:,j)) == 0 %cell not annotated
                notannotated=notannotated+1;
            end
            if sum(ncount7(:,j)) == 0 %cell not annotated
                notannotated=notannotated+1;
            end
            if sum(ncount8(:,j)) == 0 %cell not annotated
                notannotated=notannotated+1;
            end

            sa=[sum([ncount1(i,j)>0,ncount2(i,j)>0,ncount3(i,j)>0,ncount4(i,j)>0,ncount5(i,j)>0,ncount6(i,j)>0,ncount7(i,j)>0,ncount8(i,j)>0])];
            hc=dec2hex(int16(255-sa/(8-notannotated)*255));
            if length(hc)==1
                hc=strcat('0',hc);
            end
            
            co=strcat('#ff',hc,hc);
            
            if ncount1(i,j)>0
                str1=names(j);
                co1=co;
            else
                str1='&nbsp;';
                co1='#ffffff';
            end
            if ncount2(i,j)>0
                str2=names(j);
                co2=co;
            else
                str2='&nbsp;';
                co2='#ffffff';
            end
            if ncount3(i,j)>0
                str3=names(j);
                co3=co;
            else
                str3='&nbsp;';
                co3='#ffffff';
            end
            if ncount4(i,j)>0
                str4=names(j);
                co4=co;
            else
                str4='&nbsp;';
                co4='#ffffff';
            end
            if ncount5(i,j)>0
                str5=names(j);
                co5=co;
            else
                str5='&nbsp;';
                co5='#ffffff';
            end
            if ncount6(i,j)>0
                str6=names(j);
                co6=co;
            else
                str6='&nbsp;';
                co6='#ffffff';
            end
            if ncount7(i,j)>0
                str7=names(j);
                co7=co;
            else
                str7='&nbsp;';
                co7='#ffffff';
            end
            if ncount8(i,j)>0
                str8=names(j);
                co8=co;
            else
                str8='&nbsp;';
                co8='#ffffff';
            end

            if sum(ncount1(:,j)) == 0 %cell not annotated
                str1='N/A';
                co1='#cccccc';
            end
            if sum(ncount2(:,j)) == 0 %cell not annotated
                str2='N/A';
                co2='#cccccc';
            end
            if sum(ncount3(:,j)) == 0 %cell not annotated
                str3='N/A';
                co3='#cccccc';
            end
            if sum(ncount4(:,j)) == 0 %cell not annotated
                str4='N/A';
                co4='#cccccc';
            end
            if sum(ncount5(:,j)) == 0 %cell not annotated
                str5='N/A';
                co5='#cccccc';
            end
            if sum(ncount6(:,j)) == 0 %cell not annotated
                str6='N/A';
                co6='#cccccc';
            end
            if sum(ncount7(:,j)) == 0 %cell not annotated
                str7='N/A';
                co7='#cccccc';
            end
            if sum(ncount8(:,j)) == 0 %cell not annotated
                str8='N/A';
                co8='#cccccc';
            end
            
            fprintf(f,'<tr><td bgcolor="%s">%s</td><td>%.0f%%</td><td bgcolor="%s">%s</td><td>%.0f%%</td><td bgcolor="%s">%s</td><td>%.0f%%</td><td bgcolor="%s">%s</td><td>%.0f%%</td><td bgcolor="%s">%s</td><td>%.0f%%</td><td bgcolor="%s">%s</td><td>%.0f%%</td><td bgcolor="%s">%s</td><td>%.0f%%</td><td bgcolor="%s">%s</td><td>%.0f%%</td></tr>\n',co1,str1,100*ncount1(i,j)/ncount1(i,i),co2,str2,100*ncount2(i,j)/ncount2(i,i),co3,str3,100*ncount3(i,j)/ncount3(i,i),co4,str4,100*ncount4(i,j)/ncount4(i,i),co5,str5,100*ncount5(i,j)/ncount5(i,i),co6,str6,100*ncount6(i,j)/ncount6(i,i),co7,str7,100*ncount7(i,j)/ncount7(i,i),co8,str8,100*ncount8(i,j)/ncount8(i,i));
        end
    end
    fprintf(f,'</table><br><br>updated on %s<br>&copy;&nbsp; <b>Hench J and Henriksson J.</b>, L&uuml;ppert M. and B&uuml;rglin T.</body></html>',datestr(now,0));
    fclose(f);
end
fprintf(mf,'</body></html>');
fclose(mf);

for i=1:length(names1)
    if length(names1(i))<=endname
        tncount1(i)=sum(ncount1(i,:));
        tncount2(i)=sum(ncount2(i,:));
        tncount3(i)=sum(ncount3(i,:));
    end
end

sum(tncount1)
sum(tncount2)
sum(tncount3)
sum(tncount23)

close(w)