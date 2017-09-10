package com.lorne.tx.db;

import com.lorne.tx.bean.TxTransactionLocal;
import com.lorne.tx.db.relational.AbstractDBConnection;
import com.lorne.tx.db.relational.LCNDBConnection;
import com.lorne.tx.db.service.DataSourceService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;


/**
 * 关系型数据库代理连接池对象
 * create by lorne on 2017/7/29
 */

public class LCNDataSourceProxy extends AbstractResourceProxy<Connection,AbstractDBConnection> implements DataSource {


    private org.slf4j.Logger logger = LoggerFactory.getLogger(LCNDataSourceProxy.class);


    private DataSource dataSource;


    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected Connection createLcnConnection(Connection connection, TxTransactionLocal txTransactionLocal) {
        nowCount++;
        LCNDBConnection lcn = new LCNDBConnection(connection, dataSourceService, txTransactionLocal, subNowCount);
        pools.put(txTransactionLocal.getGroupId(), lcn);
        logger.info("get new connection ->" + txTransactionLocal.getGroupId());
        return lcn;
    }





    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = loadConnection();
        if(connection==null) {
             connection = initLCNConnection(dataSource.getConnection());
            if(connection==null){
                throw new SQLException("connection was overload");
            }
            return connection;
        }else {
            return connection;
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = loadConnection();
        if(connection==null) {
            return initLCNConnection(dataSource.getConnection(username, password));
        }else {
            return connection;
        }
    }


    /**default**/

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }
}
