fp=fopen('/Volumes/TBU_main02/ost4dgood/N2_071116/data/neighname.txt','r');
name={};
k=1;
while ~feof(fp)
    s=fgets(fp);
    name{k}=s(1:(length(s)-1));
    k=k+1;
end
fclose(fp);



arr1=loadone('/Volumes/TBU_main03/ost4dgood/AnglerUnixCoords')./7.4579;
arr2=loadone('/Volumes/TBU_main02/ost4dgood/N2_071116');
arr3=loadone('/Volumes/TBU_main03/ost4dgood/TB2167_0804016');
arr4=loadone('/Volumes/TBU_main02/ost4dgood/stdcelegansNew');

%%



arr=arr1;
ns=[];
na={};
res1=[];

ne=10;

myfile = fopen('table.html','w');
fprintf(myfile,'<html><body><table border="1">\n');

for n=1:length(name)

    if length(name{n}) < 6

        fprintf(myfile,'<tr><td colspan="8"><b>%s</b></td></tr>',name{n});
        fprintf(myfile,'<tr><td colspan="2">AnglerUnixCoords</td><td colspan="2">N2_071116</td><td colspan="2">TB2167_0804016</td><td colspan="2">stdcelegansNew</td></tr>\n');

        [d,i]=sort(arr1(:,n));
        i1=i(d>0);
        d1=d(d>0);
        q1=min([ne,length(i1)]);
        if q1>0
            i1=i1(1:q1);
            d1(1:q1);

        end

        [d,i]=sort(arr2(:,n));
        i2=i(d>0);
        d2=d(d>0);
        q2=min([ne,length(i2)]);
        if q2>0
            i2=i2(1:q2);
            d2(1:q2);

        end


        [d,i]=sort(arr3(:,n));
        i3=i(d>0);
        d3=d(d>0);
        q3=min([ne,length(i3)]);
        if q3>0
            i3=i3(1:q3);
            d3(1:q3);
        end

        [d,i]=sort(arr4(:,n));
        i4=i(d>0);
        d4=d(d>0);
        q4=min([ne,length(i4)]);
        if q4>0
            i4=i4(1:q4);
            d4(1:q4);
        end
        
        fprintf(myfile,'<tr>');
        for e=1:ne
            if e>q1
                fprintf(myfile,'<td>&nbsp;</td><td>&nbsp;</td>');
            else
                fprintf(myfile,'<td>%s</td><td>%f</td>',name{i1(e)},d1(e));
            end
            if e>q2
                fprintf(myfile,'<td>&nbsp;</td><td>&nbsp;</td>');
            else
                fprintf(myfile,'<td>%s</td><td>%f</td>',name{i2(e)},d2(e));
            end
            if e>q3
                fprintf(myfile,'<td>&nbsp;</td><td>&nbsp;</td>');
            else
                fprintf(myfile,'<td>%s</td><td>%f</td>',name{i3(e)},d3(e));
            end
            if e>q4
                fprintf(myfile,'<td>&nbsp;</td><td>&nbsp;</td>');
            else
                fprintf(myfile,'<td>%s</td><td>%f</td>',name{i4(e)},d4(e));
            end
            fprintf(myfile,'</tr>');
        end


        %fprintf(myfile,'</tr>\n');
    end
end


fprintf(myfile,'</table></body></html>\n');
fclose (myfile);


