package util2.misc;

import endrov.ev.*;
import endrov.imagesetImserv.EvImserv;
import endrov.nuc.*;


/**
 * Collect statistics from model
 * @author Johan Henriksson
 */
public class CEmodelStats
	{
	public static void main(String[] args)
		{
		try
			{
			Log.listeners.add(new StdoutLog());
			EV.loadPlugins();


			///////////
			String url="imserv://:@localhost/";
//			String query="not trash and CCM";
			//EvImserv.EvImservSession session=
			EvImserv.getSession(new EvImserv.ImservURL(url));
//			String[] imsets=session.conn.imserv.getDataKeys(query);
			NucLineage reflin=EvImserv.getImageset(url+"celegans2008.2").getObjects(NucLineage.class).iterator().next();
			//////////


			
			for(String nucName:reflin.nuc.keySet())
				{
				NucLineage.Nuc nuc=reflin.nuc.get(nucName);
				if(nuc.pos.size()>1)
					{
					NucExp exp=nuc.exp.get("divDev");
					if(exp!=null && !exp.level.isEmpty())
						{
						double divdevl=exp.level.values().iterator().next();
						
						double lifelen=nuc.lastFrame()-nuc.firstFrame();
						if(lifelen!=0)
							System.out.println(nucName+"\t"+(divdevl/lifelen));
						
						
						
						}
					}
				}
			
			


			
			
			
			
			
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		System.out.println("done");
		System.exit(0);
		}
	
	

	
	}



//rewrite this code using retain
//int fa=lifeLenFrames/clength;
/*nextcurp: */
/*
int m=curp*lifeLenFrames/clength; //corresponding frame
int fl=(int)Math.floor(m);
int ce=(int)Math.ceil(m);
//if(ce>=0 && ce<=lifeLenFrames && fl>=0 && fl<=lifeLenFrames)
	for(int x:lin.contactsf.get(nucName).get(nucName2))
		{
		int nf=Math.round(x-lin.lin.nuc.get(nucName).firstFrame());
		if(nf==ce || nf==fl)
			{
			neighOverlaps[curp]=true;
			continue nextcurp; //Optimization
			}
		}

 */
//if(percLifeLen>1)
/* || nucName.equals(nucName2)*/

//Override color for match on itself when it exists
/*							if(nucName.equals(nucName2) && lin.lin.nuc.containsKey(nucName))
								{
//								timeString="";
//								neighString="100%";
								neighColor=timeColor=htmlColorSelf;
								}*/


//TODO stretch image
/*									StringBuffer imgcode=new StringBuffer();
									for(boolean b:neighOverlaps)
										imgcode.append("<img src=\""+(b ? 'n' : 'a')+"_bar.png\">");*/


//Override occurance for self-referal
//if(nucName.equals(nucName2))
//sa=occurance;

