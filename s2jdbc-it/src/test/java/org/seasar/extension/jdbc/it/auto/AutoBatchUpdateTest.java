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
package org.seasar.extension.jdbc.it.auto;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.OptimisticLockException;

import org.seasar.extension.jdbc.JdbcManager;
import org.seasar.extension.jdbc.it.entity.CompKeyDepartment;
import org.seasar.extension.jdbc.it.entity.Department;
import org.seasar.extension.jdbc.it.entity.Department2;
import org.seasar.extension.jdbc.it.entity.Department3;
import org.seasar.extension.jdbc.it.entity.Department4;
import org.seasar.extension.jdbc.it.entity.Employee;
import org.seasar.extension.jdbc.where.SimpleWhere;
import org.seasar.extension.unit.S2TestCase;

/**
 * @author taedium
 * 
 */
public class AutoBatchUpdateTest extends S2TestCase {

    private JdbcManager jdbcManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        include("jdbc.dicon");
    }

    /**
     * 
     * @throws Exception
     */
    public void testExecuteTx() throws Exception {
        List<Department> list = new ArrayList<Department>();
        Department department = new Department();
        department.departmentId = 1;
        department.departmentName = "hoge";
        department.version = 1;
        list.add(department);
        Department department2 = new Department();
        department2.departmentId = 2;
        department2.departmentName = "foo";
        department2.version = 1;
        list.add(department2);

        int[] result = jdbcManager.updateBatch(list).execute();
        assertEquals(2, result.length);

        department =
            jdbcManager.from(Department.class).where(
                new SimpleWhere().eq("departmentId", 1)).getSingleResult();
        assertEquals(1, department.departmentId);
        assertEquals(0, department.departmentNo);
        assertEquals("hoge", department.departmentName);
        assertNull(department.location);
        assertEquals(2, department.version);

        department =
            jdbcManager.from(Department.class).where(
                new SimpleWhere().eq("departmentId", 2)).getSingleResult();
        assertEquals(2, department.departmentId);
        assertEquals(0, department.departmentNo);
        assertEquals("foo", department.departmentName);
        assertNull(department.location);
        assertEquals(2, department.version);
    }

    /**
     * 
     * @throws Exception
     */
    public void testExecute_includesVersionTx() throws Exception {
        List<Department> list = new ArrayList<Department>();
        Department department = new Department();
        department.departmentId = 1;
        department.departmentName = "hoge";
        department.version = 100;
        list.add(department);
        Department department2 = new Department();
        department2.departmentId = 2;
        department2.departmentName = "foo";
        department2.version = 200;
        list.add(department2);

        int[] result =
            jdbcManager.updateBatch(list).includesVersion().execute();
        assertEquals(2, result.length);

        department =
            jdbcManager.from(Department.class).where(
                new SimpleWhere().eq("departmentId", 1)).getSingleResult();
        assertEquals(1, department.departmentId);
        assertEquals(0, department.departmentNo);
        assertEquals("hoge", department.departmentName);
        assertNull(department.location);
        assertEquals(100, department.version);

        department =
            jdbcManager.from(Department.class).where(
                new SimpleWhere().eq("departmentId", 2)).getSingleResult();
        assertEquals(2, department.departmentId);
        assertEquals(0, department.departmentNo);
        assertEquals("foo", department.departmentName);
        assertNull(department.location);
        assertEquals(200, department.version);
    }

    /**
     * 
     * @throws Exception
     */
    public void testExecute_includesTx() throws Exception {
        List<Department> list = new ArrayList<Department>();
        Department department = new Department();
        department.departmentId = 1;
        department.departmentNo = 100;
        department.departmentName = "hoge";
        department.location = "foo";
        department.version = 1;
        list.add(department);
        Department department2 = new Department();
        department2.departmentId = 2;
        department2.departmentNo = 200;
        department2.departmentName = "bar";
        department2.location = "baz";
        department2.version = 1;
        list.add(department2);

        int[] result =
            jdbcManager
                .updateBatch(list)
                .includes("departmentName", "location")
                .execute();
        assertEquals(2, result.length);

        department =
            jdbcManager.from(Department.class).where(
                new SimpleWhere().eq("departmentId", 1)).getSingleResult();
        assertEquals(1, department.departmentId);
        assertEquals(10, department.departmentNo);
        assertEquals("hoge", department.departmentName);
        assertEquals("foo", department.location);
        assertEquals(2, department.version);

        department =
            jdbcManager.from(Department.class).where(
                new SimpleWhere().eq("departmentId", 2)).getSingleResult();
        assertEquals(2, department.departmentId);
        assertEquals(20, department.departmentNo);
        assertEquals("bar", department.departmentName);
        assertEquals("baz", department.location);
        assertEquals(2, department.version);
    }

    /**
     * 
     * @throws Exception
     */
    public void testExecute_excludesTx() throws Exception {
        List<Department> list = new ArrayList<Department>();
        Department department = new Department();
        department.departmentId = 1;
        department.departmentNo = 98;
        department.departmentName = "hoge";
        department.location = "foo";
        department.version = 1;
        list.add(department);
        Department department2 = new Department();
        department2.departmentId = 2;
        department2.departmentNo = 99;
        department2.departmentName = "bar";
        department2.location = "baz";
        department2.version = 1;
        list.add(department2);

        int[] result =
            jdbcManager
                .updateBatch(list)
                .excludes("departmentName", "location")
                .execute();
        assertEquals(2, result.length);

        department =
            jdbcManager.from(Department.class).where(
                new SimpleWhere().eq("departmentId", 1)).getSingleResult();
        assertEquals(1, department.departmentId);
        assertEquals(98, department.departmentNo);
        assertEquals("ACCOUNTING", department.departmentName);
        assertEquals("NEW YORK", department.location);
        assertEquals(2, department.version);

        department =
            jdbcManager.from(Department.class).where(
                new SimpleWhere().eq("departmentId", 2)).getSingleResult();
        assertEquals(2, department.departmentId);
        assertEquals(99, department.departmentNo);
        assertEquals("RESEARCH", department.departmentName);
        assertEquals("DALLAS", department.location);
        assertEquals(2, department.version);
    }

    /**
     * 
     * @throws Exception
     */
    public void testCompKeyTx() throws Exception {
        List<CompKeyDepartment> list = new ArrayList<CompKeyDepartment>();
        CompKeyDepartment department = new CompKeyDepartment();
        department.departmentId1 = 1;
        department.departmentId2 = 1;
        department.departmentName = "hoge";
        department.version = 1;
        list.add(department);
        CompKeyDepartment department2 = new CompKeyDepartment();
        department2.departmentId1 = 2;
        department2.departmentId2 = 2;
        department2.departmentName = "foo";
        department2.version = 1;
        list.add(department2);

        int[] result = jdbcManager.updateBatch(list).execute();
        assertEquals(2, result.length);

        department =
            jdbcManager
                .from(CompKeyDepartment.class)
                .where(
                    new SimpleWhere().eq("departmentId1", 1).eq(
                        "departmentId2",
                        1))
                .getSingleResult();
        assertEquals(1, department.departmentId1);
        assertEquals(1, department.departmentId2);
        assertEquals(0, department.departmentNo);
        assertEquals("hoge", department.departmentName);
        assertNull(department.location);
        assertEquals(2, department.version);

        department =
            jdbcManager
                .from(CompKeyDepartment.class)
                .where(
                    new SimpleWhere().eq("departmentId1", 2).eq(
                        "departmentId2",
                        2))
                .getSingleResult();
        assertEquals(2, department.departmentId1);
        assertEquals(2, department.departmentId2);
        assertEquals(0, department.departmentNo);
        assertEquals("foo", department.departmentName);
        assertNull(department.location);
        assertEquals(2, department.version);
    }

    /**
     * 
     * @throws Exception
     */
    public void testOptimisticLockExceptionTx() throws Exception {
        Employee employee1 =
            jdbcManager
                .from(Employee.class)
                .where("employeeId = ?", 1)
                .getSingleResult();
        Employee employee2 =
            jdbcManager
                .from(Employee.class)
                .where("employeeId = ?", 1)
                .getSingleResult();
        Employee employee3 =
            jdbcManager
                .from(Employee.class)
                .where("employeeId = ?", 2)
                .getSingleResult();
        jdbcManager.update(employee1).execute();
        int[] result = null;
        try {
            result = jdbcManager.updateBatch(employee2, employee3).execute();
        } catch (OptimisticLockException ignore) {
            return;
        }
        if (!containsSuccessNoInfo(result)) {
            fail();
        }
    }

    /**
     * 
     * @throws Exception
     */
    public void testOptimisticLockException_includesVersionTx()
            throws Exception {
        Employee employee1 =
            jdbcManager
                .from(Employee.class)
                .where("employeeId = ?", 1)
                .getSingleResult();
        Employee employee2 =
            jdbcManager
                .from(Employee.class)
                .where("employeeId = ?", 1)
                .getSingleResult();
        Employee employee3 =
            jdbcManager
                .from(Employee.class)
                .where("employeeId = ?", 2)
                .getSingleResult();
        jdbcManager.update(employee1).execute();
        jdbcManager
            .updateBatch(employee2, employee3)
            .includesVersion()
            .execute();
    }

    /**
     * 
     * @throws Exception
     */
    public void testColumnAnnotationTx() throws Exception {
        List<Department2> list = new ArrayList<Department2>();
        Department2 department = new Department2();
        department.departmentId = 1;
        department.departmentName = "hoge";
        list.add(department);
        Department2 department2 = new Department2();
        department2.departmentId = 2;
        department2.departmentName = "foo";
        list.add(department2);

        int[] result = jdbcManager.updateBatch(list).execute();
        assertEquals(2, result.length);
        String sql =
            "select department_name from Department where department_id = ?";
        String departmentName =
            jdbcManager.selectBySql(String.class, sql, 1).getSingleResult();
        assertEquals("ACCOUNTING", departmentName);
        departmentName =
            jdbcManager.selectBySql(String.class, sql, 2).getSingleResult();
        assertEquals("RESEARCH", departmentName);
    }

    /**
     * 
     * @throws Exception
     */
    public void testTransientAnnotationTx() throws Exception {
        List<Department3> list = new ArrayList<Department3>();
        Department3 department = new Department3();
        department.departmentId = 1;
        department.departmentName = "hoge";
        list.add(department);
        Department3 department2 = new Department3();
        department2.departmentId = 2;
        department2.departmentName = "foo";
        list.add(department2);

        int[] result = jdbcManager.updateBatch(list).execute();
        assertEquals(2, result.length);
        String sql =
            "select department_name from Department where department_id = ?";
        String departmentName =
            jdbcManager.selectBySql(String.class, sql, 1).getSingleResult();
        assertEquals("ACCOUNTING", departmentName);
        departmentName =
            jdbcManager.selectBySql(String.class, sql, 2).getSingleResult();
        assertEquals("RESEARCH", departmentName);
    }

    /**
     * 
     * @throws Exception
     */
    public void testTransientModifierTx() throws Exception {
        List<Department4> list = new ArrayList<Department4>();
        Department4 department = new Department4();
        department.departmentId = 1;
        department.departmentName = "hoge";
        list.add(department);
        Department4 department2 = new Department4();
        department2.departmentId = 2;
        department2.departmentName = "foo";
        list.add(department2);

        int[] result = jdbcManager.updateBatch(list).execute();
        assertEquals(2, result.length);
        String sql =
            "select department_name from Department where department_id = ?";
        String departmentName =
            jdbcManager.selectBySql(String.class, sql, 1).getSingleResult();
        assertEquals("ACCOUNTING", departmentName);
        departmentName =
            jdbcManager.selectBySql(String.class, sql, 2).getSingleResult();
        assertEquals("RESEARCH", departmentName);
    }

    private boolean containsSuccessNoInfo(int[] batchResult) {
        for (int i : batchResult) {
            if (i == Statement.SUCCESS_NO_INFO) {
                return true;
            }
        }
        return false;
    }
}
