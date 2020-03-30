package nl.thanus.reboot.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static nl.thanus.reboot.ReBootKt.main;

@Mojo(name = "reboot")
public class ReBootMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", property = "directory", required = true)
    private File directory;

    @Parameter(property = "excluded")
    private List<String> excluded;

    @Override
    public void execute() {
        List<String> args = new ArrayList<>();
        args.add(directory.getAbsolutePath());
        if(excluded != null && excluded.size() >= 1) {
            for(String refactoring : excluded) {
                args.add("-e");
                args.add(refactoring);
            }
        }
        main(args.toArray(new String[0]));
    }
}
