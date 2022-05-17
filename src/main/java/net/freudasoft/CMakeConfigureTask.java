package net.freudasoft;

import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Marco 'freudi74' Freudenberger
 * @author Alexander 'KitsuneAlex' Hinze
 * @since 28/05/2019
 */
public class CMakeConfigureTask extends AbstractCMakeTask {
    private final Property<String> configurationTypes;
    private final Property<String> installPrefix;
    private final Property<String> platform; // for example "x64" or "Win32" or "ARM" or "ARM64", supported on vs > 8.0
    private final Property<String> toolset; // for example "v142", supported on vs > 10.0
    private final Property<Boolean> buildSharedLibs;
    private final Property<Boolean> buildStaticLibs;
    private final MapProperty<String, String> def;

    public CMakeConfigureTask() {
        setGroup("cmake");
        setDescription("Configure a Build with CMake");
        configurationTypes = getProject().getObjects().property(String.class);
        installPrefix = getProject().getObjects().property(String.class);

        platform = getProject().getObjects().property(String.class);
        toolset = getProject().getObjects().property(String.class);
        buildSharedLibs = getProject().getObjects().property(Boolean.class);
        buildStaticLibs = getProject().getObjects().property(Boolean.class);
        def = getProject().getObjects().mapProperty(String.class, String.class);
    }

    @Override
    protected void gatherParameters(ArrayList<String> params) {
        if (this.generator.isPresent() && !this.generator.get().isEmpty()) {
            params.add("-G");
            params.add(this.generator.get());
        }

        if (platform.isPresent() && !platform.get().isEmpty()) {
            params.add("-A");
            params.add(platform.get());
        }

        if (toolset.isPresent() && !toolset.get().isEmpty()) {
            params.add("-T");
            params.add(toolset.get());
        }

        if (configurationTypes.isPresent() && !configurationTypes.get().isEmpty())
            params.add("-DCMAKE_CONFIGURATION_TYPES=" + configurationTypes.get());

        if (installPrefix.isPresent() && !installPrefix.get().isEmpty())
            params.add("-DCMAKE_INSTALL_PREFIX=" + installPrefix.get());


        if (buildSharedLibs.isPresent())
            params.add("-DBUILD_SHARED_LIBS=" + (buildSharedLibs.get() ? "ON" : "OFF"));

        if (buildStaticLibs.isPresent())
            params.add("-DBUILD_STATIC_LIBS=" + (buildStaticLibs.get() ? "ON" : "OFF"));


        if (def.isPresent()) {
            for (final Map.Entry<String, String> entry : def.get().entrySet()) {
                params.add("-D" + entry.getKey() + "=" + entry.getValue());
            }
        }

        params.add(sourceFolder.getAsFile().get().getAbsolutePath());
    }

    @Override
    protected void gatherBuildParameters(ArrayList<String> params) {

    }

    @Override
    protected void copyConfiguration(CMakePluginExtension ext) {
        configurationTypes.set(ext.getConfigurationTypes());
        installPrefix.set(ext.getInstallPrefix());
        platform.set(ext.getPlatform());
        toolset.set(ext.getToolset());
        buildSharedLibs.set(ext.getBuildSharedLibs());
        buildStaticLibs.set(ext.getBuildStaticLibs());
        def.set(ext.getDef());
    }

    @Input
    @Optional
    public Property<String> getConfigurationTypes() {
        return configurationTypes;
    }

    @Input
    @Optional
    public Property<String> getInstallPrefix() {
        return installPrefix;
    }

    @Input
    @Optional
    public Property<String> getPlatform() {
        return platform;
    }

    @Input
    @Optional
    public Property<String> getToolset() {
        return toolset;
    }

    @Input
    @Optional
    public Property<Boolean> getBuildSharedLibs() {
        return buildSharedLibs;
    }

    @Input
    @Optional
    public Property<Boolean> getBuildStaticLibs() {
        return buildStaticLibs;
    }

    @Input
    @Optional
    public MapProperty<String, String> getDef() {
        return def;
    }
}
