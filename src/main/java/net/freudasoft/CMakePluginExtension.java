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

import java.io.File;
import java.util.*;

public class CMakePluginExtension {
    private String executable = "cmake";
    private File workingFolder;
    private File sourceFolder;
    private String configurationTypes = "";
    private String installPrefix = "";
    private String generator = ""; // for example: "Visual Studio 16 2019"
    private String platform = ""; // for example "x64" or "Win32" or "ARM" or "ARM64", supported on vs > 8.0
    private String toolset = ""; // for example "v142", supported on vs > 10.0
    private Optional<Boolean> buildSharedLibs = Optional.empty();
    private Optional<Boolean> buildStaticLibs = Optional.empty();
    private Map<String,String> def = new HashMap<>();

    // parameters used on build step
    private String buildConfig = "";
    private String buildTarget = "";
    private boolean buildClean = false;

    public CMakePluginExtension( File workingFolder, File sourceFolder ) {
        this.workingFolder = workingFolder;
        this.sourceFolder = sourceFolder;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public File getWorkingFolder() {
        return workingFolder;
    }

    public void setWorkingFolder(File workingFolder) {
        this.workingFolder = workingFolder;
    }

    public File getSourceFolder() {
        return sourceFolder;
    }

    public void setSourceFolder(File sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public String getConfigurationTypes() {
        return configurationTypes;
    }

    public void setConfigurationTypes(String configurationTypes) {
        this.configurationTypes = configurationTypes;
    }

    public String getInstallPrefix() {
        return installPrefix;
    }

    public void setInstallPrefix(String installPrefix) {
        this.installPrefix = installPrefix;
    }

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getToolset() {
        return toolset;
    }

    public void setToolset(String toolset) {
        this.toolset = toolset;
    }

    public Optional<Boolean> getBuildSharedLibs() {
        return buildSharedLibs;
    }

    public void setBuildSharedLibs(boolean buildSharedLibs) {
        this.buildSharedLibs = Optional.of(buildSharedLibs);
    }

    public Optional<Boolean> getBuildStaticLibs() {
        return buildStaticLibs;
    }

    public void setBuildStaticLibs(boolean buildStaticLibs) {
        this.buildStaticLibs = Optional.of(buildStaticLibs);
    }

    public void setDef(Map<String,String> def) {
        this.def = def;
    }
    public Map<String,String> getDef() {
        return def;
    }

    public String getBuildConfig() {
        return buildConfig;
    }

    public void setBuildConfig(String buildConfig) {
        this.buildConfig = buildConfig;
    }

    public String getBuildTarget() {
        return buildTarget;
    }

    public void setBuildTarget(String buildTarget) {
        this.buildTarget = buildTarget;
    }

    public boolean isBuildClean() {
        return buildClean;
    }

    public void setBuildClean(boolean buildClean) {
        this.buildClean = buildClean;
    }


    List<String> buildConfigCommandLineParameters() {
        List<String> parameters = new ArrayList<>();

        parameters.add(executable);

        if ( ! generator.isEmpty() ) {
            parameters.add("-G");
            parameters.add(generator);
        }

        if (! platform.isEmpty() ) {
            parameters.add("-A");
            parameters.add(platform);
        }

        if (! toolset.isEmpty() ) {
            parameters.add("-T");
            parameters.add(toolset);
        }

        if (! configurationTypes.isEmpty() ) {
            parameters.add("-DCMAKE_CONFIGURATION_TYPES="+ configurationTypes);
        }

        if (! installPrefix.isEmpty() )
            parameters.add("-DCMAKE_INSTALL_PREFIX="+installPrefix);


        if ( buildSharedLibs.isPresent() ) {
            parameters.add("-DBUILD_SHARED_LIBS=" + (buildSharedLibs.get().booleanValue() ? "ON" : "OFF") );
        }
        if ( buildStaticLibs.isPresent() ) {
            parameters.add("-DBUILD_STATIC_LIBS=" + (buildStaticLibs.get().booleanValue() ? "ON" : "OFF") );
        }

        for ( Map.Entry<String,String> entry : def.entrySet() )
            parameters.add("-D"+entry.getKey()+"="+entry.getValue());

        parameters.add( sourceFolder.getAbsolutePath() );

        return parameters;
    }

    List<String> buildBuildCommandLineParameters() {
        List<String> parameters = new ArrayList<>();

        parameters.add(executable);
        parameters.add("--build");
        parameters.add( workingFolder.getAbsolutePath() );

        if ( ! buildConfig.isEmpty() ) {
            parameters.add("--config");
            parameters.add(buildConfig);
        }

        if ( ! buildTarget.isEmpty() ) {
            parameters.add("--target");
            parameters.add(buildTarget);
        }

        if ( buildClean )
            parameters.add( "--clean-first" );

        return parameters;
    }


}