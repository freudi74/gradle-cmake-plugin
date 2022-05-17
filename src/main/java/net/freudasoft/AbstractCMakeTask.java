package net.freudasoft;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;

/**
 * Base class that makes sure the CMake build is always invoked
 * with the optimal number of threads. This can immensely speed
 * up build times on heavy multi-core systems.
 *
 * @author Alexander 'KitsuneAlex' Hinze
 * @since 17/05/2022
 */
public abstract class AbstractCMakeTask extends DefaultTask {
    private static final int numHostThreads = Runtime.getRuntime().availableProcessors();
    private static final int numUsableThreads = Math.max(2, numHostThreads - 2);
    protected final Property<String> generator; // for example: "Visual Studio 16 2019"
    private final DirectoryProperty workingFolder;

    protected AbstractCMakeTask() {
        workingFolder = getProject().getObjects().directoryProperty();
        workingFolder.set(new File(getProject().getBuildDir(), "cmake"));
        generator = getProject().getObjects().property(String.class);
    }

    protected abstract void gatherParameters(ArrayList<String> params);

    protected abstract void copyConfiguration(CMakePluginExtension ext);

    public void configureFromProject() {
        final CMakePluginExtension ext = (CMakePluginExtension) getProject().getExtensions().getByName("cmake");
        workingFolder.set(ext.getWorkingFolder());
        generator.set(ext.getGenerator());
        copyConfiguration(ext);
    }

    private ArrayList<String> buildCmdLine(String generator) {
        final ArrayList<String> params = new ArrayList<>();
        gatherParameters(params);

        if (generator.contains("Visual Studio")) {
            params.add("-- /MP"); // Automatically grabs the right amount of threads
        }
        else if (generator.contains("MinGW Makefiles")) {
            params.add(String.format("-- -j %d", numUsableThreads));
        }
        else if (generator.contains("Unix Makefiles")) {
            params.add(String.format("-- -j %d", numUsableThreads));
        }

        return params;
    }

    @OutputDirectory
    public DirectoryProperty getWorkingFolder() {
        return workingFolder;
    }

    @TaskAction
    public void performAction() {
        final String generator = this.generator.getOrNull();

        if (generator == null) {
            throw new IllegalStateException("Missing generator definition");
        }

        new CMakeExecutor(getLogger(), getName()).exec(buildCmdLine(generator), workingFolder.getAsFile().get());
    }
}
