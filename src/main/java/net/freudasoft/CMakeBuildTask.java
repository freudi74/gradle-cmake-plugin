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
    private static final int numHostThreads = Runtime.getRuntime().availableProcessors();
    /**
     * Find the optimal number of threads to use for the build;
     * We usually aim for n-2 threads, where n is the number of logical host threads,
     * but since some systems (especially VMs for CI/CDs etc.) might have 2 or less threads,
     * we define the optimal number of threads as max(min(n, 2), n - 2), which gives us
     * the results we are looking for.
     * TODO: maybe make this configurable at some point..
     */
    private static final int numUsableThreads = Math.max(Math.min(numHostThreads, 2), numHostThreads - 2);

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
        params.add("--build");
        params.add("."); // working folder will be executable working dir --- workingFolder.getAsFile().get().getAbsolutePath()

        if (buildConfig.isPresent()) {
            params.add("--config");
            params.add(buildConfig.get());
        }

        if (buildTarget.isPresent()) {
            params.add("--target");
            params.add(buildTarget.get());
        }

        if (buildClean.getOrElse(false)) {
            params.add("--clean-first");
        }
    }

    @Override
    protected void gatherBuildParameters(ArrayList<String> params) {
        final String gen = generator.getOrNull();

        if(gen != null) {
            if(gen.equals("Unix Makefiles") || gen.equals("MinGW Makefiles")) {
                params.add("-j");
                params.add(Integer.toString(numUsableThreads));
            }
            else if(gen.contains("Visual Studio")) {
                params.add("/MP"); // Automatically grabs the right # of threads :)
            }
        }
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
}
