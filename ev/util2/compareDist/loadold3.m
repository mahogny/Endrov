%% input arguments
neighfiles = {'/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords/data/newneigh.mat',...
              '/Volumes/TBU_main02/ost4dgood/N2_071116/data/newneigh.mat',...
              '/Volumes/TBU_main03/ost4dgood/TB2167_0804016/data/newneigh.mat',...
%               '/Volumes/TBU_main02/ost4dgood/N2_071114/data/newneigh.mat',...
%               '/Volumes/TBU_main02/ost4dgood/N2greenLED080206/data/newneigh.mat',...
%               '/Volumes/TBU_main02/ost4dgood/TB2142_071129/data/newneigh.mat',...
%               '/Volumes/TBU_main02/ost4dgood/TB2164_080118/data/newneigh.mat',...
%               '/Volumes/TBU_main02/ost4dgood/stdcelegansNew/data/newneigh.mat'...
    };

recnames = {'AnglerUnixCoords','N2_071116','TB2167_0804016','N2_071114','N2greenLED080206','TB2142_071129','TB2164_080118','stdcelegansNew'};

targetdir='htmlexport/';

cheight=13; %px
clength=50; %px

%%
%create bitmaps for bars
for brit=0:255
    for n=1:cheight
        colormap('gray');
        img(n,1)=brit;
    end

    imgn=sprintf('%s%i_bar.gif',targetdir,brit);
    imwrite(img,imgn,'GIF');
end

%%
w=waitbar(0,'loadold3 running');
% load the mat files
namesx={};
ncountx={};
nstatusx={};
dtx=[];
nstartx={};
nendx={};
r = length(neighfiles);
for rec=1:r;
    waitbar(rec/r,w,'loading data');
    load(neighfiles{rec});
    ncountx{rec}=ncount;
    nstatusx{rec}=nstatus;
    dtx(rec)=dt;
    nstartx{rec}=nstart;
    nendx{rec}=nend;
end
%%
%clear unused variables (from MAT file loading)
clear ncount; 
%there will only be one version of "names" based on the reference lineage
%as specified in findneigh.m
%%
%write mainpage for frames in HTML
mf=fopen(strcat(targetdir,'main_single.htm'),'w');
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

mf2=fopen(strcat(targetdir,'main_tree.htm'),'w');
fprintf(mf2,'<html><head>\n');
fprintf(mf2,'   <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">\n');
fprintf(mf2,'   <meta name="Author" content="J. Hench">\n');
fprintf(mf2,'   <meta name="GENERATOR" content="Mozilla/4.5 [de] (Macintosh; I; PPC) [Netscape]">\n');
fprintf(mf2,'   <title>Neighbours</title>\n');
fprintf(mf2,'   <base target="frame1">\n');
fprintf(mf2,'<script>\n');
fprintf(mf2,'\n');
fprintf(mf2,'if(window == window.top) \n');
fprintf(mf2,'{\n');
fprintf(mf2,'        var address=window.location;\n');
fprintf(mf2,'        var s=\''<html><head><title>Neighbours</title></head>\''+\n');
fprintf(mf2,'        \''<frameset cols="15%%,85%%" frameborder="4" onload="return true;" onunload="return true;">\''+\n');
fprintf(mf2,'        \''<frame src="\''+address+\''?" name="indexframe">\''+\n');
fprintf(mf2,'		\''<frame src="AB_neightime.htm" name="frame1">\''+\n');
fprintf(mf2,'        \''</frameset>\''+\n');
fprintf(mf2,'        \''</html>\'';\n');
fprintf(mf2,'\n');
fprintf(mf2,'        document.write(s);      \n');
fprintf(mf2,'}\n');
fprintf(mf2,'</script>\n');

le=length(names);

for i=1:le
    lname(i)=length(names(i));
end

[s,in]=sort(lname);

for i=1:le
    fprintf(mf,'<a href="%s_neigh.htm">%s</a><br>\n',names(in(i)),names(in(i)));
    fprintf(mf2,'<a href="%s_neightime.htm">%s</a><br>\n',names(in(i)),names(in(i)));
end

tanno=[];
for i=1:le
    waitbar(i/le,w,'generating tables');
    fprintf(mf,'<a href="%s_neigh.htm">%s</a><br>\n',names(i),names(i));
    fprintf(mf2,'<a href="%s_neightime.htm">%s</a><br>\n',names(i),names(i));
    f=fopen(strcat(targetdir,names(i),'_neigh.htm'),'w');
    f2=fopen(strcat(targetdir,names(i),'_neightime.htm'),'w');
    fprintf(f,'<html><body><h1>%s</h1>\n',names(i));
    fprintf(f,'<table border="1" cellpadding="0" cellspacing="0">\n');
    fprintf(f,'<tr><td colspan="2">&nbsp;</td><td colspan="%i" align="center"><tt>Data Sets</tt></td></tr>\n',r);
    fprintf(f,'<tr><td><tt>Cell Name</tt></td><td><tt>Score</tt></td>');
    fprintf(f2,'<html><body><h1>%s</h1>\n',names(i));
    fprintf(f2,'<table border="1" cellpadding="0" cellspacing="0">\n');
    fprintf(f2,'<tr><td colspan="2">&nbsp;</td><td colspan="%i" align="center"><tt>Data Sets</tt></td></tr>\n',r);
    fprintf(f2,'<tr><td><tt>Cell Name</tt></td><td><tt>Score</tt></td>');

    for q=1:r
        fprintf(f,'<td><tt>%i</tt></td>\n',q);
        fprintf(f2,'<td width="%i"><tt>%i</tt></td>\n',clength,q);
        tanno(q) = sum(ncountx{q}(i,:)); %% determine target annotation status
    end


    fprintf(f,'</tr>\n');
    fprintf(f2,'</tr>\n');
    for j=1:length(names)
        boo=0;
        for q=1:r
            boo=boo+any(ncountx{q}(i,j));
        end
        if boo>0
            notannotated=0;
            for q=1:r
                if sum(ncountx{q}(:,j)) == 0 || tanno(q) == 0  % cell or target not annotated
                    notannotated=notannotated+1;
                end
            end
            sa=0;
            for q=1:r
                sa=sa+sum(ncountx{q}(i,j)>0);
            end
            anno=r-notannotated;
            hits=sa/anno;
            hc=dec2hex(int16(255-hits*255));
            if length(hc)==1
                hc=strcat('0',hc);
            end
            if i==j
                htco='#33ccff';
            else
                htco=strcat('#ff',hc,hc);
            end

            str={};
            co={};
            for q=1:r
                if ncountx{q}(i,j)>0
                    %str{q}=names(j);
                    str{q}='an';
                    co{q}=htco;
                else
                    str{q}='&nbsp;';
                    co{q}='#ffffff';
                end
                if sum(ncountx{q}(:,j)) == 0 %%  %cell not annotated
                    str{q}='n.a.';
                    co{q}='#cccccc';
                end
                if tanno(q) == 0 %% target not annotated
                    str{q}='<font color="#ff0000">n.t.</font>';
                    co{q}='#000000';
                end
            end

            fprintf(f,'<tr><td bgcolor="%s"><tt><a href="%s_neigh.htm">%s</a></tt></td><td bgcolor="%s"><tt>%i/%i<tt></td>\n',htco,names(j),names(j),htco,sa,anno);
            fprintf(f2,'<tr><td bgcolor="%s"><tt><a href="%s_neightime.htm">%s</a></tt></td><td bgcolor="%s"><tt>%i/%i<tt></td>\n',htco,names(j),names(j),htco,sa,anno);

            for q=1:r
                col=[co{q}];
                perc=100*ncountx{q}(i,j)/ncountx{q}(i,i);
                imgcode='';
                if perc>=1
                    stri=sprintf('%.0f%%',perc);
                    sl=(nendx{q}(i)-nstartx{q}(i))/dtx(q);
                    for pix=1:clength
                        if find(nstatusx{q}{i,j},nstart==round(pix/clength*sl))
                            imgcode=sprintf('%s<img src="255_bar.gif">',imgcode);
                        else
                            imgcode=sprintf('%s<img src="0_bar.gif">',imgcode);    
                    
                        end
                    end
                    %%build image code for display of bars
                    %sl = length(nstatusx{q}{i,j});
%                     sl= nstartx{q}(i)
%                     ste=clength/sl;
%                     for bl=1:clength
%                         ind=round(1+bl/ste);
%                         if ind>0 && ind<=sl
%                             if nstatusx{q}{i,j}(ind)=='n';
%                                 imgcode=sprintf('%s<img src="0_bar.gif">',imgcode);
%                             else
%                                 imgcode=sprintf('%s<img src="255_bar.gif">',imgcode);
%                             end
%                         end

                        %%end building image string
                    
                elseif findstr(str{q},'n.')
                    stri=[str{q}];
                else
                    stri='&nbsp;';
                end
                fprintf(f,'<td bgcolor="%s"><tt>%s</tt></td>',col,stri);
                fprintf(f2,'<td bgcolor="%s"><tt>%s<br>%s<br>%s</tt></td>',col,imgcode,stri,nstatusx{q}{i,j});
            end
            fprintf(f,'</tr>\n');
            fprintf(f2,'</tr>\n');
        end
    end
    fprintf(f,'</table>\n');
    fprintf(f,'<br><u>Data Sets:</u><br>\n');
    fprintf(f2,'</table>\n');
    fprintf(f2,'<br><u>Data Sets:</u><br>\n');
    for q=1:r
        fprintf(f,'%i: %s<br>',q,recnames{q});
        fprintf(f2,'%i: %s<br>',q,recnames{q});
    end

    fprintf(f,'<br><u>Legend:</u><br>');
    fprintf(f,'<table border="1" cellpadding="0" cellspacing="0" height="83" width="468"><tbody><tr><td><tt>target cell</tt><br></td><td bgcolor="#33ccff"><br>');
    fprintf(f,'</td></tr><tr><td><tt>neighbour cell<br></tt></td><td bgcolor="#ff0000"><tt><font color="#ff0000"><font color="#000000">neigbour for %% of life time</font></font></tt></td>');
    fprintf(f,'</tr><tr><td valign="top"><tt>cell not neighbour<br></tt></td><td valign="top"><br></td></tr><tr><td bgcolor="#ffffff"><tt>neighbour not annotated</tt><br></td>');
    fprintf(f,'<td bgcolor="#cccccc"><font color="#000000"><tt>n.a.</tt></font></td></tr><tr><td bgcolor="#ffffff"><tt>target not annotated<br></tt></td><td bgcolor="#000000"><tt><font color="#ff0000">n.t.</font></tt></td></tr></tbody></table>');

    fprintf(f,'<br><br>updated on %s<br>&copy;&nbsp; <b>Hench J and Henriksson J.</b>, L&uuml;ppert M. and B&uuml;rglin T.</body></html>',datestr(now,0));
    fclose(f);

    fprintf(f2,'<br><u>Legend:</u><br>');
    fprintf(f2,'<table border="1" cellpadding="0" cellspacing="0" height="83" width="468"><tbody><tr><td><tt>target cell</tt><br></td><td bgcolor="#33ccff"><br>');
    fprintf(f2,'</td></tr><tr><td><tt>neighbour cell<br></tt></td><td bgcolor="#ff0000"><tt><font color="#ff0000"><font color="#000000">neigbour for %% of life time</font></font></tt></td>');
    fprintf(f2,'</tr><tr><td valign="top"><tt>cell not neighbour<br></tt></td><td valign="top"><br></td></tr><tr><td bgcolor="#ffffff"><tt>neighbour not annotated</tt><br></td>');
    fprintf(f2,'<td bgcolor="#cccccc"><font color="#000000"><tt>n.a.</tt></font></td></tr><tr><td bgcolor="#ffffff"><tt>target not annotated<br></tt></td><td bgcolor="#000000"><tt><font color="#ff0000">n.t.</font></tt></td></tr></tbody></table>');

    fprintf(f2,'<br><br>updated on %s<br>&copy;&nbsp; <b>Hench J and Henriksson J.</b>, L&uuml;ppert M. and B&uuml;rglin T.</body></html>',datestr(now,0));
    fclose(f2);


end
fprintf(mf,'</body></html>');
fprintf(mf2,'</body></html>');
fclose(mf);
fclose(mf2);
close(w);