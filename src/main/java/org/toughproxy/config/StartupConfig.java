package org.toughproxy.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.toughproxy.common.RestResult;
import org.toughproxy.common.shell.ExecuteResult;
import org.toughproxy.common.shell.LocalCommandExecutor;
import org.toughproxy.component.Memarylogger;

@Component
public class StartupConfig implements CommandLineRunner {

    @Autowired
    private Memarylogger logger;

    @Autowired
    private ThreadPoolTaskExecutor systaskExecutor;

    @Autowired
    private LocalCommandExecutor localCommandExecutor;

    @Override
    public void run(String... args) throws Exception {
        systaskExecutor.execute(()->{
            ExecuteResult ert = localCommandExecutor.executeCommand("/usr/local/bin/addppp",300*1000);
            RestResult result = new RestResult(ert.getExitCode(), ert.getExecuteOut());
            logger.info("IP池初始化结果：" + result.toString(), Memarylogger.SYSTEM);
        });
    }


}