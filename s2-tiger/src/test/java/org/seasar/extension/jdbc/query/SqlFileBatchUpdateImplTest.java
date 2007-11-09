/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.extension.jdbc.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.persistence.EntityExistsException;

import junit.framework.TestCase;

import org.seasar.extension.jdbc.JdbcContext;
import org.seasar.extension.jdbc.SqlLogRegistry;
import org.seasar.extension.jdbc.SqlLogRegistryLocator;
import org.seasar.extension.jdbc.dialect.StandardDialect;
import org.seasar.extension.jdbc.manager.JdbcManagerImpl;
import org.seasar.extension.jta.TransactionManagerImpl;
import org.seasar.extension.jta.TransactionSynchronizationRegistryImpl;
import org.seasar.framework.exception.ResourceNotFoundRuntimeException;
import org.seasar.framework.mock.sql.MockDataSource;
import org.seasar.framework.mock.sql.MockPreparedStatement;

import static java.util.Arrays.*;

/**
 * @author taedium
 * 
 */
public class SqlFileBatchUpdateImplTest extends TestCase {

    private static final String PATH_SIMPLE = SqlFileUpdateImplTest.class
            .getName()
            + "_update_simpleType";

    private static final String PATH_DTO = SqlFileUpdateImplTest.class
            .getName()
            + "_update_dto";

    private JdbcManagerImpl manager;

    private int addedBatch;

    private int executedBatch;

    private boolean preparedBindVariables;

    @Override
    protected void setUp() throws Exception {
        manager = new JdbcManagerImpl();
        manager.setSyncRegistry(new TransactionSynchronizationRegistryImpl(
                new TransactionManagerImpl()));
        manager.setDataSource(new MockDataSource());
        manager.setDialect(new StandardDialect());

    }

    @Override
    protected void tearDown() throws Exception {
        SqlLogRegistry regisry = SqlLogRegistryLocator.getInstance();
        regisry.clear();
        manager = null;
    }

    /**
     * 
     */
    public void testCallerClass() {
        SqlFileBatchUpdateImpl<Object> query = new SqlFileBatchUpdateImpl<Object>(
                manager, "aaa.sql", new ArrayList<Object>());
        assertSame(query, query.callerClass(getClass()));
        assertEquals(getClass(), query.callerClass);
    }

    /**
     * 
     */
    public void testCallerMethodName() {
        SqlFileBatchUpdateImpl<Object> query = new SqlFileBatchUpdateImpl<Object>(
                manager, "aaa.sql", new ArrayList<Object>());
        assertSame(query, query.callerMethodName("hoge"));
        assertEquals("hoge", query.callerMethodName);
    }

    /**
     * 
     */
    public void testQueryTimeout() {
        SqlFileBatchUpdateImpl<Object> query = new SqlFileBatchUpdateImpl<Object>(
                manager, "aaa.sql", new ArrayList<Object>());
        assertSame(query, query.queryTimeout(100));
        assertEquals(100, query.queryTimeout);
    }

    /**
     * 
     */
    public void testParameterList() {
        SqlFileBatchUpdateImpl<String> query = new SqlFileBatchUpdateImpl<String>(
                manager, "aaa.sql", asList("hoge", "hoge2"));
        assertEquals(2, query.parameterList.size());
        Object param = query.parameterList.get(0);
        assertEquals("hoge", param);
        param = query.parameterList.get(1);
        assertEquals("hoge2", param);
    }

    /**
     * 
     */
    public void testPrepareNode() {
        SqlFileBatchUpdateImpl<String> query = new SqlFileBatchUpdateImpl<String>(
                manager, PATH_SIMPLE, asList("foo", "bar"));
        query.prepareCallerClassAndMethodName("execute");
        query.prepareNode();
        assertNotNull(query.node);
    }

    /**
     * 
     */
    public void testPrepareNode_resourceNotFound() {
        SqlFileBatchUpdateImpl<String> query = new SqlFileBatchUpdateImpl<String>(
                manager, "xxx", asList("foo", "bar"));
        query.prepareCallerClassAndMethodName("execute");
        try {
            query.prepareNode();
            fail();
        } catch (ResourceNotFoundRuntimeException e) {
            System.out.println(e);
            assertEquals("xxx", e.getPath());
        }
    }

    /**
     * 
     */
    public void testExecuteBatch_simpleType() {
        SqlFileBatchUpdateImpl<String> query = new SqlFileBatchUpdateImpl<String>(
                manager, PATH_SIMPLE, asList("foo", "bar")) {

            @Override
            protected PreparedStatement getPreparedStatement(
                    JdbcContext jdbcContext) {
                assertNotNull(executedSql);
                MockPreparedStatement ps = new MockPreparedStatement(null, null) {

                    @Override
                    public void addBatch() throws SQLException {
                        ++addedBatch;
                        super.addBatch();
                    }

                    @Override
                    public int[] executeBatch() throws SQLException {
                        ++executedBatch;
                        return new int[] { 1, 1 };
                    }
                };
                return ps;
            }

            @Override
            protected void prepareInParams(PreparedStatement ps) {
                preparedBindVariables = true;
                super.prepareInParams(ps);
            }

        };
        int[] ret = query.execute();
        assertEquals(2, ret.length);
        assertEquals(2, addedBatch);
        assertEquals(1, executedBatch);
        assertTrue(preparedBindVariables);
        assertEquals("update aaa set name = ? where id = 1", query.sqlContext
                .getSql());
        SqlLogRegistry sqlLogRegistry = SqlLogRegistryLocator.getInstance();
        assertEquals(2, sqlLogRegistry.getSize());
        assertEquals("update aaa set name = 'foo' where id = 1", sqlLogRegistry
                .get(0).getCompleteSql());
        assertEquals("update aaa set name = 'bar' where id = 1", sqlLogRegistry
                .get(1).getCompleteSql());
    }

    /**
     * 
     */
    public void testExecuteBatch_simpleType_bindNull() {
        SqlFileBatchUpdateImpl<String> query = new SqlFileBatchUpdateImpl<String>(
                manager, PATH_SIMPLE, asList("foo", "bar")) {

            @Override
            protected PreparedStatement getPreparedStatement(
                    JdbcContext jdbcContext) {
                assertNotNull(executedSql);
                MockPreparedStatement ps = new MockPreparedStatement(null, null) {

                    @Override
                    public void addBatch() throws SQLException {
                        ++addedBatch;
                        super.addBatch();
                    }

                    @Override
                    public int[] executeBatch() throws SQLException {
                        ++executedBatch;
                        return new int[] { 1, 1 };
                    }
                };
                return ps;
            }

            @Override
            protected void prepareInParams(PreparedStatement ps) {
                preparedBindVariables = true;
                super.prepareInParams(ps);
            }

        };
        try {
            query.prepareParameter(null);
            fail();
        } catch (NullPointerException ignore) {
        }
    }

    /**
     * 
     */
    public void testExecuteBatch_dto() {
        MyDto dto = new MyDto();
        dto.id = 1;
        dto.name = "foo";
        MyDto dto2 = new MyDto();
        dto2.id = 2;
        dto2.name = "bar";
        SqlFileBatchUpdateImpl<MyDto> query = new SqlFileBatchUpdateImpl<MyDto>(
                manager, PATH_DTO, asList(dto, dto2)) {

            @Override
            protected PreparedStatement getPreparedStatement(
                    JdbcContext jdbcContext) {
                assertNotNull(executedSql);
                MockPreparedStatement ps = new MockPreparedStatement(null, null) {

                    @Override
                    public void addBatch() throws SQLException {
                        ++addedBatch;
                        super.addBatch();
                    }

                    @Override
                    public int[] executeBatch() throws SQLException {
                        ++executedBatch;
                        return new int[] { 1, 1 };
                    }
                };
                return ps;
            }

            @Override
            protected void prepareInParams(PreparedStatement ps) {
                preparedBindVariables = true;
                super.prepareInParams(ps);
            }

        };
        int[] ret = query.execute();
        assertEquals(2, ret.length);
        assertEquals(1, executedBatch);
        assertEquals(2, addedBatch);
        assertTrue(preparedBindVariables);
        assertEquals("update aaa set name = ? where id = ?", query.sqlContext
                .getSql());
        SqlLogRegistry sqlLogRegistry = SqlLogRegistryLocator.getInstance();
        assertEquals(2, sqlLogRegistry.getSize());
        assertEquals("update aaa set name = 'foo' where id = 1", sqlLogRegistry
                .get(0).getCompleteSql());
        assertEquals("update aaa set name = 'bar' where id = 2", sqlLogRegistry
                .get(1).getCompleteSql());
    }

    /**
     * 
     */
    public void testExecuteBatch_entityExists() {
        SqlFileBatchUpdateImpl<String> query = new SqlFileBatchUpdateImpl<String>(
                manager, PATH_SIMPLE, asList("foo", "bar")) {

            @Override
            protected PreparedStatement getPreparedStatement(
                    JdbcContext jdbcContext) {
                assertNotNull(executedSql);
                MockPreparedStatement ps = new MockPreparedStatement(null, null) {

                    @Override
                    public int[] executeBatch() throws SQLException {
                        throw new SQLException("hoge", "23");
                    }
                };
                return ps;
            }

            @Override
            protected void prepareInParams(PreparedStatement ps) {
                preparedBindVariables = true;
                super.prepareInParams(ps);
            }

        };
        try {
            query.execute();
            fail();
        } catch (EntityExistsException expected) {
            expected.printStackTrace();
        }
    }

    /**
     * 
     */
    public void testExecuteBatch_batchSize1() {
        SqlFileBatchUpdateImpl<String> query = new SqlFileBatchUpdateImpl<String>(
                manager, PATH_SIMPLE, asList("foo", "bar", "baz")) {

            @Override
            protected PreparedStatement getPreparedStatement(
                    JdbcContext jdbcContext) {
                assertNotNull(executedSql);
                MockPreparedStatement ps = new MockPreparedStatement(null, null) {

                    @Override
                    public void addBatch() throws SQLException {
                        ++addedBatch;
                        super.addBatch();
                    }

                    @Override
                    public int[] executeBatch() throws SQLException {
                        ++executedBatch;
                        return executedBatch == 1 ? new int[] { 1, 2 }
                                : new int[] { 3 };
                    }
                };
                return ps;
            }

            @Override
            protected void prepareInParams(PreparedStatement ps) {
                preparedBindVariables = true;
                super.prepareInParams(ps);
            }

        };
        int[] ret = query.batchSize(2).execute();
        assertEquals(3, addedBatch);
        assertEquals(2, executedBatch);
        assertEquals(3, ret.length);
        assertEquals(1, ret[0]);
        assertEquals(2, ret[1]);
        assertEquals(3, ret[2]);
        assertTrue(preparedBindVariables);
        assertEquals("update aaa set name = ? where id = 1", query.sqlContext
                .getSql());
        SqlLogRegistry sqlLogRegistry = SqlLogRegistryLocator.getInstance();
        assertEquals(3, sqlLogRegistry.getSize());
        assertEquals("update aaa set name = 'foo' where id = 1", sqlLogRegistry
                .get(0).getCompleteSql());
        assertEquals("update aaa set name = 'bar' where id = 1", sqlLogRegistry
                .get(1).getCompleteSql());
        assertEquals("update aaa set name = 'baz' where id = 1", sqlLogRegistry
                .get(2).getCompleteSql());
    }

    /**
     * 
     */
    public void testExecuteBatch_batchSize2() {
        SqlFileBatchUpdateImpl<String> query = new SqlFileBatchUpdateImpl<String>(
                manager, PATH_SIMPLE, asList("hoge", "foo", "bar", "baz")) {

            @Override
            protected PreparedStatement getPreparedStatement(
                    JdbcContext jdbcContext) {
                assertNotNull(executedSql);
                MockPreparedStatement ps = new MockPreparedStatement(null, null) {

                    @Override
                    public void addBatch() throws SQLException {
                        ++addedBatch;
                        super.addBatch();
                    }

                    @Override
                    public int[] executeBatch() throws SQLException {
                        ++executedBatch;
                        return executedBatch == 1 ? new int[] { 1, 2 }
                                : new int[] { 3, 4 };
                    }
                };
                return ps;
            }

            @Override
            protected void prepareInParams(PreparedStatement ps) {
                preparedBindVariables = true;
                super.prepareInParams(ps);
            }

        };
        int[] ret = query.batchSize(2).execute();
        assertEquals(4, addedBatch);
        assertEquals(2, executedBatch);
        assertEquals(4, ret.length);
        assertEquals(1, ret[0]);
        assertEquals(2, ret[1]);
        assertEquals(3, ret[2]);
        assertEquals(4, ret[3]);
        assertTrue(preparedBindVariables);
        assertEquals("update aaa set name = ? where id = 1", query.sqlContext
                .getSql());
        SqlLogRegistry sqlLogRegistry = SqlLogRegistryLocator.getInstance();
        assertEquals(3, sqlLogRegistry.getSize());
        assertEquals("update aaa set name = 'foo' where id = 1", sqlLogRegistry
                .get(0).getCompleteSql());
        assertEquals("update aaa set name = 'bar' where id = 1", sqlLogRegistry
                .get(1).getCompleteSql());
        assertEquals("update aaa set name = 'baz' where id = 1", sqlLogRegistry
                .get(2).getCompleteSql());
    }

    private static class MyDto {

        /**
         * 
         */
        public Integer id;

        /**
         * 
         */
        public String name;

    }
}
