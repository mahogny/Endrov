/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util.debian;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

import endrov.util.EvFileUtil;

/**
 * Take all-platform release and turn into .deb
 * 
 * All code and jars should be dumped into:
 * /usr/share/endrov/
 * 
 * docs is put into
 * /usr/share/doc/endrov/
 * 
 * other files
 * /usr/share/man/man1/endrov.1.gz
 * /usr/share/menu/endrov 
 * /usr/share/pixmaps/endrov.xpm
 * 
 * http://freedesktop.org/wiki/Specifications/desktop-entry-spec?action=show&redirect=Standards%2Fdesktop-entry-spec
 * 
 * 
 * 
 * ============ goals ========
 * turn libraries into dependencies
 * delete plugins that are specicific to other OS'
 * integrate endrov into debian desktop
 * ======= debian guidelines ====
 * separate packaging system from project. this is why
 * this is a separate project.
 * automatic upgrades by downloading source
 * -> later problem
 * 
 * @author Johan Henriksson
 *
 */
public class Main
	{

	public static void main(String[] args)
		{
		try
			{
			File zip=null;
			if(args.length==1)
				zip=new File(args[0]);
			else
				{
				System.out.println("Argument: zip-file of release");
				System.exit(1);
				}
			
			
			
			File dPkg=new File("/tmp/endrov");
			File dUsr=new File(dPkg,"usr");
			//File dEtc=new File(dPkg,"etc");
			File dShare=new File(dUsr,"share");
			File dEndrov=new File(dShare,"endrov");
			File dEndrovLibs=new File(dEndrov,"libs");
			//File dMan=new File(dShare,"man/man1");
			//File dMenu=new File(dShare,"menu");
			File dControl=new File(dPkg,"DEBIAN");
			File dZipTemp=new File("/tmp/unzip");
			File dShareDoc=new File(dShare,"doc");
			//File dSharePixmaps=new File(dShare,"pixmaps");
			File dUsrBin=new File(dUsr,"bin");
			//File dShareApplications=new File(dShare,"applications");
			//File dEtcBash=new File(dEtc,"bash_completion.d");
			File dRes=new File("util/debian");
			
			File fUsrBinEndrov=new File(dUsrBin,"endrov");
			//File fIcon=new File(dSharePixmaps,"endrov.xpm");
			//File fUsrShareMenuEndrov=new File(dMenu,"endrov");
			//File fUsrShareApplicationsEndrov=new File(dShareApplications,"endrov.desktop");
			
			//Clean dirs
			if(dPkg.exists())
				recursiveDelete(dPkg);
			if(dZipTemp.exists())
				recursiveDelete(dZipTemp);
			
			//Make dirs
			dShare.mkdirs();
			dControl.mkdirs();
			dShareDoc.mkdirs();
			//dMan.mkdirs();
			//dMenu.mkdirs();
			dUsrBin.mkdirs();
			//dSharePixmaps.mkdirs();
			//dShareApplications.mkdirs();
			
			//Extract files
			System.out.println("unzipping "+zip.getPath());
			dZipTemp.mkdirs();
			Process proc=Runtime.getRuntime().exec(new String[]{"/usr/bin/unzip",zip.getPath(),"-d",dZipTemp.getPath()});
			BufferedReader os=new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while(os.readLine()!=null);
			proc.waitFor();
			System.out.println("unzip done");

			
			
			
			
			System.out.println("Moving into place, "+dZipTemp.listFiles()[0]+" ---> "+dEndrov);
			dZipTemp.listFiles()[0].renameTo(dEndrov);
			new File(dEndrov,"docs").renameTo(new File(dShareDoc,"endrov"));
			/*copyFile(new File(dRes,"endrov.1"), new File(dMan,"endrov.1"));
			copyFile(new File(dRes,"icon.xpm"), fIcon);
			copyFile(new File(dRes,"endrov.sh"),fUsrBinEndrov);
			copyFile(new File(dRes,"usrShareMenuEndrov"),fUsrShareMenuEndrov);
			copyFile(new File(dRes,"endrov.desktop"),fUsrShareApplicationsEndrov);
			copyFile(new File(dRes,"bash_completion"),new File(dEtcBash,"endrov"));*/
			
			copyRecursive(new File(dRes,"root"), dPkg);
			
			setExec(fUsrBinEndrov);
			setExec(new File(dControl,"postinst"));
			setExec(new File(dControl,"postrm"));
			//fUsrEndrov.setExecutable(true);
			
			System.out.println("Cleaning out windows/mac specific files");
			for(File f:dEndrov.listFiles())
				if(f.getName().startsWith("libmmgr_dal") || f.getName().endsWith(".jnilib") || f.getName().endsWith(".dll") || f.getName().startsWith("hs_err"))
					f.delete();
			
			
			System.out.println("Set up package dependencies");
			//Packages
			//maybe do pattern match instead?
			List<DebPackage> pkgs=new LinkedList<DebPackage>();
			pkgs.add(new DebPackage("java2-runtime",null,new String[]{"linux.paths"}));
			pkgs.add(new DebPackage("bsh",new String[]{"bsh-2.0b5.jar"},new String[]{"bsh-2.0b5.jar"}));
			pkgs.add(new DebPackage("junit",new String[]{"junit.jar"},new String[]{"junit.jar"}));
			pkgs.add(new DebPackage("libpg-java",new String[]{"postgresql.jar"},new String[]{"postgresql-8.2-505.jdbc3.jar"}));
			//pkgs.add(new DebPackage("libvecmath1.2-java",new String[]{"vecmath1.2.jar"},new String[]{"vecmath.jar"}));
			pkgs.add(new DebPackage("libbcel-java",new String[]{"bcel.jar"},new String[]{"bcel-5.2.jar"}));
			pkgs.add(new DebPackage("libservlet2.3-java",new String[]{"servlet-2.3.jar"},new String[]{"servlet.jar"})); //2.4 also exists
			pkgs.add(new DebPackage("libxalan2-java",new String[]{"xalan2.jar"},new String[]{"xalan.jar","xerces.jar","xml-apis.jar"}));
			pkgs.add(new DebPackage("libjdom1-java",new String[]{"jdom1.jar"},new String[]{"jdom.jar"})); //any overlap here?
			pkgs.add(new DebPackage("libjfreechart-java",new String[]{"jfreechart.jar"},new String[]{"jfreechart-1.0.5.jar"}));
			pkgs.add(new DebPackage("libjakarta-poi-java",new String[]{"jakarta-poi-contrib.jar","jakarta-poi.jar","jakarta-poi-scratchpad.jar"},new String[]{"poi-contrib-3.0.1-FINAL-20070705.jar","poi-3.0.1-FINAL-20070705.jar","poi-scratchpad-3.0.1-FINAL-20070705.jar"}));
			pkgs.add(new DebPackage("libjaxen-java",new String[]{"jaxen.jar"},new String[]{"jaxen-core.jar","jaxen-jdom.jar","saxpath.jar"}));
			
			
			//jogl2 must get into the repos!
			//pkgs.add(new DebPackage("libjogl-java",new String[]{"jogl.jar","gluegen-rt.jar"},new String[]{"gluegen-rt.jar","jogl.jar","libgluegen-rt.so","libjogl_awt.so","libjogl_cg.so","libjogl.so"}));
			
			//the system jutils seems to clash badly with the one needed by jogl2, hence jinput has to go for now
			//pkgs.add(DebPackage.recommends("libjinput-java",new String[]{"jinput.jar"},new String[]{"libjinput-linux.so","jinput.jar","jinput-test.jar"}));

			pkgs.add(new DebPackage("qhull-bin",new String[]{},new String[]{}));
			
			pkgs.add(DebPackage.recommends("micromanager",new String[]{},new String[]{"umanager_inc"})); //rely on inc-file to add jar files

			//JAI, seems to work without
			//the filter system might need some operations, not sure
			//pkgs.add(new DebPackage(null,null,new String[]{"jai_codec.jar","jai_core.jar","mlibwrapper_jai.jar","libmlib_jai.so"}));
			
			

			//Specialty for micro-manager
//			File dLibs=new File(dEndrov,"libs");
//			EvFileUtil.writeFile(new File(dLibs,"umanager.paths"), "b:/usr/lib/micro-manager");
			
			//For OME  http://trac.openmicroscopy.org.uk/omero/wiki/OmeroClientLibrary
			//Consider a separate OME package to reduce dependencies
			pkgs.add(new DebPackage("liblog4j1.2-java",new String[]{"log4j-1.2.jar"},new String[]{"log4j-1.2.14.jar"}));
			pkgs.add(new DebPackage("libjboss-aop-java",new String[]{"jboss-aop.jar"},new String[]{"jboss-aop-jdk50-4.2.1.GA.jar","jboss-aop-jdk50-client-4.2.1.GA.jar"}));
			pkgs.add(new DebPackage("libjboss-aspects-java",new String[]{"jboss-aspects.jar"},new String[]{"jboss-aspect-library-jdk50-4.2.1.GA.jar"}));
			pkgs.add(new DebPackage("libjboss-ejb3-java",new String[]{"jboss-ejb3.jar"},new String[]{"jboss-ejb3-4.2.1.GA.jar"}));
			pkgs.add(new DebPackage("libjboss-ejb3x-java",new String[]{"jboss-ejb3x.jar"},new String[]{"jboss-ejb3x-4.2.1.GA.jar"}));                 
			pkgs.add(new DebPackage("libjcommon-java",new String[]{"jcommon.jar"},new String[]{"jcommon-1.0.9.jar"}));
			pkgs.add(new DebPackage("libcommons-codec-java",new String[]{"commons-codec.jar"},new String[]{"commons-codec-1.3.jar"}));
			pkgs.add(new DebPackage("libcommons-httpclient-java",new String[]{"commons-httpclient.jar"},new String[]{"commons-httpclient-3.0.1.jar"}));           
			pkgs.add(new DebPackage("libcommons-logging-java",new String[]{"commons-logging.jar"},new String[]{"commons-logging-1.0.4.jar"}));                  

			//pkgs.add(new DebPackage("libcommons-io-java",new String[]{"commons-io.jar"},new String[]{"commons-io-2.0.1.jar"}));
			
			pkgs.add(DebPackage.recommends("ffmpeg",null,null));
			
			//Unused
			//pkgs.add(new DebPackage("lib-jline-java",new String[]{"jline-0.9.94.jar"}));
			//pkgs.add(new DebPackage("libjsch-java",new String[]{"jsch-0.1.34.jar"}));

			
			/*
			_jboss_remoting.jar
			jbossall-client-4.2.1.GA.jar         
			spring-2.0.6.jar
jboss-annotations-ejb3-4.2.1.GA.jar

jbossas4
libjboss-cluster-java
libjboss-connector-java
libjboss-deployment-java
libjboss-j2ee-java
libjboss-jms-java
libjboss-jmx-java
libjboss-management-java
libjboss-messaging-java
libjboss-naming-java
libjboss-security-java
libjboss-server-java
libjboss-system-java
libjboss-test-java
libjboss-transaction-java
libjboss-webservices-java
			 */
			
			System.out.println("Deleting binary files not for linux");
			deleteBinDirs(dEndrov);
			deleteExt(dEndrov, ".app"); //Mac APP-bundles
			
			//Check which packages are present
			for(DebPackage pkg:new LinkedList<DebPackage>(pkgs))
				{
				for(String jar:pkg.linkJars)
					{
					File jarfile=new File("/usr/share/java",jar);
					if(!jarfile.exists())
						{
						System.out.println("System jar does not exist: "+jarfile+", excluding package "+pkg.name);
						pkgs.remove(pkg);
						}
					}
				//TODO check that files exist 
				//for(String jar:pkg.providesFiles)

				}

			
			System.out.println("Extracting packages");
			deletePkgFiles(pkgs, dEndrovLibs);

			File manifestFile=File.createTempFile("MANIFEST", "");
			StringBuffer manifestContent=new StringBuffer();
			manifestContent.append("Manifest-Version: 1.0\n");
			manifestContent.append("Class-Path: \n");
			for(DebPackage pkg:pkgs)
				for(String jar:pkg.linkJars)
					{
					File jarfile=new File("/usr/share/java",jar);
					manifestContent.append(" "+jarfile.getAbsolutePath()+" \n");
					}
			manifestContent.append("\n");
			EvFileUtil.writeFile(manifestFile, manifestContent.toString());
			runUntilQuit(new String[]{"/usr/bin/jar","cmf",manifestFile.getAbsolutePath(),new File(dEndrovLibs,"debian.jar").getAbsolutePath()});

			System.out.println("Writing control file");
			Scanner scannerVersion = new Scanner(EvFileUtil.readFile(new File(dEndrov,"endrov/ev/version.txt")));
			Scanner scannerTimestamp = new Scanner(EvFileUtil.readFile(new File(dEndrov,"endrov/ev/timestamp.txt")));
			String version=scannerVersion.nextLine()+"."+scannerTimestamp.nextLine();
			int totalSize=(int)Math.ceil((recursiveSize(dUsr)+100000)/1000000.0);
			
			
			String controlFile=EvFileUtil.readFile(new File(dRes,"debiancontrol-TEMPLATE")).
			replace("DEPENDENCIES", makeDeps(pkgs)).
			replace("RECOMMENDS", makeRecommends(pkgs)).
			replace("SUGGESTS", makeSuggests(pkgs)).
			replace("VERSION",version).
			replace("SIZE",""+totalSize);
			System.out.println("--------------------------------------");
			System.out.println(controlFile);
			System.out.println("--------------------------------------");
			EvFileUtil.writeFile(new File(dControl,"control"), controlFile);
			
			System.out.println("Debianizing");
			
			//String datepart=zip.getName().substring(2,8);
//			File outDeb=new File(zip.getParentFile(),zip.getName().replace(".zip", ".deb"));
			File outDeb=new File(zip.getParentFile(),"endrov-"+version+".deb");

			
			if(outDeb.exists())
				outDeb.delete();
			runUntilQuit(new String[]{"/usr/bin/dpkg-deb","-b","/tmp/endrov"});
			//dpkg-deb -b endrov
			
			runUntilQuit(new String[]{"/bin/mv","/tmp/endrov.deb",outDeb.toString()});
			//boolean moveOk=new File("/tmp/endrov.deb").renameTo(outDeb);
			System.out.println(outDeb);
			
			
			System.out.println("Done");
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}	
		
		}

	public static void setExec(File file)
		{
		System.out.println("set exec "+file);
		runUntilQuit(new String[]{"/bin/chmod","+x",file.getPath()});
		}
	
	public static void runUntilQuit(String[] arg)
		{
		try
			{
			Process proc=Runtime.getRuntime().exec(arg);
			BufferedReader os=new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while(os.readLine()!=null);
			proc.waitFor();
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		
		}
	
	
	public static String makeDeps(List<DebPackage> pkgs) throws Exception
		{
		StringBuffer sb=new StringBuffer();
		boolean first=true;
		for(DebPackage p:pkgs)
			if(p.name!=null && p.isDepends)
				{
				if(!first)
					sb.append(",");
				sb.append(p.name);
				first=false;
				}
		return sb.toString();
		}
	
	public static String makeSuggests(List<DebPackage> pkgs) throws Exception
		{
		StringBuffer sb=new StringBuffer();
		boolean first=true;
		for(DebPackage p:pkgs)
			if(p.name!=null && p.isSuggestion)
				{
				if(!first)
					sb.append(",");
				sb.append(p.name);
				first=false;
				}
		return sb.toString();
		}
	
	
	public static String makeRecommends(List<DebPackage> pkgs) throws Exception
		{
		StringBuffer sb=new StringBuffer();
		boolean first=true;
		for(DebPackage p:pkgs)
			if(p.name!=null && p.isRecommended)
				{
				if(!first)
					sb.append(",");
				sb.append(p.name);
				first=false;
				}
		return sb.toString();
		}
	
	public static void deletePkgFiles(List<DebPackage> pkgs, File root)
		{
		boolean toDel=false;
		String fname=root.getName();
		for(DebPackage p:pkgs)
			if(p.providesFiles.contains(fname))
				{
				toDel=true;
				break;
				}
		if(toDel)
			recursiveDelete(root);
		else
			{
			if(root.isDirectory())
				for(File child:root.listFiles())
					deletePkgFiles(pkgs, child);
			}
		}

	public static void deleteExt(File root, String ext)
		{
		for(File child:root.listFiles())
			if(child.getName().endsWith(ext))
				recursiveDelete(child);
			else if(child.isDirectory())
				deleteExt(child, ext);
		}
	
	
	public static void deleteBinDirs(File root)
		{
		for(File child:root.listFiles())
			{
			if(child.isDirectory() && child.getName().startsWith("bin_"))
				{
				String osName=child.getName().substring(4);
				if(!osName.equals("linux"))
					recursiveDelete(child);
				}
			else if(child.isDirectory())
				deleteBinDirs(child);
			}
		}
	
	
	public static void copyFile(File in, File out) throws IOException 
		{
		//limitation http://forums.sun.com/thread.jspa?threadID=439695&messageID=2917510
		out.getParentFile().mkdirs();
		FileChannel inChannel = new	FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try{inChannel.transferTo(0, inChannel.size(), outChannel);} 
		catch (IOException e){throw e;}
		finally 
			{
			if (inChannel != null) inChannel.close();
			if (outChannel != null) outChannel.close();
			}
		}
	
	
	public static void copyRecursive(File in, File out) throws IOException 
		{
		if(in.isDirectory())
			{
			for(File c:in.listFiles())
				if(!c.getName().equals(".") && !c.getName().equals(".."))
					{
					File outc=new File(out,c.getName());
					if(c.isDirectory())
						outc.mkdirs();
					copyRecursive(c, outc);
					}
			}
		else
			copyFile(in,out);
		}
	
	
	public static void recursiveDelete(File root)
		{
		//System.out.println("delete "+root);
		if(root.isDirectory())
			for(File child:root.listFiles())
				recursiveDelete(child);
		root.delete();
		}

	public static long recursiveSize(File root)
		{
		//System.out.println("delete "+root);
		if(root.isDirectory())
			{
			long size=0;
			for(File child:root.listFiles())
				size+=recursiveSize(child);
			return size;
			}
		else
			return root.length();
		}

	
	
	}
/*			String line;
while((line=os.readLine())!=null)
	System.out.println(line);*/
//ProcessBuilder pb=new ProcessBuilder("/usr/bin/unzip",zip.getPath(),"-d","/tmp");
//pb.start().waitFor();
