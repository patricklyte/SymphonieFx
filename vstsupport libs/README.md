# JVstHost

JVstHost is an open-source Java-based VST host for Linux, OS X, and Windows written by Martin Roth and Matthew Yee-King. It aims to load and manipulate all audio plugins conforming to the Steinberg VST standard, including those generated with jVSTwRapper.

Note that the (only) constructor of JVstHost throws an exception of type `com.synthbot.audioplugin.vst.JVstLoadException`. There are several  things which may go wrong while loading a VST. For this reason, an exception  is thrown with the details (see `JVstLoadException.getMessage()`).

A class implementing the `com.synthbot.audioplugin.vst.JVstHostListener` interface can also register itself with a JvstHost object. It will receive callbacks from the plugin.

JVstHost is licensed under the GNU [Lesser General Public License](http://www.gnu.org/copyleft/lesser.html) (LGPL).


## Getting Started
 
All that you really need is to put JVstHost.jar into your java class path, and to put:
* `libjvsthost2.jnilib` into `/Library/Java/Extensions` (Mac OS X)
* `libjvsthost2.so` into `/usr/local/lib` (Linux)
* `jvsthost2.dll` into `C:\WINDOWS\system32` (Windows)
The directories may be system dependent. They must simply by listed in the Java classpath so that Java will know where to look for them.

**NOTE:** Not all synths work. If you have one that you are particularly keen on working with, please e-mail us and we'll try to debug it with you.


## Testing It Out

To run the test program:

`java -jar JVstHost.jar <plugin name>`

e.g. (LINUX)

`java -jar JVstHost.jar ./mdaDX10.so`

e.g. (MAC)

`java -jar JVstHost.jar ~/Library/Audio/Plug-Ins/VST/mda\ DX10.vst`


## jVSTwRapper 

JVstHost does interoperate with [jVSTwRapper](http://jvstwrapper.sourceforge.net/ ). Ensure that the `jVSTsYstem.jar` library (included with jVSTwRapper) is included in the classpath when starting the host. In Mac OS X, this library is most commonly found in the jVSTwRapper vst directory, `./Contents/Resources`


## Compilation

### Mac OS X 
A script file, buildJvh.sh, is included for compiling the JVstHost native library under Mac OS X. The script is made for 10.5, however it should be easily modifiable for any other version of the operating system. The script assumes that the Steinberg VST libraries (not included in this distribution due to Steinberg's licensing terms) are in the package subdirectory, `./vst2.x`. The script will also copy the resulting library, `libjvsthost2.jnilib`, to `/Library/Java/Extensions`. An ant build script is included to build and test the Java parts of JVstHost. To build the jar: `ant jar`.

### Linux 
A script file, `buildJvh_linux.sh`, is included for compiling the JVstHost native library under Linux. The script assumes that the Steinberg VST libraries (not included in this distribution due to Steinberg's licensing terms) are in the package subdirectory, `./vst2.x`.

### Windows
Compiling the native library is unfortunately somewhat more complicated under Windows. We use [Cygwin](http://www.cygwin.com/) and [MinGW](http://www.mingw.org/), and not Visual Studio (it was hard enough for us to get a hold of a Windows machine to develop on, let alone Visual Studio). Use the `buildJvh_win.sh` script.


## Programming API

There is a basic idiom to loading a VST, which is written below.

```Java
JVstHost2 vst;
try {
  vst = JVstHost2.newInstance(vstFile, SAMPLE_RATE, BLOCK_SIZE);
} catch (FileNotFoundException fnfe) {
  fnfe.printStackTrace(System.err);
} catch (JVstLoadException jvle) {
  jvle.printStackTrace(System.err);
}
```

This will return to you a fully initialised and operational vst. JVstHost2 does not have a constructor. Use only the newInstance method.