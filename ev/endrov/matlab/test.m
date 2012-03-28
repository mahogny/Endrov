initev('/Users/tbudev3/javaproj/ev/');
evm=EvMatlab;

ost=evplugin.imagesetOST.OstImageset('/Volumes/TBU_main02/ost4dgood/TB2161_071120');
channel=ost.getChild('DIC')
frames=channel.imageLoader.keySet

[pl,zs]=getEvStack(ost.channelImages.get('DIC'),1010);







%matlab code:
%J = javaObject('class_name',x1,...,xn);
%so: getJImage need a friend that returns raw data. I take this would be the next-gen interface.
