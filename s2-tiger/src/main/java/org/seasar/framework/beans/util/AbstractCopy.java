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
package org.seasar.framework.beans.util;

import java.util.Iterator;
import java.util.Map;

import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.PropertyDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;

/**
 * JavaBeansやMapに対して操作を行う抽象クラスです。
 * 
 * @author higa
 * @param <S>
 *            JavaBeansに対して操作を行うサブタイプです。
 * 
 */
public abstract class AbstractCopy<S extends AbstractCopy<S>> {

    /**
     * 空の文字列の配列です。
     */
    protected static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * 操作の対象に含めるプロパティ名の配列です。
     */
    protected String[] includePropertyNames = EMPTY_STRING_ARRAY;

    /**
     * 操作の対象に含めないプロパティ名の配列です。
     */
    protected String[] excludePropertyNames = EMPTY_STRING_ARRAY;

    /**
     * null値のプロパティを操作の対象外にするかどうかです。
     */
    protected boolean excludesNull = false;

    /**
     * プレフィックスです。
     */
    protected String prefix;

    /**
     * JavaBeanのデリミタです。
     */
    protected char beanDelimiter = '$';

    /**
     * Mapのデリミタです。
     */
    protected char mapDelimiter = '.';

    /**
     * 操作の対象に含めるプロパティ名を指定します。
     * 
     * @param propertyNames
     *            プロパティ名の配列
     * @return このインスタンス自身
     */
    @SuppressWarnings("unchecked")
    public S includes(String... propertyNames) {
        if (excludePropertyNames.length > 0) {
            throw new IllegalArgumentException(
                    "Do not specify both includes and excludes.");
        }
        this.includePropertyNames = propertyNames;
        return (S) this;
    }

    /**
     * 操作の対象に含めないプロパティ名を指定します。
     * 
     * @param propertyNames
     *            プロパティ名の配列
     * @return このインスタンス自身
     */
    @SuppressWarnings("unchecked")
    public S excludes(String... propertyNames) {
        if (includePropertyNames.length > 0) {
            throw new IllegalArgumentException(
                    "Do not specify both includes and excludes.");
        }
        this.excludePropertyNames = propertyNames;
        return (S) this;
    }

    /**
     * null値のプロパティを操作の対象外にします。
     * 
     * @param propertyNames
     *            プロパティ名の配列
     * @return このインスタンス自身
     */
    @SuppressWarnings("unchecked")
    public S excludesNull() {
        this.excludesNull = true;
        return (S) this;
    }

    /**
     * プレフィックスを指定します。
     * 
     * @param prefix
     *            プレフィックス
     * @return このインスタンス自身
     * 
     */
    @SuppressWarnings("unchecked")
    public S prefix(String prefix) {
        this.prefix = prefix;
        return (S) this;
    }

    /**
     * JavaBeansのデリミタを設定します。
     * 
     * @param beanDelimiter
     *            JavaBeansのデリミタ
     * @return このインスタンス自身
     */
    @SuppressWarnings("unchecked")
    public S beanDelimiter(char beanDelimiter) {
        this.beanDelimiter = beanDelimiter;
        return (S) this;
    }

    /**
     * Mapのデリミタを設定します。
     * 
     * @param mapDelimiter
     *            Mapのデリミタ
     * @return このインスタンス自身
     */
    @SuppressWarnings("unchecked")
    public S mapDelimiter(char mapDelimiter) {
        this.mapDelimiter = mapDelimiter;
        return (S) this;
    }

    /**
     * 対象のプロパティかどうかを返します。
     * 
     * @param name
     *            プロパティ名
     * @return 対象のプロパティかどうか
     */
    protected boolean isTargetProperty(String name) {
        if (includePropertyNames.length > 0) {
            for (String s : includePropertyNames) {
                if (s.equals(name)
                        && (prefix == null || name.startsWith(prefix))) {
                    return true;
                }
            }
            return false;
        }
        if (excludePropertyNames.length > 0) {
            for (String s : excludePropertyNames) {
                if (s.equals(name)) {
                    return false;
                }
            }
            return true;
        }
        return prefix == null || name.startsWith(prefix);
    }

    /**
     * BeanからBeanにコピーを行います。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     */
    protected void copyBeanToBean(Object src, Object dest) {
        BeanDesc srcBeanDesc = BeanDescFactory.getBeanDesc(src.getClass());
        BeanDesc destBeanDesc = BeanDescFactory.getBeanDesc(dest.getClass());
        int size = srcBeanDesc.getPropertyDescSize();
        for (int i = 0; i < size; i++) {
            PropertyDesc srcPropertyDesc = srcBeanDesc.getPropertyDesc(i);
            String srcPropertyName = srcPropertyDesc.getPropertyName();
            if (!srcPropertyDesc.isReadable()
                    || !isTargetProperty(srcPropertyName)) {
                continue;
            }
            String destPropertyName = trimPrefix(srcPropertyName);
            if (!destBeanDesc.hasPropertyDesc(destPropertyName)) {
                continue;
            }
            PropertyDesc destPropertyDesc = destBeanDesc
                    .getPropertyDesc(destPropertyName);
            if (!destPropertyDesc.isWritable()) {
                continue;
            }
            Object value = srcPropertyDesc.getValue(src);
            if (value == null && excludesNull) {
                continue;
            }
            destPropertyDesc.setValue(dest, value);
        }
    }

    /**
     * BeanからMapにコピーを行います。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     */
    @SuppressWarnings("unchecked")
    protected void copyBeanToMap(Object src, Map dest) {
        BeanDesc srcBeanDesc = BeanDescFactory.getBeanDesc(src.getClass());
        int size = srcBeanDesc.getPropertyDescSize();
        for (int i = 0; i < size; i++) {
            PropertyDesc srcPropertyDesc = srcBeanDesc.getPropertyDesc(i);
            String srcPropertyName = srcPropertyDesc.getPropertyName();
            if (!srcPropertyDesc.isReadable()
                    || !isTargetProperty(srcPropertyName)) {
                continue;
            }
            Object value = srcPropertyDesc.getValue(src);
            if (value == null && excludesNull) {
                continue;
            }
            String destPropertyName = trimPrefix(srcPropertyName.replace(
                    beanDelimiter, mapDelimiter));
            dest.put(destPropertyName, value);
        }
    }

    /**
     * MapからBeanにコピーを行います。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     */
    protected void copyMapToBean(Map<String, Object> src, Object dest) {
        BeanDesc destBeanDesc = BeanDescFactory.getBeanDesc(dest.getClass());
        for (Iterator<String> i = src.keySet().iterator(); i.hasNext();) {
            String srcPropertyName = i.next();
            if (!isTargetProperty(srcPropertyName)) {
                continue;
            }
            String destPropertyName = trimPrefix(srcPropertyName.replace(
                    mapDelimiter, beanDelimiter));
            if (!destBeanDesc.hasPropertyDesc(destPropertyName)) {
                continue;
            }
            PropertyDesc destPropertyDesc = destBeanDesc
                    .getPropertyDesc(destPropertyName);
            if (!destPropertyDesc.isWritable()) {
                continue;
            }
            Object value = src.get(srcPropertyName);
            if (value == null && excludesNull) {
                continue;
            }
            destPropertyDesc.setValue(dest, value);
        }
    }

    /**
     * MapからMapにコピーを行います。
     * 
     * @param src
     *            コピー元
     * @param dest
     *            コピー先
     */
    protected void copyMapToMap(Map<String, Object> src,
            Map<String, Object> dest) {
        for (Iterator<String> i = src.keySet().iterator(); i.hasNext();) {
            String srcPropertyName = i.next();
            if (!isTargetProperty(srcPropertyName)) {
                continue;
            }
            String destPropertyName = trimPrefix(srcPropertyName);
            Object value = src.get(srcPropertyName);
            if (value == null && excludesNull) {
                continue;
            }
            dest.put(destPropertyName, value);
        }
    }

    /**
     * プレフィックスを削ります。
     * 
     * @param propertyName
     *            プロパティ名
     * @return 削った結果
     */
    protected String trimPrefix(String propertyName) {
        if (prefix == null) {
            return propertyName;
        }
        return propertyName.substring(prefix.length());
    }
}