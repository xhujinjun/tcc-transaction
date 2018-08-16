package org.mengyun.tcctransaction;


import lombok.Getter;
import lombok.Setter;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.api.TransactionXid;
import org.mengyun.tcctransaction.common.TransactionType;

import javax.transaction.xa.Xid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCC事务
 *
 * Created by changmingxie on 10/26/15.
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = 7291423944314337931L;
    /**
     * 事务编号
     *      用于唯一标识一个事务(使用 UUID 算法生成，保证唯一性)
     */
    private TransactionXid xid;
    /**
     * 事务状态
     *  trying, confirming, cancelling
     */
    @Getter
    private TransactionStatus status;
    /**
     * 事务类型
     *   root, branch
     */
    @Getter
    private TransactionType transactionType;
    /**
     * 重试次数
     */
    @Getter
    private volatile int retriedCount = 0;
    /**
     * 创建时间
     */
    @Getter
    private Date createTime = new Date();
    /**
     * 最好更新时间
     */
    @Getter @Setter
    private Date lastUpdateTime = new Date();
    /**
     * 版本号
     */
    @Getter @Setter
    private long version = 1;
    /**
     * 事务参与者
     */
    @Getter
    private List<Participant> participants = new ArrayList<Participant>();
    /**
     * 附加属性
     */
    @Getter
    private Map<String, Object> attachments = new ConcurrentHashMap<String, Object>();

    public Transaction() {

    }

    public Transaction(TransactionContext transactionContext) {
        this.xid = transactionContext.getXid();
        this.status = TransactionStatus.TRYING;
        this.transactionType = TransactionType.BRANCH;
    }

    public Transaction(TransactionType transactionType) {
        this.xid = new TransactionXid();
        this.status = TransactionStatus.TRYING;
        this.transactionType = transactionType;
    }

    /**
     * 招募参与者
     */
    public void enlistParticipant(Participant participant) {
        participants.add(participant);
    }

    /**
     * 提交TCC事务
     *   调用参与者们提交事务
     */
    public void commit() {
        for (Participant participant : participants) {
            participant.commit();
        }
    }

    /**
     * 回滚TCC事务
     *  调用参与者们回滚事务
     */
    public void rollback() {
        for (Participant participant : participants) {
            participant.rollback();
        }
    }

    public Xid getXid() {
        return xid.clone();
    }

    public void changeStatus(TransactionStatus status) {
        this.status = status;
    }

    public void addRetriedCount() {
        this.retriedCount++;
    }

    public void resetRetriedCount(int retriedCount) {
        this.retriedCount = retriedCount;
    }

    public void updateVersion() {
        this.version++;
    }

    public void updateTime() {
        this.lastUpdateTime = new Date();
    }


}
