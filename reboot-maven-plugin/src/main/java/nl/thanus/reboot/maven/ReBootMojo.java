package nl.thanus.reboot.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

import static nl.thanus.reboot.ReBootKt.main;

@Mojo(name = "reboot")
public class ReBootMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", property = "directory", required = true)
    private File directory;

    @Override
    public void execute() {
        final String[] args = {directory.getAbsolutePath()};
        main(args);
    }
}
