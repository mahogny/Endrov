function out=q(arr1,arr2)

av=(arr1+arr2)/2;
av(av==0)=1;
out=(arr1-arr2)./av;
out=abs(out)*20;
