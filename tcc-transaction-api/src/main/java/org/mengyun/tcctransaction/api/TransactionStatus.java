package org.mengyun.tcctransaction.api;

/**
 * 事务状态
 * Created by changmingxie on 10/28/15.
 */
public enum TransactionStatus {
    /**
     * try阶段
     */
    TRYING(1),
    /**
     * 确认阶段
     */
    CONFIRMING(2),
    /**
     * 取消阶段
     */
    CANCELLING(3);

    private int id;

     TransactionStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static TransactionStatus valueOf(int id) {

        switch (id) {
            case 1:
                return TRYING;
            case 2:
                return CONFIRMING;
            default:
                return CANCELLING;
        }
    }

}
