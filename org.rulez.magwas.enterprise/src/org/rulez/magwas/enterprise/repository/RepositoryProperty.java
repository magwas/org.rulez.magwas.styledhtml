package org.rulez.magwas.enterprise.repository;

import javax.persistence.Id;

public class RepositoryProperty {
//	@Id
	String RepositoryObjectPK;
	String type;
	String key;
	String value;
	Integer x1;
	Integer y1;
	Integer x2;
	Integer y2;
	
	
	/**
	 * 
	 */
	public RepositoryProperty() {
	}
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the x1
	 */
	public Integer getX1() {
		return x1;
	}
	/**
	 * @param x1 the x1 to set
	 */
	public void setX1(Integer x1) {
		this.x1 = x1;
	}
	/**
	 * @return the y1
	 */
	public Integer getY1() {
		return y1;
	}
	/**
	 * @param y1 the y1 to set
	 */
	public void setY1(Integer y1) {
		this.y1 = y1;
	}
	/**
	 * @return the x2
	 */
	public Integer getX2() {
		return x2;
	}
	/**
	 * @param x2 the x2 to set
	 */
	public void setX2(Integer x2) {
		this.x2 = x2;
	}
	/**
	 * @return the y2
	 */
	public Integer getY2() {
		return y2;
	}
	/**
	 * @param y2 the y2 to set
	 */
	public void setY2(Integer y2) {
		this.y2 = y2;
	}
	
}
