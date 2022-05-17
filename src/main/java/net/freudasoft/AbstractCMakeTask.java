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
    protected abstract void gatherBuildParameters(ArrayList<String> params);
    protected abstract void copyConfiguration(CMakePluginExtension ext);

    public void configureFromProject() {
        final CMakePluginExtension ext = (CMakePluginExtension) getProject().getExtensions().getByName("cmake");
        workingFolder.set(ext.getWorkingFolder());
        generator.set(ext.getGenerator());
        executable.set(ext.getExecutable());
        sourceFolder.set(ext.getSourceFolder());
        copyConfiguration(ext);
    }

    private ArrayList<String> buildCmdLine() {
        final ArrayList<String> params = new ArrayList<>();

        params.add(executable.getOrElse("cmake"));
        gatherParameters(params);

        final ArrayList<String> buildParams = new ArrayList<>();
        gatherBuildParameters(buildParams);

        if(!buildParams.isEmpty()) {
            params.add("--");
            params.addAll(buildParams);
        }

        return params;
    }

    @Input
    @Optional
    public Property<String> getGenerator() {
        return generator;
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
        new CMakeExecutor(getLogger(), getName()).exec(buildCmdLine(), workingFolder.getAsFile().get());
    }
}
