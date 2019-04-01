/**
 * Copyright 2019 Marco Freudenberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.freudasoft;

import org.gradle.api.*;
import org.gradle.api.logging.LogLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CMakePlugin implements Plugin<Project> {

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @Override
    public void apply(Project project) {
        CMakePluginExtension extension = project.getExtensions().create("cmake", CMakePluginExtension.class,
                new File( project.getBuildDir(), "cmake"),
                new File( project.getProjectDir(), "src" + File.separator + "main" + File.separator+ "cpp")
        );

        Task cmakeConfigure = project.task("cmakeConfigure").doLast(task -> {
            List<String> cmdLine = extension.buildConfigCommandLineParameters();

            // log command line parameters
            StringBuilder sb = new StringBuilder("  CMakePlugin.cmakeConfigure: ");
            for ( String s : cmdLine ) {
                sb.append(s).append(" ");
            }
            project.getLogger().log(LogLevel.INFO, sb.toString());

            // build process
            ProcessBuilder pb = new ProcessBuilder(cmdLine);
            pb.directory( extension.getWorkingFolder() );

            try {
                // make sure working folder exists
                extension.getWorkingFolder().mkdirs();

                // start
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    project.getLogger().log(LogLevel.INFO, line);
                }
                if ( null != (line = errorReader.readLine()) ) {
                    project.getLogger().log(LogLevel.QUIET, "  CMakePlugin.cmakeConfigure - ERRORS: " );
                    do {
                        project.getLogger().log(LogLevel.QUIET, line);
                    } while ((line = errorReader.readLine()) != null);
                }


                int retCode = process.waitFor();
                if ( retCode != 0 )
                    throw new GradleException("Error: CMAKE configuration returned "+retCode );
            }
            catch ( IOException e ) {
                throw new GradleScriptException( "cmake configuration failed.", e );
            }
            catch ( InterruptedException e ) {
                throw new GradleScriptException( "cmake configuration failed.", e );
            }
        } );
        cmakeConfigure.setGroup("cmake");
        cmakeConfigure.setDescription("Configure a Build with CMake");


        Task cmakeBuild = project.task("cmakeBuild").doLast(task -> {
            List<String> cmdLine = extension.buildBuildCommandLineParameters();

            // log command line parameters
            StringBuilder sb = new StringBuilder("  CMakePlugin.cmakeBuild: ");
            for ( String s : cmdLine ) {
                sb.append(s).append(" ");
            }
            project.getLogger().log(LogLevel.INFO, sb.toString());

            // build process
            ProcessBuilder pb = new ProcessBuilder(cmdLine);
            try {
                // start
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    project.getLogger().log(LogLevel.INFO, line);
                }
                if ( null != (line = errorReader.readLine()) ) {
                    project.getLogger().log(LogLevel.QUIET, "  CMakePlugin.cmakeBuild - ERRORS: " );
                    do {
                        project.getLogger().log(LogLevel.QUIET, line);
                    } while ((line = errorReader.readLine()) != null);
                }

                int retCode = process.waitFor();
                if ( retCode != 0 )
                    throw new GradleException("Error: CMAKE build returned "+retCode );
            }
            catch ( IOException e ) {
                throw new GradleScriptException( "cmake build failed.", e );
            }
            catch ( InterruptedException e ) {
                throw new GradleScriptException( "cmake build failed.", e );
            }
        } );
        cmakeBuild.setGroup("cmake");
        cmakeBuild.setDescription("Build a configured Build with CMake");
        cmakeBuild.dependsOn(cmakeConfigure);

        Task cmakeClean = project.task( "cmakeClean" ).doFirst(task -> {
            // should go to clean...
            File workingFolder = extension.getWorkingFolder().getAbsoluteFile();
            if ( workingFolder.exists() ) {
                project.getLogger().log(LogLevel.INFO, "Deleting folder "+ workingFolder.toString());
                if (!deleteDirectory(workingFolder))
                    throw new GradleException("Could not delete working folder " + workingFolder);
            }
        });
        cmakeClean.setGroup("cmake");
        cmakeClean.setDescription("Clean CMake configuration");

        Task cmakeGenerators = project.task( "cmakeGenerators" ).doFirst(task -> {
            // should go to clean...
            ProcessBuilder pb = new ProcessBuilder(extension.getExecutable(), "--help");
            try {
                // make sure working folder exists
                extension.getWorkingFolder().mkdirs();

                // start
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                boolean foundGenerators = false;
                while ((line = reader.readLine()) != null) {
                    if ( line.equals("Generators") )
                        foundGenerators = true;
                    if ( foundGenerators )
                        project.getLogger().log(LogLevel.QUIET, line);
                }
                process.waitFor();
            }
            catch ( IOException e ) {
                throw new GradleScriptException( "cmake configuration failed.", e );
            }
            catch ( InterruptedException e ) {
                throw new GradleScriptException( "cmake configuration failed.", e );
            }

            File workingFolder = extension.getWorkingFolder().getAbsoluteFile();
            if ( workingFolder.exists() ) {
                project.getLogger().log(LogLevel.INFO, "Deleting folder "+ workingFolder.toString());
                if (!deleteDirectory(workingFolder))
                    throw new GradleException("Could not delete working folder " + workingFolder);
            }
        });
        cmakeGenerators.setGroup("cmake");
        cmakeGenerators.setDescription("List available CMake generators");

    }

}