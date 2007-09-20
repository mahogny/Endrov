rem *******************************************************************************
rem  Copyright (c) 2005, 2007 IBM Corporation and others.
rem  All rights reserved. This program and the accompanying materials
rem  are made available under the terms of the Eclipse Public License v1.0
rem  which accompanies this distribution, and is available at
rem  http://www.eclipse.org/legal/epl-v10.html
rem 
rem  Contributors:
rem      IBM Corporation - initial API and implementation
rem *******************************************************************************
@ECHO OFF

SET MEMSIZE=700M

:: Windows-specific
SET JARS=.;libs/windows/postgresql-8.2-505.jdbc3.jar;libs/windows/sqlitejdbc-v033-native.jar;libs/windows/gluegen-rt.jar;libs/windows/jogl.jar;libs/windows/jai_codec.jar;libs/windows/jai_core.jar;libs/windows/mlibwrapper_jai.jar;
:: Portable
SET JARS=%JARS%libs/jcommon-1.0.9.jar;libs/jfreechart-1.0.5.jar;libs/servlet.jar;libs/gnujaxp.jar;libs/itext-2.0.1.jar;libs/junit.jar;
SET JARS=%JARS%libs/gnujaxp.jar;libs/jdom.jar;libs/jaxen-core.jar;libs/jaxen-jdom.jar;libs/saxpath.jar;libs/vecmath.jar;libs/bio-formats.jar;
SET JARS=%JARS%libs/xalan.jar:libs/xerces.jar:libs/xml-apis.jar

SET JAVACMD=java -Djava.library.path=libs/windows -cp %JARS% -Xmx%MEMSIZE%

IF "%1" == "" GOTO RUNDEF
GOTO RUNARG

:: ---- with argument ----
:RUNARG
@ECHO ON
%JAVACMD% %1
@ECHO OFF
GOTO END

:: ---- just run GUI ----
:RUNDEF
@ECHO ON
%JAVACMD% evgui.GUI
@ECHO OFF
GOTO END

:END
