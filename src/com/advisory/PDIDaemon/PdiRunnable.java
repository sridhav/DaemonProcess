/**
 * Copyright (c) 2015 The Advisory Board Company. All rights reserved.
 * This file is part of the Crimson Care Management platform.
 */

package com.advisory.PDIDaemon;

import java.io.*;
import java.lang.reflect.Field;

public class PdiRunnable {
    /**
     * Log Message for pid already exists
     */
    public static final String MESSAGE_PID_EXISTS = "Process with PID #%d already exists\n";
    /**
     * log message got starting a process
     */
    public static final String MESSAGE_PROCESS_STARTED = "Started running a process with PID #%d\n";
    /**
     * command that needs to run in regular intervals
     */
    private String commandToRun = null;
    /**
     * pid of the process
     */
    private int pid = -1;

    /**
     * limit cpu value
     */
    private int limit = -1;

    /**
     * log file to store command output
     */
    private String logFile;

    /**
     * logwriter for autospawn logs
     */
    private PdiLogWriter logWriter = null;

    /**
     * constructor for the runnable process
     *
     * @param command command to be executed
     */
    public PdiRunnable(String command, String logFile) {
        this.setCommand(command);
        this.setLogFile(logFile);
    }

    /**
     * runs the command and stores the pid for the process that is executed
     */
    public void run() {
        String command = this.getCommand();
        String logFile = this.getLogFile();
        ProcessCreator pc = new ProcessCreator(command, logFile);
        int pid = pc.execute();
        this.setPid(pid);
        String log = String.format(MESSAGE_PROCESS_STARTED, pid);
        this.getLogWriter().writeLog(log);
    }

    /**
     * checks whether process exists or not
     *
     * @return process exists or not
     */
    public boolean isProcessExists() {
        int pid = this.getPid();
        try {
            if(pid >0) {
                String cmd = "ps -ef | awk '{printf \"%s\\n\",$2;}' | grep " + pid;
                Process process = Runtime.getRuntime().exec(cmd);
                InputStream is = process.getInputStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(is));
                String temp = null;
                while ((temp = bf.readLine()) != null) {
                    int returnedPid = Integer.parseInt(temp);
                    if (returnedPid == pid && pid > 0) {
                        String log = String.format(MESSAGE_PID_EXISTS, pid);
                        this.getLogWriter().writeLog(log);
                        return true;
                    }
                }
                bf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * @param command sets command to be executed
     */
    protected void setCommand(String command) {
        this.commandToRun = command;
    }

    /**
     * @return command to be executed
     */
    protected String getCommand() {
        return this.commandToRun;
    }

    /**
     * @return pid
     */
    private int getPid() {
        return this.pid;
    }

    /**
     * @param pid sets pid
     */
    private void setPid(int pid) {
        this.pid = pid;
    }

    /**
     * @param logFile log file to store command output
     */
    private void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    /**
     * @return log file to store command output
     */
    private String getLogFile() {
        return logFile;
    }

    /**
     * @param logWriter sets PdilogWriter
     */
    protected void setLogWriter(PdiLogWriter logWriter) {
        this.logWriter = logWriter;
    }

    /**
     * @return logwriter for autospawn data
     */
    protected PdiLogWriter getLogWriter() {
        return this.logWriter;
    }
}

