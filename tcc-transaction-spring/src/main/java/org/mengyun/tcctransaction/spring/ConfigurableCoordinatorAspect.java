package org.mengyun.tcctransaction.spring;

import org.aspectj.lang.annotation.Aspect;
import org.mengyun.tcctransaction.interceptor.ResourceCoordinatorAspect;
import org.mengyun.tcctransaction.interceptor.ResourceCoordinatorInterceptor;
import org.mengyun.tcctransaction.support.TransactionConfigurator;
import org.springframework.core.Ordered;

/**
 * 协调者织面bean配置信息
 * Created by changmingxie on 11/8/15.
 */
@Aspect
public class ConfigurableCoordinatorAspect extends ResourceCoordinatorAspect implements Ordered {

    private TransactionConfigurator transactionConfigurator;

    public void init() {

        ResourceCoordinatorInterceptor resourceCoordinatorInterceptor = new ResourceCoordinatorInterceptor();
        resourceCoordinatorInterceptor.setTransactionManager(transactionConfigurator.getTransactionManager());
        this.setResourceCoordinatorInterceptor(resourceCoordinatorInterceptor);
    }

    /**
     * 拦截顺序 （一定要后与可补偿事务拦截器）
     * @return
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
