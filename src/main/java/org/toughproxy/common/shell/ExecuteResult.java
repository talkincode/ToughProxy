package org.toughproxy.common.shell;

public class ExecuteResult {
    private int exitCode;
    private String executeOut;

    public ExecuteResult(int exitCode, String executeOut) {
        this.exitCode = exitCode;
        this.executeOut = executeOut;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getExecuteOut() {
        return executeOut;
    }

    public void setExecuteOut(String executeOut) {
        this.executeOut = executeOut;
    }

    @Override
    public String toString() {
        return "ExecuteResult{" +
                "exitCode=" + exitCode +
                ", executeOut='" + executeOut + '\'' +
                '}';
    }
}