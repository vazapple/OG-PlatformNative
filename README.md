OpenGamma platform native
-------------------------

This repository contains the native extensions to the OpenGamma Platform.

This repository is focussed on non-Java projects that build on the Java
platform. Projects located in this repository use a combination of Ant and
Maven to build and deploy.

You will need Ant and Maven to build these packages.

The nature of the build will depend on the platform and tools available. To get
started, run `ant configure` with one or more of the following parameters to
set up the local environment.

<dl>
  <dt>-Dprofile.nix=true</dt>
  <dd>Build all Posix-style artifacts (for Max and Linux users)</dd>
  <dt>-Dprofile.windows=true</dt>
  <dd>Build all Windows artifacts</dd>
  <dt>-Dtool.ai=true</dt>
  <dd>Advanced Installer is available for building Windows installation packages</dd>
  <dt>-Dtool.cpptasks=true</dt>
  <dd>The Ant CPPTasks component is available for C/C++ compilation</dd>
  <dt>-Dtool.msvc=true</dt>
  <dd>Microsoft Visual Studio is available for C/C++ compilation</dd>
  <dt>-Dtool.r=true</dt>
  <dd>R tools are available for building the OpenGamma R plugin</dd>
</dl>

For details of additional `profile.*` properties that can further refine the
targets, for example `profile.debug.windows` please refer to the `build.xml`
file.

After the `configure` task has been run, Maven can be invoked with the correct
parameters using `ant install`.

Note that the default build target will perform the `configure` action before
launching Maven to perform its `install` action to compile and deploy the
available projects. For example, to build all `debug` 32-bit Windows components
with Visual Studio and R available, use:

    ant -Dprofile.debug.windows.win32=true -Dtool.msvc=true -Dtool.r=true


### Working with Eclipse

Due to the structure of this project the Maven import tool does not work.
Eclipse project files can be generated by running Ant after download. If you
have previously imported any of these projects into the workspace it is
recommended that you delete them before proceeding. To import the Java projects
into a new (or existing) Eclipse workspace, use:

    ant eclipse

This will download any dependencies and perform a basic compile. If the
`ant configure` task has not previously been run this will just build the Java
source code.

After this task completes, the projects can be imported into Eclipse. This has
been tested with the Kepler version of Eclipse platform (4.3.0):

<ol>
  <li>Choose the "File -> Import" menu item</li>
  <li>Select the "General -> Existing Projects into Workspace" option</li>
  <li>In the popul, click the "Browse..." button (for "Select root directory")</li>
  <li>Choose the root directory of the OG-PlatformNative download (containing this file)</li>
  <li>Select all of the projects found, if they aren't already selected</li>
  <li>Click "Finish"</li>
</ol>

After updating source code (using `git pull` for example) it is recommended
that `ant eclipse` be repeated in order to pick up any changes to project
dependencies that might affect the build.

By default, the Eclipse project files will refer to libraries that are
available in your local Maven repository. This will include the OpenGamma
Platform. If you have already imported the OpenGamma Platform source code into
Eclipse, then the following command should be used:

    ant eclipse -Declipse.workspace=/path/to/your/eclipse/workspace

This will recognise the OpenGamma Platform projects you already have installed
and ensure that these are referenced based on the source code and not the JARs
from the last Maven compilation.

Alternatively, to avoid entering the workspace path each time the project files
are generated, create a file called `local/eclipse.properties` to include the
line:

    eclipse.workspace=/path/to/your/eclipse/workspace

[![OpenGamma](http://developers.opengamma.com/res/display/default/chrome/masthead_logo.png "OpenGamma")](http://developers.opengamma.com)
