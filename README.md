# gradle-cmake-plugin
This plugin allows to configure and build using CMake. 

This plugin should work as documented, but is in an early development phase. 
If you have requests or find bugs, please create an issue.

## Prerequisites

* `CMake` installed on the system. Available [here](https://www.cmake.org "CMake Homepage").

## To apply the plugin:

**plugins DSL**

	plugins {
	  id 'net.freudasoft.gradle-cmake-plugin' version '0.0.2'
	}

**Legacy plugin application**

	buildscript {
	  repositories {
	    maven {
	      url "https://plugins.gradle.org/m2/"
	    }
	  }
	  dependencies {
	    classpath 'net.freudasoft:gradle-cmake-plugin:0.0.2'
	  }
	  repositories {
	    mavenCentral()
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
	  installPrefix="${System.properties['user.home']}"

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
	  // define arbitrary CMake parameters. The below adds -Dtest=hello to cmake command line.
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

You can create custom tasks the following way:

	task configureFoo(type: net.freudasoft.CMakeConfigureTask) {
	  sourceFolder=file("$projectDir/src/main/cpp/foo")
	  workingFolder=file("$buildDir/cmake/foo")
	  // ... other parameters you need, see above, except the ones listed under cmakeBuild Parameters
	}
	
	task buildFoo(type: net.freudasoft.CMakeBuildTask) {
	  workingFolder=file("$buildDir/cmake/foo")
	  // ... other parameters you need, see above, except the ones listed under cmakeConfigure parameters
	}

	buildFoo.dependsOn configureFoo // optional --- make sure its configured when you run the build task

### Custom tasks using main configuration

You can also "import" the settings you've made in the main configuration "cmake" using the 'configureFromProject()' call:

	cmake {
	  executable='/my/path/to/cmake'
	  workingFolder=file("$buildDir/cmake")

	  sourceFolder=file("$projectDir/src/main/cpp")
	  installPrefix="${System.properties['user.home']}"

	  generator='Visual Studio 15 2017'
	  platform='x64'
	}

	task cmakeConfigureX86(type: net.freudasoft.CMakeConfigureTask) {
	  configureFromProject() // uses everything in the cmake { ... } section.

	  // overwrite target platform
	  platform='x86'
	  // set a different working folder to not collide with default task
	  workingFolder=file("$buildDir/cmake_x86")
	}
	
	task cmakeBuildX86(type: net.freudasoft.CMakeBuildTask) {
	  configureFromProject() // uses everything in the cmake { ... } section.
	  workingFolder=file("$buildDir/cmake_x86")
	}

	cmakeBuildX86.dependsOn cmakeConfigureX86

## Stability

This is a very young project. There might be some API breaking changes in newer versions.

## License

All these plugins are licensed under the Apache License, Version 2.0 with no warranty (expressed or implied) for any purpose.
