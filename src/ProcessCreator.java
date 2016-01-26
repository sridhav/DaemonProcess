import java.io.IOException;
import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by vemulas on 1/25/16.
 */
public class ProcessCreator {

    /**
     * holds command to be executed
     */
    private String command;

    /**
     * log file to write the output
     */
    private String logFile = "/tmp/logs/output.log";

    private Process process = null;

    public ProcessCreator(String command) {
        this.setCommand(command);
    }

    public ProcessCreator(String command, String logFileName) {
        this.setCommand(command);
        this.setLogFile(logFileName);
    }

    public int execute() {
        String command = this.getCommand();
        ProcessBuilder pb = new ProcessBuilder();
        try {
            pb.command(command);
            pb.redirectOutput(new File(this.getLogFile()));
            pb.directory(new File("/opt/di"));
            Process process = pb.start();
            this.setProcess(process);
            return this.getPidFromProcess(process);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }


    /**
     * returns the pid from the process
     *
     * @param process where the pid needs to be extracted
     * @return pid of the process
     */
    private int getPidFromProcess(Process process) {
        try {
            Field f = process.getClass().getDeclaredField("pid" );
            f.setAccessible(true);
            int pid = f.getInt(process);
            return pid;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * @return command to be executed
     */
    protected String getCommand() {
        return this.command;
    }

    /**
     * @param command set command to be executed
     */
    protected void setCommand(String command) {
        this.command = command;
    }

    private void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    private String getLogFile() {
        return logFile;
    }

    protected void setProcess(Process process) {
        this.process = process;
    }

    protected Process getProcess() {
        return this.process;
    }
}