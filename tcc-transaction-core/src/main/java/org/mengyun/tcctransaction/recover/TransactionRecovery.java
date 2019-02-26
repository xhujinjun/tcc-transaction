package org.mengyun.tcctransaction.recover;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.mengyun.tcctransaction.OptimisticLockException;
import org.mengyun.tcctransaction.Transaction;
import org.mengyun.tcctransaction.TransactionRepository;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.common.TransactionType;
import org.mengyun.tcctransaction.support.TransactionConfigurator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 事务恢复器
 *
 * Created by changmingxie on 11/10/15.
 */
@Slf4j
public class TransactionRecovery {

    private TransactionConfigurator transactionConfigurator;

    /**
     * 开启事务恢复（job调用入口）
     */
    public void startRecover() {
        //查询执行异常的事务列表
        List<Transaction> transactions = loadErrorTransactions();
        //恢复事务
        recoverErrorTransactions(transactions);
    }

    private List<Transaction> loadErrorTransactions() {

        long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();

        TransactionRepository transactionRepository = transactionConfigurator.getTransactionRepository();
        RecoverConfig recoverConfig = transactionConfigurator.getRecoverConfig();

        return transactionRepository.findAllUnmodifiedSince(new Date(currentTimeInMillis - recoverConfig.getRecoverDuration() * 1000));
    }

    private void recoverErrorTransactions(List<Transaction> transactions) {

        for (Transaction transaction : transactions) {

            if (transaction.getRetriedCount() > transactionConfigurator.getRecoverConfig().getMaxRetryCount()) {

                log.error(String.format("recover failed with max retry count,will not try again. txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)));
                continue;
            }

            if (transaction.getTransactionType().equals(TransactionType.BRANCH)
                    && (transaction.getCreateTime().getTime() +
                    transactionConfigurator.getRecoverConfig().getMaxRetryCount() *
                            transactionConfigurator.getRecoverConfig().getRecoverDuration() * 1000
                    > System.currentTimeMillis())) {
                continue;
            }
            
            try {
                transaction.addRetriedCount();

                if (transaction.getStatus().equals(TransactionStatus.CONFIRMING)) {

                    transaction.changeStatus(TransactionStatus.CONFIRMING);
                    transactionConfigurator.getTransactionRepository().update(transaction);
                    transaction.commit();
                    transactionConfigurator.getTransactionRepository().delete(transaction);

                } else if (transaction.getStatus().equals(TransactionStatus.CANCELLING)
                        || transaction.getTransactionType().equals(TransactionType.ROOT)) {

                    transaction.changeStatus(TransactionStatus.CANCELLING);
                    transactionConfigurator.getTransactionRepository().update(transaction);
                    transaction.rollback();
                    transactionConfigurator.getTransactionRepository().delete(transaction);
                }

            } catch (Throwable throwable) {

                if (throwable instanceof OptimisticLockException
                        || ExceptionUtils.getRootCause(throwable) instanceof OptimisticLockException) {
                    log.warn(String.format("optimisticLockException happened while recover. txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)), throwable);
                } else {
                    log.error(String.format("recover failed, txid:%s, status:%s,retried count:%d,transaction content:%s", transaction.getXid(), transaction.getStatus().getId(), transaction.getRetriedCount(), JSON.toJSONString(transaction)), throwable);
                }
            }
        }
    }

    public void setTransactionConfigurator(TransactionConfigurator transactionConfigurator) {
        this.transactionConfigurator = transactionConfigurator;
    }
}
