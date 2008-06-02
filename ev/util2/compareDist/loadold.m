load ('/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords/data/newneigh.mat')
names1=names;
ncount1=ncount;

load ('/Volumes/TBU_main02/ost4dgood/N2_071116/data/newneigh.mat')
names2=names;
ncount2=ncount;

load ('/Volumes/TBU_main03/ost4dgood/TB2167_0804016/data/newneigh.mat')
names3=names;
ncount3=ncount;

mf=fopen('main.htm','w');
fprintf(mf,'<html><head>\n');
fprintf(mf,'   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">\n');
fprintf(mf,'   <meta name="Author" content="J. Hench">\n');
fprintf(mf,'   <meta name="GENERATOR" content="Mozilla/4.5 [de] (Macintosh; I; PPC) [Netscape]">\n');
fprintf(mf,'   <title>Neighbours</title>\n');
fprintf(mf,'   <base target="bildframe">\n');
fprintf(mf,'<script>\n');
fprintf(mf,'\n');
fprintf(mf,'if(window == window.top) \n');
fprintf(mf,'{\n');
fprintf(mf,'        var address=window.location;\n');
fprintf(mf,'        var s=\''<html><head><title>Neighbours</title></head>\''+\n');
fprintf(mf,'        \''<frameset cols="50%%,50%%" frameborder="4" onload="return true;" onunload="return true;">\''+\n');
fprintf(mf,'        \''<frame src="\''+address+\''?" name="indexframe">\''+\n');
fprintf(mf,'		\''<frame src="AB_neigh.htm" name="bildframe">\''+\n');
fprintf(mf,'        \''</frameset>\''+\n');
fprintf(mf,'        \''</html>\'';\n');
fprintf(mf,'\n');
fprintf(mf,'        document.write(s);      \n');
fprintf(mf,'}\n');
fprintf(mf,'</script>\n');



endname=6;

tncount1=[];
tncount2=[];
tncount3=[];
tncount23=[];

for i=1:length(names1);
    tncount23(i)=0;
    fprintf(mf,'<a href="%s_neigh.htm">%s</a><br>\n',names(i),names(i));
    fn=strcat(names1(i),'_neigh.htm');
    f=fopen(fn,'w');
    fprintf(f,'<html><body><h1>%s</h1><table border="1">\n',names1(i));
    fprintf(f,'<tr><td colspan="2">AnglerUnixCoords</td><td colspan="2">N2_071116</td><td colspan="2">TB2167_0804016</td></tr>\n');
    for j=1:length(names1)
        if ncount1(i,j)>0 || ncount2(i,j)>0 || ncount3(i,j)>0
            if ncount1(i,j)>0
                str1=names(j);
            else
                str1='&nbsp;';
            end
            if ncount2(i,j)>0
                str2=names(j);
            else
                str2='&nbsp;';
            end
            if ncount3(i,j)>0
                str3=names(j);
            else
                str3='&nbsp;';
            end

            if ncount2(i,j)>0 && ncount3(i,j)>0 && length(names1(i))<=endname
                tncount23(i)=tncount23(i)+mean([ncount2(i,j),ncount3(i,j)]);
            end
            if ncount1(i,j)>0 && ncount2(i,j)>0 && ncount3(i,j)>0
                co1='#ff6666';
                co2='#ff6666';
                co3='#ff6666';
            elseif  ncount1(i,j)==0 && ncount2(i,j)>0 && ncount3(i,j)>0
                co1='#ffffff';
                co2='#ffcccc';
                co3='#ffcccc';
            elseif  ncount1(i,j)>0 && ncount2(i,j)==0 && ncount3(i,j)>0
                co1='#ffcccc';
                co2='#ffffff';
                co3='#ffcccc';
            elseif  ncount1(i,j)>0 && ncount2(i,j)>0 && ncount3(i,j)==0
                co1='#ffcccc';
                co2='#ffcccc';
                co3='#ffffff';
            else
                co1='#ffffff';
                co2='#ffffff';
                co3='#ffffff';
            end

            fprintf(f,'<tr><td bgcolor="%s">%s</td><td>%i</td><td bgcolor="%s">%s</td><td>%i</td><td bgcolor="%s">%s</td><td>%i</td></tr>\n',co1,str1,ncount1(i,j),co2,str2,ncount2(i,j),co3,str3,ncount3(i,j));
        end
    end
    fprintf(f,'</table></body></html>');
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