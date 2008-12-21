import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

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
			File zip=new File("../ev081220.zip");
			//File zip=new File(args[0]);
			
			File dPkg=new File("/tmp/endrov");
			File dUsr=new File(dPkg,"usr");
			File dShare=new File(dUsr,"share");
			File dEndrov=new File(dShare,"endrov");
			File dMan=new File(dShare,"man1/man");
			File dMenu=new File(dShare,"menu");
			File dControl=new File(dPkg,"DEBIAN");
			File dZipTemp=new File("/tmp/unzip");
			File dShareDoc=new File(dShare,"doc");
			File dSharePixmaps=new File(dShare,"pixmaps");
			
			File fUsrEndrov=new File(dEndrov,"endrov");
			File fIcon=new File(dSharePixmaps,"endrov.xpm");
			
			//Clean dirs
			if(dPkg.exists())
				recursiveDelete(dPkg);
			if(dZipTemp.exists())
				recursiveDelete(dZipTemp);
			
			//Make dirs
			dShare.mkdirs();
			dControl.mkdirs();
			dShareDoc.mkdirs();
			dMan.mkdirs();
			dMenu.mkdirs();
			dSharePixmaps.mkdirs();
			
			//Extract files
			System.out.println("unzipping");
			Process proc=Runtime.getRuntime().exec(new String[]{"/usr/bin/unzip",zip.getPath(),"-d",dZipTemp.getPath()});
			BufferedReader os=new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while(os.readLine()!=null);
			proc.waitFor();
			System.out.println("unzip done");

			System.out.println("Moving into place");
			dZipTemp.listFiles()[0].renameTo(dEndrov);
			new File(dEndrov,"docs").renameTo(new File(dShareDoc,"endrov"));
			copyFile(new File("endrov.1"), new File(dMan,"endrov.1"));
			copyFile(new File("icon.xpm"), fIcon);
			copyFile(new File("endrov.sh"),fUsrEndrov);
			fUsrEndrov.setExecutable(true);
			
			//Packages
			//maybe do pattern match instead?
			List<DebPackage> pkgs=new LinkedList<DebPackage>();
			pkgs.add(new DebPackage("java2-runtime","",null));
			pkgs.add(new DebPackage("bsh","bsh",new String[]{"bsh-2.0b4.jar"}));
			pkgs.add(new DebPackage("junit","junit",new String[]{"junit.jar"}));
			pkgs.add(new DebPackage("libpg-java","postgresql",new String[]{"postgresql-8.2-505.jdbc3.jar"}));
			pkgs.add(new DebPackage("libvecmath1.2-java","vecmath",new String[]{"vecmath.jar"}));
			//micro-manager, does it work with bp?
			pkgs.add(new DebPackage("micro-manager","micro-manager",new String[]{"umanager_inc"}));
			pkgs.add(new DebPackage("libbcel-java","bcel",new String[]{"bcel-5.2.jar"}));
			pkgs.add(new DebPackage("libservlet2.3-java","servlet",new String[]{"servlet.jar"})); //2.4 also exists
			pkgs.add(new DebPackage("libxalan2-java","xalan2",new String[]{"xalan.jar"}));
			//xerces and xml-apis separate?
			pkgs.add(new DebPackage("libxerces2-java","xerces xml-apis",new String[]{"xerces.jar","xml-apis.jar"}));
			pkgs.add(new DebPackage("libjdom-java","jdom",new String[]{"jdom.jar"})); //any overlap here?
			//jogl TODO. how to handle so's?
			pkgs.add(new DebPackage("libjogl-java","",new String[]{"gluegen-rt.jar","jogl.jar","libgluegen-rt.so","libjogl_awt.so","libjogl_cg.so","libjogl.so"}));
			pkgs.add(new DebPackage("libjfreechart-java","jfreechart",new String[]{"jfreechart-1.0.5.jar"}));
			pkgs.add(new DebPackage("libjakarta-poi-java","poi",new String[]{"poi-contrib-3.0.1-FINAL-20070705.jar","poi-3.0.1-FINAL-20070705.jar","poi-scratchpad-3.0.1-FINAL-20070705.jar"}));
			pkgs.add(new DebPackage("libsaxpath-java","saxpath",new String[]{"saxpath.jar"}));
			pkgs.add(new DebPackage("libjaxen-java","jaxen-core jaxen-jdom",new String[]{"jaxen-core.jar","jaxen-jdom.jar"}));
			/*pkgs.add(new DebPackage("",new String[]{""});
			pkgs.add(new DebPackage("",new String[]{""});*/
			//jinput-test.jar jinput.jar
			//JAI, can I trust it to be included? reduce need?

			
			
			//For OME  http://trac.openmicroscopy.org.uk/omero/wiki/OmeroClientLibrary
			pkgs.add(new DebPackage("liblog4j1.2-java","log4j",new String[]{"log4j-1.2.14.jar"}));
			pkgs.add(new DebPackage("libjboss-aop-java","jboss-aop",new String[]{"jboss-aop-jdk50-4.2.1.GA.jar","jboss-aop-jdk50-client-4.2.1.GA.jar"}));
			pkgs.add(new DebPackage("libjboss-aspects-java","jboss-aspects",new String[]{"jboss-aspect-library-jdk50-4.2.1.GA.jar"}));
			pkgs.add(new DebPackage("libjboss-ejb3-java","jboss-ejb3",new String[]{"jboss-ejb3-4.2.1.GA.jar"}));
			pkgs.add(new DebPackage("libjboss-ejb3x-java","jboss-ejb3x",new String[]{"jboss-ejb3x-4.2.1.GA.jar"}));                 
			pkgs.add(new DebPackage("libjcommon-java","jcommon",new String[]{"jcommon-1.0.9.jar"}));
			pkgs.add(new DebPackage("libcommons-codec-java","commons-codec",new String[]{"commons-codec-1.3.jar"}));
			pkgs.add(new DebPackage("libcommons-httpclient-java","commons-httpclient",new String[]{"commons-httpclient-3.0.1.jar"}));           
			pkgs.add(new DebPackage("libcommons-logging-java","commons-logging",new String[]{"commons-logging-1.0.4.jar"}));                  

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
			
			System.out.println("Extracting packages");
			deletePkgFiles(pkgs, new File(dEndrov,"libs"));
			
			System.out.println("Writing control file");
			Scanner scanner = new Scanner(EvFileUtil.readFile(new File(dEndrov,"endrov/ev/version.txt")));
			String version=scanner.nextLine();
			String depString=makeDeps(pkgs);
			int totalSize=(int)Math.ceil((recursiveSize(dUsr)+100000)/1000000.0);
			
			EvFileUtil.writeFile(new File(dControl,"control"), 
					EvFileUtil.readFile(new File("debiancontrol-TEMPLATE")).
					replace("DEPENDENCIES", depString).
					replace("VERSION",version).
					replace("SIZE",""+totalSize));
			
			System.out.println("Done");
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}	
		
		}

	public static String makeDeps(List<DebPackage> pkgs) throws Exception
		{
		StringBuffer sb=new StringBuffer();
		boolean first=true;
		for(DebPackage p:pkgs)
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
