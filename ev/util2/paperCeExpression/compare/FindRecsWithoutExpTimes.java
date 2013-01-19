package util2.paperCeExpression.compare;

import java.io.File;
import java.util.Set;

import endrov.core.EndrovCore;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.imageset.EvChannel;
import endrov.imageset.Imageset;
import endrov.util.EvDecimal;

import util2.paperCeExpression.collectData.PaperCeExpressionUtil;

public class FindRecsWithoutExpTimes
	{

	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
		Set<File> datas=PaperCeExpressionUtil.getAnnotated();
		System.out.println(datas);
		
		for(File f:datas)
			{
			EvData data=EvData.loadFile(f);
			Imageset imset=data.getObjects(Imageset.class).iterator().next();
			
			String channelName="GFP";
			EvChannel ch=imset.getChannel(channelName);
			
			if(ch==null)
				continue;
			
			boolean hasExpTime=true;
			boolean missingFrames=false;
			boolean butHasImsetMeta=true;
			for(EvDecimal frame:ch.getFrames())
				if(ch.metaFrame.get(frame)==null)
					{
					missingFrames=true;
					if(imset.metaFrame.get(frame)!=null && imset.metaFrame.get(frame).get("exposureTime")!=null)
						butHasImsetMeta=true;
					}
				else if(ch.metaFrame.get(frame).get("exposuretime")==null)
					{
					hasExpTime=false;
					break;
					}

			if(butHasImsetMeta)
				System.out.println(f+"\tglobal meta");
			else if(!hasExpTime || missingFrames)
				System.out.println(f+"\tmissing frames");
			else
				System.out.println(f+"\tok");
			
			}
		
		
		
		
		}
	}
