/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
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
package org.seasar.extension.jdbc.gen.dialect;

import java.math.BigDecimal;
import java.sql.Types;

import javax.persistence.GenerationType;

/**
 * H2の方言を扱うクラスです。
 * 
 * @author taedium
 */
public class H2GenDialect extends StandardGenDialect {

    /** テーブルが見つからないことを示すエラーコード */
    protected static int TABLE_NOT_FOUND_ERROR_CODE = 42101;

    /**
     * インスタンスを構築します。
     */
    public H2GenDialect() {
        typeMap.put(Types.BINARY, H2Type.BINARY);
        typeMap.put(Types.DECIMAL, H2Type.DECIMAL);
    }

    @Override
    public String getDefaultSchemaName(String userName) {
        return null;
    }

    @Override
    public GenerationType getDefaultGenerationType() {
        return GenerationType.IDENTITY;
    }

    @Override
    public boolean supportsSequence() {
        return true;
    }

    @Override
    public String getSequenceDefinitionFragment(String dataType, int initValue,
            int allocationSize) {
        return "start with " + allocationSize + " increment by " + initValue;
    }

    @Override
    public String getIdentityColumnDefinition() {
        return "generated by default as identity";
    }

    @Override
    public boolean isTableNotFound(Throwable throwable) {
        Integer errorCode = getErrorCode(throwable);
        return errorCode != null
                && errorCode.intValue() == TABLE_NOT_FOUND_ERROR_CODE;
    }

    /**
     * H2用の{@link Type}の実装です。
     * 
     * @author taedium
     */
    public static class H2Type extends StandardType {

        private static Type BINARY = new H2Type() {

            @Override
            public Class<?> getJavaClass(int length, int precision, int scale,
                    String typeName) {
                return byte[].class;
            }

            @Override
            public String getColumnDefinition(int length, int precision,
                    int scale, String typeName) {
                return format("binary(%d)", length);
            }
        };

        private static Type DECIMAL = new H2Type() {

            @Override
            public Class<?> getJavaClass(int length, int precision, int scale,
                    String typeName) {
                return BigDecimal.class;
            }

            @Override
            public String getColumnDefinition(int length, int precision,
                    int scale, String typeName) {
                return format("decimal(%d,%d)", precision, scale);
            }
        };

        /**
         * インスタンスを構築します。
         */
        protected H2Type() {
        }

    }
}
