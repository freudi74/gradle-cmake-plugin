package net.freudasoft;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
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
    /**
     * Find the optimal number of threads to use for the build;
     * We usually aim for n-2 threads, where n is the number of logical host threads,
     * but since some systems (especially VMs for CI/CDs etc.) might have 2 or less threads,
     * we define the optimal number of threads as max(min(n, 2), n - 2), which gives us
     * the results we are looking for.
     * TODO: maybe make this configurable at some point..
     */
    private static final int numUsableThreads = Math.max(Math.min(numHostThreads, 2), numHostThreads - 2);

    protected final Property<String> executable;
    protected final DirectoryProperty sourceFolder;
    protected final Property<String> generator; // for example: "Visual Studio 16 2019"
    protected final DirectoryProperty workingFolder;

    protected AbstractCMakeTask() {
        workingFolder = getProject().getObjects().directoryProperty();
        workingFolder.set(new File(getProject().getBuildDir(), "cmake"));
        generator = getProject().getObjects().property(String.class);
        executable = getProject().getObjects().property(String.class);
        sourceFolder = getProject().getObjects().directoryProperty();
        sourceFolder.set(new File(getProject().getBuildDir(), "src" + File.separator + "main" + File.separator + "cpp"));
    }

    protected abstract void gatherParameters(ArrayList<String> params);

    protected abstract void copyConfiguration(CMakePluginExtension ext);

    public void configureFromProject() {
        final CMakePluginExtension ext = (CMakePluginExtension) getProject().getExtensions().getByName("cmake");
        workingFolder.set(ext.getWorkingFolder());
        generator.set(ext.getGenerator());
        executable.set(ext.getExecutable());
        sourceFolder.set(ext.getSourceFolder());
        copyConfiguration(ext);
    }

    private ArrayList<String> buildCmdLine(String generator) {
        final ArrayList<String> params = new ArrayList<>();

        params.add(executable.getOrElse("cmake"));
        gatherParameters(params);
        params.add(sourceFolder.getAsFile().get().getAbsolutePath());

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

    @Input
    @Optional
    public Property<String> getExecutable() {
        return executable;
    }

    @InputDirectory
    public DirectoryProperty getSourceFolder() {
        return sourceFolder;
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
