https://jogl-demos.dev.java.net/applettest.html

<applet code="org.jdesktop.applet.util.JNLPAppletLauncher"
      width=600
      height=400
      archive="http://download.java.net/media/applet-launcher/applet-launcher.jar,
               http://download.java.net/media/jogl/builds/archive/jsr-231-webstart-current/jogl.jar,
               http://download.java.net/media/gluegen/webstart/gluegen-rt.jar,
               http://download.java.net/media/jogl/builds/archive/jsr-231-webstart-current/jogl-demos.jar">
   <param name="codebase_lookup" value="false">
   <param name="subapplet.classname" value="demos.applets.GearsApplet">
   <param name="subapplet.displayname" value="JOGL Gears Applet">
   <param name="noddraw.check" value="true">
   <param name="progressbar" value="true">
   <param name="jnlpNumExtensions" value="1">
   <param name="jnlpExtension1"
          value="http://download.java.net/media/jogl/builds/archive/jsr-231-webstart-current/jogl.jnlp">
</applet>

