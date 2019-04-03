package net.freudasoft;

import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CMakeExecutor {
    private Logger logger;
    private String taskName;

    CMakeExecutor( Logger logger, String taskName ) {
        this.logger = logger;
        this.taskName = taskName;
    }

    protected void exec(List<String> cmdLine, File workingFolder) throws GradleException {
        // log command line parameters
        StringBuilder sb = new StringBuilder("  CMakePlugin.task "+taskName+" - exec: ");
        for ( String s : cmdLine ) {
            sb.append(s).append(" ");
        }
        logger.info(sb.toString());

        // build process
        ProcessBuilder pb = new ProcessBuilder(cmdLine);
        pb.directory( workingFolder );


        try {
            // make sure working folder exists
            workingFolder.mkdirs();

            // start
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info( line );
            }
            if ( null != (line = errorReader.readLine()) ) {
                logger.error( "  CMakePlugin.cmakeConfigure - ERRORS: " );
                do {
                    logger.error(line);
                } while ((line = errorReader.readLine()) != null);
            }


            int retCode = process.waitFor();
            if ( retCode != 0 )
                throw new GradleException("["+taskName+"]Error: CMAKE returned "+retCode );
        }
        catch ( IOException e ) {
            throw new GradleScriptException( "CMakeExecutor["+taskName+"].", e );
        }
        catch ( InterruptedException e ) {
            throw new GradleScriptException( "CMakeExecutor["+taskName+"].", e );
        }
    }


}

