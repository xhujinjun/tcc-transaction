package org.mengyun.tcctransaction.recover;

import java.util.Set;

/**
 * 事务回复配置
 *
 * Created by changming.xie on 6/1/16.
 */
public interface RecoverConfig {

    int getMaxRetryCount();

    int getRecoverDuration();

    String getCronExpression();

    Set<Class<? extends Exception>> getDelayCancelExceptions();

    void setDelayCancelExceptions(Set<Class<? extends Exception>> delayRecoverExceptions);

    int getAsyncTerminateThreadPoolSize();
}
