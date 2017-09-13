package com.lorne.tx.service.impl;

import com.lorne.core.framework.exception.ServiceException;
import com.lorne.tx.Constants;
import com.lorne.tx.bean.TxTransactionInfo;
import com.lorne.tx.bean.TxTransactionLocal;
import com.lorne.tx.mq.model.TxGroup;
import com.lorne.tx.mq.service.MQTxManagerService;
import com.lorne.tx.service.TransactionServer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 分布式事务启动开始时的业务处理
 * Created by lorne on 2017/6/8.
 */
@Service(value = "txStartTransactionServer")
public class TxStartTransactionServerImpl implements TransactionServer {


    private Logger logger = LoggerFactory.getLogger(TxStartTransactionServerImpl.class);


    @Autowired
    protected MQTxManagerService txManagerService;


    @Override
    public Object execute(ProceedingJoinPoint point, TxTransactionInfo info) throws Throwable {
        //分布式事务开始执行
        logger.info("tx-start");

        //创建事务组
        TxGroup txGroup = txManagerService.createTransactionGroup();

        //获取不到模块信息重新连接，本次事务异常返回数据.
        if (txGroup == null) {
            throw new ServiceException("创建事务组异常.");
        }
        final String groupId = txGroup.getGroupId();
        int state = 0;
        try {
            TxTransactionLocal txTransactionLocal = new TxTransactionLocal();
            txTransactionLocal.setGroupId(groupId);
            txTransactionLocal.setHasStart(true);
            txTransactionLocal.setTransactional(info.getTransactional());
            txTransactionLocal.setMaxTimeOut(Constants.maxOutTime);
            TxTransactionLocal.setCurrent(txTransactionLocal);
            Object obj = point.proceed();
            state = 1;
            return obj;
        } catch (Throwable e) {
            throw e;
        } finally {
            txManagerService.closeTransactionGroup(groupId, state);
            TxTransactionLocal.setCurrent(null);
            logger.info("tx-end-"+txGroup.getGroupId()+">"+state);
        }
    }

}
