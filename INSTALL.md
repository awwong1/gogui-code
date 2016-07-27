# GoGui Installation Notes

## Basics

GoGui requires at least Java version 5\. The recommended Java runtime environment is Oracle's Java SE 7 or a new version of OpenJDK (it might work on older versions of OpenJDK but there can be minor rendering issues, e.g. the stones on the board are not completely round).

GoGui comes with precompiled Java jar files for GoGui and the GTP tools in the lib directory. GoGui does not necessarily need to be installed on the system, it can be run from the directory where the distribution file was extracted to.

For Unix systems there are launcher scripts in the bin directory. The launcher scripts respect the environment variable GOGUI_JAVA_HOME, if defined, and look for the jar files relative to the bin directory.

## Installation on Linux

For Linux, there is an installation script <tt>install.sh</tt>, which installs GoGui into the system directories and integrates it into the desktop environment. This script needs to be run as root. It has options to specify the location of the java runtime (-j, default is /usr), the installation directory (-p, default is /usr/local) and sysconf directory (-s, default is etc relative to the installation directory).

On newer Linux systems with a recent version of OpenJDK, you probably do not have to change the default parameters and you can simply run

> <tt>sudo ./install.sh</tt>.

## Installation on Windows

A Windows installer is available from GoGui's project page.

## Installation on Mac OS

A Mac application bundle can be created by compiling GoGui from the sources (see below) and running

> <tt>ant gogui.app</tt>.

## Compiling from the Sources

*   Compiling GoGui from the sources needs the [Ant build system](http://ant.apache.org).
*   The HTML documentation is generated using Docbook. This requires that the XSL transformer [xsltproc](http://xmlsoft.org/XSLT) and the [Docbook XSL Stylesheets](http://sourceforge.net/projects/docbook/files/docbook-xsl/) are installed. The location of the Docbook XSL directory can be defined with the ant option -Ddocbook-xsl.dir=directory (default is /usr/share/xml/docbook/stylesheet/docbook-xsl).
*   To run the validation of the GoGui documentation XML sources (which is part of the check target in build.xml), a local copy of the Docbook DTD 4.2 needs to be installed. The location of the file can be defined with the ant option -Ddocbook.dtd-4.2=file (default is /usr/share/xml/docbook/schema/dtd/4.2/docbookx.dtd).
*   The file quaqua.jar from the Quaqua Look and Feel (http://www.randelshofer.ch/quaqua) needs to be downloaded and copied to lib/quaqua.jar. The recommended version tested with GoGui is in http://www.randelshofer.ch/quaqua/files/quaqua-5.2.1.nested.zip For building GoGui with limited support for the Mac, Quaqua can be disabled by invoking ant with the option -Dquaqua.ignore=true. GoGui will use Quaqua if it finds the file quaqua.jar in the same directory as gogui.jar, so it is still possible to manually copy it there later.
*   If all requirements are met, simply type <tt>ant</tt> in the main directory of GoGui.
