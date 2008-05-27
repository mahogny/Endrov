
 c1=[1,7,8,3,5,87,3,6,678];
c2=[2,2,65,234];
mm=max([c1,c2]);
                list1=zeros(mm,1);
                list2=zeros(mm,1);
                list1(c1)=1;
                list2(c2)=1;
                ijneigh=any(list1 & list2)