package com.bitcoding.helper;

import com.google.common.collect.Lists;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * create by: liumeng
 * description: TODO
 * create time:  2021/3/17 11:30
 *
 * @author LongQi-Howard
 */
@ConditionalOnClass({MybatisAutoConfiguration.class})
@DependsOn("sqlSessionFactory")
@Component
public class BatchSqlUtils {
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    private static SqlSessionFactory SQL_SESSION_FACTORY;

    @PostConstruct
    private void construct() {
        BatchSqlUtils.setSessionFactory(sqlSessionFactory);
    }

    /**
     * 解决当执行批量新增/批量更新时会触发SQLSERVER的最大参数限制(2100)的问题
     * 目前使用到批量插入/批量新增的方法中入参都为LIST集合，故此方法会将LIST按等份切割后遍历执行，如SIZE=100 batchSize=50 则会分割为50 50 执行两次
     * <p>
     * <p>
     * {@see https://docs.microsoft.com/en-us/sql/sql-server/maximum-capacity-specifications-for-sql-server?redirectedfrom=MSDN&view=sql-server-ver15}
     *
     * @param statement mybatis 例子：com.longqi.report.mapper.AmazonTransactionListMapper.insertBatch
     * @param listT     LIST[T]
     * @param batchSize 每次影响的记录条数,以30-50条为佳(sqlserver需保证 每条记录入参*batchSize<2100) {@see https://blog.csdn.net/huanghanqian/article/details/83177178}
     * @param <T>       业务实体
     * @return resultCount
     */
    public static <T> int batchInsert(String statement, List<T> listT, final int batchSize) {
        List<List<T>> list = Lists.partition(listT, batchSize);
        int resultSize = 0;
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession(ExecutorType.BATCH, false)) {
            int size = list.size();
            for (int i = 0; i < size; ) {
                sqlSession.insert(statement, list.get(i));
                i++;
                resultSize = flush(resultSize, sqlSession);
            }
            sqlSession.commit();
        }
        return resultSize;
    }

    /**
     * 解决当执行批量新增/批量更新时会触发SQLSERVER的最大参数限制(2100)的问题
     * 目前使用到批量插入/批量新增的方法中入参都为LIST集合，故此方法会将LIST按等份切割后遍历执行，如SIZE=100 batchSize=50 则会分割为50 50 执行两次
     * <p>
     * <p>
     * {@see https://docs.microsoft.com/en-us/sql/sql-server/maximum-capacity-specifications-for-sql-server?redirectedfrom=MSDN&view=sql-server-ver15}
     *
     * @param statement MYBATIS-STATEMENT {@see org.apache.ibatis.session.SqlSession} 需为批量修改sql,见后续示例
     * @param listT     LIST[T]
     * @param batchSize 每次刷新影响的记录条数,以30-50条为佳 {@see https://blog.csdn.net/huanghanqian/article/details/83177178}
     * @param <T>       业务实体
     * @return resultCount
     */
    public static <T> int batchUpdate(String statement, List<T> listT, final int batchSize) {
        List<List<T>> list = Lists.partition(listT, batchSize);
        int resultSize = 0;
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession(ExecutorType.BATCH, false)) {
            int size = list.size();
            for (int i = 0; i < size; ) {
                sqlSession.update(statement, list.get(i));
                i++;
                resultSize = flush(resultSize, sqlSession);
            }
            sqlSession.commit();
        }
        return resultSize;
    }

    public static <T> int batchDelete(String statement, List<T> listT, final int batchSize) {
        List<List<T>> list = Lists.partition(listT, batchSize);
        int resultSize = 0;
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession(ExecutorType.BATCH, false)) {
            int size = list.size();
            for (int i = 0; i < size; ) {
                sqlSession.delete(statement, list.get(i));
                i++;
                resultSize = flush(resultSize, sqlSession);
            }
            sqlSession.commit();
        }
        return resultSize;
    }

    /**
     * 复用单条update的sql时调用此方法 -效率比批量sql略低
     * {@see https://docs.microsoft.com/en-us/sql/sql-server/maximum-capacity-specifications-for-sql-server?redirectedfrom=MSDN&view=sql-server-ver15}
     *
     * @param statement MYBATIS-STATEMENT {@see org.apache.ibatis.session.SqlSession} 需为单条修改sql
     * @param list      LIST[T]
     * @param batchSize 每次刷新影响的记录条数,以30-50条为佳 {@see https://blog.csdn.net/huanghanqian/article/details/83177178}
     * @param <T>       业务实体
     * @return resultCount
     */
    public static <T> int batchUpdateSingleSQL(String statement, List<T> list, final int batchSize) {
        int resultSize = 0;
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession(ExecutorType.BATCH, false)) {
            int size = list.size();
            for (int i = 0; i < size; ) {
                sqlSession.update(statement, list.get(i));
                i++;
                if (i % batchSize == 0 || i == size) {
                    resultSize = flush(resultSize, sqlSession);
                }
            }
            sqlSession.commit();
        }
        return resultSize;
    }

    private static int flush(int resultSize, SqlSession sqlSession) {
        List<BatchResult> results = sqlSession.flushStatements();
        resultSize = resultCount(results, resultSize);
        sqlSession.clearCache();
        return resultSize;
    }

    private static int resultCount(List<BatchResult> results, int resultSize) {
        for (BatchResult result : results) {
            int[] counts = result.getUpdateCounts();
            for (int count : counts) {
                resultSize += count;
            }
        }
        return resultSize;
    }

    private static void setSessionFactory(SqlSessionFactory sessionFactory) {
        BatchSqlUtils.SQL_SESSION_FACTORY = sessionFactory;
    }
}
