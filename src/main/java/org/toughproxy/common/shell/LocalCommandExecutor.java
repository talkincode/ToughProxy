package org.toughproxy.common.shell;

public interface LocalCommandExecutor {
    ExecuteResult executeCommand(String command, long timeout);
}
