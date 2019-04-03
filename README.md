# gradle-cmake-plugin
This plugin allows to configure and build using CMake. 

Plugin applies the base plugin automatically, and hooks msbuild output folders into the clean task process. Below tasks are provided by the plugin:

## Prerequisites

* CMake

## To apply the plugin:

**plugins DSL**

	plugins {
	  id 'net.freudasoft.gradle-cmake-plugin' version '0.0.1'
	}

**Legacy plugin application**

	buildscript {
	  repositories {
	    maven {
	      url "https://plugins.gradle.org/m2/"
	    }
	  }
	  dependencies {
	    classpath "net.freudasoft:gradle-cmake-plugin:0.0.1"
	  }
	}

	apply plugin: "net.freudasoft.gradle-cmake-plugin"

and configure by:

	cmake {
	  // optional configration to path of cmake. Not required if cmake is on the path.
	  executable='/my/path/to/cmake'
	  // optional working folder. default is ./build/cmake
	  workingFolder=file("$buildDir/cmake")

	  ////////////////////
	  // cmakeConfigure parameters
	  ////////////////////
	  // optional source folder. This is where the main CMakeLists.txt file resides. Default is ./src/main/cpp
	  sourceFolder=file("$projectDir/src/main/cpp")
	  // optional install prefix. By default, install prefix is empty.
	  installPrefix=file("${System.properties['user.home']}")

	  // select a generator (optional, otherwise cmake's default generator is used)
	  generator='Visual Studio 15 2017'
	  // set a platform for generators that support it (usually Visual Studio)
	  platform='x64'
	  // set a toolset generators that support it (usually only Visual Studio)
	  toolset='v141'
  
	  // optionally set to build static libs
	  buildStaticLibs=true
	  // optionally set to build shared libs
	  buildSharedLibs=true
	  // define any CMAKE parameter. The below adds -Dtest=hello to cmake parameters.
	  def.test='hello'

	  ////////////////////
	  // cmakeBuild parameters
	  ////////////////////
	  // optional configuration to build
	  buildConfig='Release'
	  // optional build target
	  buildTarget='install'
	  // optional build clean. if set to true, calls cmake --build with --clean-first
	  buildClean=false
	}

## Auto-created tasks

* *cmakeConfigure*: Calls CMake to generate your build scripts in the folder selected by workingFolder.

* *cmakeBuild*: Calls CMake --build in the folder selected by workingFolder to actually build.

* *cmakeClean*: Cleans the workingFolder.

## examples

clean, configure and build:
	
	./gradlew cmakeClean cmakeConfigure cmakebBuild

if you have assemble and clean tasks in your gradle project already you can also use:
	
	assemble.dependsOn cmakeBuild
	cmakeBuild.dependsOn cmakeConfigure
	clean.dependsOn cmakeClean

and just call

	./gradlew clean assemble
	
If you want to get the output of cmake, add -i to your gradle call, for example:
	
	./gradlew cmakeConfigure -i
	
## Custom tasks

~~You can create custom tasks like so:~~
**not yet implemented**

	import net.freudasoft.CMakePlugin

	task configureFoo(type: CMakePlugin) {
	  sourceFolder=file("$projectDir/src/main/cpp/foo")
	  // Other properties
	}



## License

All these plugins are licensed under the Apache License, Version 2.0 with no warranty (expressed or implied) for any purpose.
