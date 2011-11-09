package org.rulez.magwas.enterprise.repository;


import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity()
@Table(name = "version")
public class RepositoryVersion {
@Id
String id;
String description;
Date createtime;

public RepositoryVersion() {
	createtime=new Date();
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

}
