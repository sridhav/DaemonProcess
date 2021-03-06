/**
 * Copyright (c) 2015 The Advisory Board Company. All rights reserved.
 * This file is part of the Crimson Care Management platform.
 */

package com.advisory.PDIDaemon;

import java.io.IOException;
import java.io.File;
import java.lang.reflect.Field;

public class ProcessCreator {

    /**
     * holds command to be executed
     */
    private String command;

    /**
     * log file to write the output
     */
    private String logFile = "/tmp/logs/output.log";

    /**
     * process that has been dispatched
     */
    private Process process = null;

    /**
     * @param command command to be executed
     */
    public ProcessCreator(String command) {
        this.setCommand(command);
    }

    /**
     * @param command     command to be executed
     * @param logFileName log output for command to this file
     */
    public ProcessCreator(String command, String logFileName) {
        this.setCommand(command);
        this.setLogFile(logFileName);
    }

    /**
     * executes the process
     *
     * @return pid fo the process
     */
    public int execute() {
        String command = this.getCommand();
        ProcessBuilder pb = new ProcessBuilder();
        try {
            pb.command("/bin/sh", "-c", command);
            File logFile = new File(this.getLogFile());
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            pb.redirectOutput(logFile);
            pb.redirectError(logFile);
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
            Field f = process.getClass().getDeclaredField("pid");
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

    /**
     * @param logFile command log file name
     */
    private void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    /**
     * @return command log file name
     */
    private String getLogFile() {
        return logFile;
    }

    /**
     * @param process executed command process
     */
    protected void setProcess(Process process) {
        this.process = process;
    }

    /**
     * @return executed command process
     */
    protected Process getProcess() {
        return this.process;
    }
}