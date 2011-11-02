package org.rulez.magwas.enterprise.repository;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Id;

import org.hibernate.annotations.Entity;

@Entity
public class RepositoryVersion {
@Id
String id;
String description;
Date createtime;
ArrayList<RepositoryVersion> bases;


public RepositoryVersion() {
}

/**
 * @return the id
 */
public String getId() {
	return id;
}
/**
 * @param id the id to set
 */
public void setId(String id) {
	this.id = id;
}
/**
 * @return the description
 */
public String getDescription() {
	return description;
}
/**
 * @param description the description to set
 */
public void setDescription(String description) {
	this.description = description;
}
/**
 * @return the createtime
 */
public Date getCreatetime() {
	return createtime;
}
/**
 * @param createtime the createtime to set
 */
public void setCreatetime(Date createtime) {
	this.createtime = createtime;
}
/**
 * @return the bases
 */
public ArrayList<RepositoryVersion> getBases() {
	return bases;
}
/**
 * @param bases the bases to set
 */
public void setBases(ArrayList<RepositoryVersion> bases) {
	this.bases = bases;
}

}
