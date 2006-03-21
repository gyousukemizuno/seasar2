/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.framework.ejb.unit.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;
import javax.persistence.Table;

@Entity
@Table(name = "Foo1")
@SecondaryTable(name = "Foo2")
@SecondaryTables( { @SecondaryTable(name = "Foo3") })
public class Hoge3 {

	@Id
	@Column(name="Foo1aaa", table="Foo1")
	private Long aaa;

	@Column(name="Foo2bbb", table="Foo2")
	private Integer bbb;

	@Column(name="Foo3ccc", table="Foo3")
	private java.util.Date ccc;

	public Long getAaa() {
		return aaa;
	}

	public void setAaa(Long aaa) {
		this.aaa = aaa;
	}

	public Integer getBbb() {
		return bbb;
	}

	public void setBbb(Integer bbb) {
		this.bbb = bbb;
	}

	public java.util.Date getCcc() {
		return ccc;
	}

	public void setCcc(java.util.Date ccc) {
		this.ccc = ccc;
	}
}
