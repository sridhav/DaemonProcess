/**
 * Copyright (c) 2015 The Advisory Board Company. All rights reserved.
 * This file is part of the Crimson Care Management platform.
 */

package com.advisory.PDIDaemon;

import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * The PdiDaemon runs as a daemon process and spawns new process in regular intervals
 * it has different command line options to fine tune the process
 * a new process is respawned only when the old process has completed till then it waits
 * for the process to complete.
 */
public class PdiDaemon {
    /**
     * default time is minutes to spawn a new process
     */
    public static final int SPAWN_NEW_PROCESS_TIME = 60;

    /**
     * default time in minutes to wait got process to complete
     */
    public static final int WAIT_FOR_PROCESS_TO_COMPLETE = 10;

    /**
     * default log file location and name
     */
    public static final String LOG_OUTPUT_FOLDER = "/tmp/logs";

    /**
     * daemon log file name
     */
    public static final String DAEMON_LOG_NAME = "pdi_auto_spawn.log";

    /**
     * command log file name
     */
    public static final String COMMAND_LOG_NAME = "pdi_command_output.log";

    /**
     * default command to be spawned each time
     */
    public static final String COMMAND_DEFAULT = "/opt/di/kitchen.sh -file=/opt/pdi/mirth_archiving_only.kjb";

    /**
     * default size factor k - KB, m-MB, g-GB
     */
    public static final String SIZE_FACTOR_DEFAULT = "k";

    /**
     * default time factor s-seconds, m-minutes, h-hours, d-days
     */
    public static final String TIME_FACTOR_DEFAULT = "s";

    /**
     * default log size limit which is multiplied by the size factor
     */
    public static final int LOG_LIMIT_DEFAULT = 10;

    /**
     * date format for log rotation
     */
    public static final String DATE_FORMAT = "yyyyMMddhhmmss";

    /**
     * Log Message for compressing log file
     */
    public static final String MESSAGE_COMPRESS = "\nCompressing file %s to %s in folder %s\n\n";

    /**
     * log Message for spawn wait time
     */
    public static final String MESSAGE_SPAWN_WAIT = "Waiting %d %s to spawn\n\n";

    /**
     * log message for message wait
     */
    public static final String MESSAGE_WAIT_FOR_PROCESS = "Waiting %d %s to complete previous process\n\n";

    /**
     * holds command to be executed
     */
    private String command;

    /**
     * stores log file name
     */
    private String logFolder;

    /**
     * stores the spawn time to wait for process to respawn
     */
    private int spawnTime;

    /**
     * stores waitTime to wait for process to complete
     */
    private int waitTime;

    /**
     * time factor to be multiplied with time to milliseconds
     */
    private int timeFactor;

    /**
     * size factor to be multiplied with size to bytes
     */
    private int sizeFactor;

    /**
     * max log limit size
     */
    private int logLimit;

    /**
     * time unit in words
     */
    private String timeUnit;

    /**
     * size unit in words
     */
    private String sizeUnit;

    /**
     * cpu limit value
     */
    private int cpuLimit = -1;

    /**
     * Constructor for the daemon process
     *
     * @param args - arguments to pass for the tool
     */
    public PdiDaemon(String[] args) {
        initDefaults();
        checkArguments(args);
        setOutputStream();
    }

    /**
     * Main stub Runs the daemon process
     * new process is respawned in regular intervals
     * spawning takes place only when the previous process is completed
     * If previous process is still running it waits until the process is completed
     *
     * @param args - command line args
     */
    public static void main(String args[]) {
        PdiDaemon pdi = new PdiDaemon(args);

        int spawnTime = pdi.getSpawnTime();
        int waitTime = pdi.getWaitTime();
        String command = pdi.getCommand();
        int cpuLimit = pdi.getCpuLimit();
        String fileName = pdi.getLogFolder() + File.separator + COMMAND_LOG_NAME;
        PdiRunnable run = null;
        if (cpuLimit > 0) {
            run = new PdiRunnable(command, fileName, cpuLimit);
        } else {
            run = new PdiRunnable(command, fileName);
        }
        try {
            do {
                pdi.logRotate();
                if (!run.isProcessExists()) {
                    run.run();
                    System.out.printf(MESSAGE_SPAWN_WAIT, spawnTime, pdi.getTimeUnit());
                    Thread.sleep(spawnTime * pdi.getTimeFactor());
                } else {
                    System.out.printf(MESSAGE_WAIT_FOR_PROCESS, waitTime, pdi.getTimeUnit());
                    Thread.sleep(waitTime * pdi.getTimeFactor());
                }
            } while (true);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * sets output stream to the log file
     */
    public void setOutputStream() {
        File logFile = new File(this.getLogFolder() + File.separator + DAEMON_LOG_NAME);
        if (!logFile.exists()) {
            File temp = new File(this.getLogFolder());
            temp.mkdirs();
        }
        PrintStream ps = null;
        try {
            ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)), true);
            System.setOut(ps);
            System.setErr(ps);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * initializes default values
     */
    private void initDefaults() {
        this.setSizeFactor(SIZE_FACTOR_DEFAULT);
        this.setTimeFactor(TIME_FACTOR_DEFAULT);
        this.setSpawnTime(SPAWN_NEW_PROCESS_TIME);
        this.setWaitTime(WAIT_FOR_PROCESS_TO_COMPLETE);
        this.setLogFolder(LOG_OUTPUT_FOLDER);
        this.setCommand(COMMAND_DEFAULT);
        this.setLogLimit(LOG_LIMIT_DEFAULT);

    }

    /**
     * checks arguments and then parses them
     *
     * @param args - arguments to be parsed
     */
    private void checkArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            int val = parseArgument(args, i);
            if (val != -1) {
                i = val;
            }
        }
    }

    /**
     * parses the argument and returns the final index of parsed arguments
     *
     * @param args  - arguments to be parsed
     * @param index - index
     * @return index of arguments that are parsed
     */
    private int parseArgument(String[] args, int index) {
        String argument = args[index];
        if (argument.charAt(0) == '-') {
            char argOption = argument.charAt(1);
            switch (argOption) {
                case 's':
                    int spawnTime = Integer.parseInt(args[index + 1]);
                    this.setSpawnTime(spawnTime);
                    return index + 1;
                case 'w':
                    int waitTime = Integer.parseInt(args[index + 1]);
                    this.setWaitTime(waitTime);
                    return index + 1;
                case 'l':
                    String logFolder = args[index + 1];
                    this.setLogFolder(logFolder);
                    return index + 1;
                case 'c':
                    String command = args[index + 1];
                    this.setCommand(command);
                    return index + 1;
                case 't':
                    String timeFactor = args[index + 1];
                    this.setTimeFactor(timeFactor);
                    return index + 1;
                case 'k':
                    String sizeFactor = args[index + 1];
                    this.setSizeFactor(sizeFactor);
                    return index + 1;
                case 'm':
                    int logLimit = Integer.parseInt(args[index + 1]);
                    this.setLogLimit(logLimit);
                    return index + 1;
                case 'h':
                    this.printHelp();
                    System.exit(1);
                    break;
                case 'p':
                    int cpuLimit = Integer.parseInt(args[index + 1]);
                    this.setCpuLimit(cpuLimit);
                    return index+1;
                default:
                    System.out.println("Invalid option " + args[index]);
                    this.printHelp();
                    System.exit(1);
            }
        } else {
            System.exit(1);
        }

        return -1;
    }

    /**
     * Command line help
     */
    protected void printHelp() {
        System.out.println(
                "-s => set spawn time to respawn new process\n" +
                        "-w => set wait time to wait till process is complete\n" +
                        "-l => set log file name \n" +
                        "-c => set command that needs to be respawned every time\n" +
                        "-t => set time factor (s-seconds, m-minutes, h-hours, d-days)\n" +
                        "-k => set size factor (k-KB, m-MB, g-GB)\n" +
                        "-m => set log rotate size limit \n" +
                        "-p => set cpu limit value for process \n" +
                        "-h => print help"
        );
    }

    /**
     * @return cpu limit value
     */
    private int getCpuLimit() {
        return this.cpuLimit;
    }

    /**
     * @param limit cpu limit value
     */
    private void setCpuLimit(int limit) {
        this.cpuLimit = limit;
    }

    /**
     * @return max log limit
     */
    protected int getLogLimit() {
        return this.logLimit;
    }

    /**
     * @param logLimit max log limit to set
     */
    protected void setLogLimit(int logLimit) {
        this.logLimit = logLimit;
    }

    /**
     * @return spawn interval
     */
    protected int getSpawnTime() {
        return this.spawnTime;
    }

    /**
     * @param spawnTime spawn interval
     */
    protected void setSpawnTime(int spawnTime) {
        this.spawnTime = spawnTime;
    }

    /**
     * @return wait interval
     */
    protected int getWaitTime() {
        return this.waitTime;
    }

    /**
     * @param waitTime wait interval
     */
    protected void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * @return log file name
     */
    protected String getLogFolder() {
        return this.logFolder;
    }

    /**
     * @param logFolder set log file name
     */
    protected void setLogFolder(String logFolder) {
        this.logFolder = logFolder;
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

    /**
     * @return sizeUnit in words
     */
    protected String getSizeUnit() {
        return this.sizeUnit;
    }

    /**
     * @param sizeUnit set sizeUnit value
     */
    protected void setSizeUnit(String sizeUnit) {
        this.sizeUnit = sizeUnit;
    }

    /**
     * @return timeUnit in words
     */
    protected String getTimeUnit() {
        return this.timeUnit;
    }

    /**
     * @param timeUnit sets time unig
     */
    protected void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * @return sizefactor value in bytes
     */
    protected int getSizeFactor() {
        return this.sizeFactor;
    }

    /**
     * @param size convert size factor string to bytes
     */
    protected void setSizeFactor(String size) {
        int sizeFactor = 1;
        String sizeUnit = null;
        switch (size) {
            case "k":
                sizeFactor = sizeFactor * 1024;
                sizeUnit = "KB";
                break;
            case "m":
                sizeFactor = sizeFactor * 1024 * 1024;
                sizeUnit = "MB";
                break;
            case "g":
                sizeFactor = sizeFactor * 1024 * 1024 * 1024;
                sizeUnit = "GB";
                break;
        }
        this.setSizeUnit(sizeUnit);
        this.sizeFactor = sizeFactor;
    }

    /**
     * @return time factor value in milli seconds
     */
    protected int getTimeFactor() {
        return this.timeFactor;
    }

    /**
     * @param time convert time factor string to milli seconds
     */
    protected void setTimeFactor(String time) {
        int timeFactor = 1;
        String timeUnit = null;
        switch (time) {
            case "d":
                timeFactor = timeFactor * 24 * 60 * 60 * 1000;
                timeUnit = "days";
                break;
            case "h":
                timeFactor = timeFactor * 60 * 60 * 1000;
                timeUnit = "hours";
                break;
            case "m":
                timeFactor = timeFactor * 60 * 1000;
                timeUnit = "minutes";
                break;
            case "s":
                timeFactor = timeFactor * 1000;
                timeUnit = "seconds";
                break;
        }
        this.setTimeUnit(timeUnit);
        this.timeFactor = timeFactor;
    }

    /**
     * Rotates the log to a new file when log reaches a max size
     */
    protected void logRotate() {
        File logFile = new File(this.getLogFolder() + File.separator + DAEMON_LOG_NAME);
        int logFileMaxLimit = this.getLogLimit() * this.getSizeFactor();
        if (logFile.length() > logFileMaxLimit) {
            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            Date date = new Date();
            String today = dateFormat.format(date);
            String compressedLogFileName = logFile.getPath() + today;
            File compressedLogFile = new File(compressedLogFileName);
            System.out.printf(MESSAGE_COMPRESS, logFile.getName(), compressedLogFileName, logFile.getParent());
            logFile.renameTo(compressedLogFile);
            setOutputStream();
        }
    }
}
