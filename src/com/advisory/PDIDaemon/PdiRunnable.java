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
     * cpulimit command
     */
    public static final String CPULIMIT_COMMAND = "cpulimit -z -i -l %d -p %d &";
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
     *
     */
    private String logFile;

    /**
     * constructor for the runnable process
     *
     * @param command command to be executed
     */
    public PdiRunnable(String command, String logFile) {
        this.setCommand(command);
        this.setLogFile(logFile);
    }

    public PdiRunnable(String command, String logFile, int limit) {
        this.setCommand(command);
        this.setLimit(limit);
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
            int limit = this.getLimit();
            if (limit > 0) {
                String cpuLimitCommand = String.format(CPULIMIT_COMMAND, limit, pid);
                pc = new ProcessCreator(cpuLimitCommand);
                pc.execute();
            }
           /** InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            FileWriter fw = new FileWriter(this.getLogFile(), true);
            String temp = null;
            while((temp = br.readLine()) != null) {
                fw.write(temp+"\n");
            }
            fw.close();
*/
            System.out.printf(MESSAGE_PROCESS_STARTED, pid);

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
                String command[] = this.getCmdArgs(cmd);
                Process process = Runtime.getRuntime().exec(command);
                InputStream is = process.getInputStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(is));
                String temp = null;
                while ((temp = bf.readLine()) != null) {
                    int returnedPid = Integer.parseInt(temp);
                    if (returnedPid == pid && pid > 0) {
                        System.out.printf(MESSAGE_PID_EXISTS, pid);
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
            this.setPid(pid);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return pid;
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
     * @return cpu limit value
     */
    private int getLimit() {
        return this.limit;
    }

    /**
     * @param limit cpu limit value
     */
    private void setLimit(int limit) {
        this.limit = limit;
    }

    private void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    private String getLogFile() {
        return logFile;
    }

    private String[] getCmdArgs(String command) {
        String[] cmdArgs = {"/bin/sh", "-c", command};

        return cmdArgs;
    }
}

