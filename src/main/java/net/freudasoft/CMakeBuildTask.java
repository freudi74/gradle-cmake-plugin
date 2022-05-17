package net.freudasoft;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import java.util.ArrayList;

/**
 * @author Marco 'freudi74' Freudenberger
 * @author Alexander 'KitsuneAlex' Hinze
 * @since 28/05/2019
 */
public class CMakeBuildTask extends AbstractCMakeTask {
    private final Property<String> executable;
    private final Property<String> buildConfig;
    private final Property<String> buildTarget;
    private final Property<Boolean> buildClean;

    public CMakeBuildTask() {
        setGroup("cmake");
        setDescription("Build a configured Build with CMake");
        executable = getProject().getObjects().property(String.class);
        buildConfig = getProject().getObjects().property(String.class);
        buildTarget = getProject().getObjects().property(String.class);
        buildClean = getProject().getObjects().property(Boolean.class);
    }

    @Override
    protected void copyConfiguration(CMakePluginExtension ext) {
        executable.set(ext.getExecutable());
        buildConfig.set(ext.getBuildConfig());
        buildTarget.set(ext.getBuildTarget());
        buildClean.set(ext.getBuildClean());
    }

    @Override
    protected void gatherParameters(ArrayList<String> params) {

    }

    @Input
    @Optional
    public Property<String> getExecutable() {
        return executable;
    }

    @Input
    @Optional
    public Property<String> getBuildConfig() {
        return buildConfig;
    }

    @Input
    @Optional
    public Property<String> getBuildTarget() {
        return buildTarget;
    }

    @Input
    @Optional
    public Property<Boolean> getBuildClean() {
        return buildClean;
    }

    //private List<String> buildCmdLine() {
    //    List<String> parameters = new ArrayList<>();
    //
    //    parameters.add(executable.getOrElse("cmake"));
    //    parameters.add("--build");
    //    parameters.add("." ); // working folder will be executable working dir --- workingFolder.getAsFile().get().getAbsolutePath()
    //
    //    if ( ! buildConfig.isPresent() ) {
    //        parameters.add("--config");
    //        parameters.add(buildConfig.get());
    //    }
    //
    //    if ( ! buildTarget.isPresent() ) {
    //        parameters.add("--target");
    //        parameters.add(buildTarget.get());
    //    }
    //
    //    if ( buildClean.getOrElse(false) )
    //        parameters.add( "--clean-first" );
    //
    //    return parameters;
    //}
}
