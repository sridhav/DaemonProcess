/**
 * Copyright (c) 2015 The Advisory Board Company. All rights reserved.
 * This file is part of the Crimson Care Management platform.
 */

package com.advisory.PDIDaemon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

public class PdiLogWriter {
    /**
     * FileWriter for log writing
     */
    private FileWriter logWriter = null;

    /**
     * log file name
     */
    private String logFileName = null;

    /**
     * Constructor
     *
     * @param logFileName filename of log
     */
    public PdiLogWriter(String logFileName) {
        this.setLogFileName(logFileName);
        createLogWriter();
    }

    /**
     * creates logwriter uses filewriter class
     */
    private void createLogWriter() {
        String fileName = this.getLogFileName();
        File logFile = new File(fileName);
        if (fileName != null) {
            File parentFile = new File(logFile.getParent());
            if (!parentFile.exists()) {
                logFile.mkdirs();
            }
        }
        try {
            FileWriter logWriter = new FileWriter(logFile, true);
            this.setFileWriter(logWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * writes to the log file
     *
     * @param log log data
     */
    protected void writeLog(String log) {
        FileWriter logWriter = this.getFileWriter();
        try {
            if (logWriter != null) {
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date.getTime());
                logWriter.write(timestamp + " " + log);
                logWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param logWriter sets filewriter class
     */
    private void setFileWriter(FileWriter logWriter) {
        this.logWriter = logWriter;
    }

    /**
     * @return filewriter
     */
    private FileWriter getFileWriter() {
        return this.logWriter;
    }

    /**
     * @param logFileName log file name
     */
    private void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    /**
     * @return log file name
     */
    private String getLogFileName() {
        return this.logFileName;
    }
}

