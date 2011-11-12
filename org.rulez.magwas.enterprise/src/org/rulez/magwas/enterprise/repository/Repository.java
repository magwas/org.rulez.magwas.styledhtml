package org.rulez.magwas.enterprise.repository;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.widgets.Shell;
import org.postgresql.jdbc2.AbstractJdbc2Connection;

import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IProperty;

public class Repository {

	Connection con;
	private List<String> basevers;
	private int versionid;
	
	
	public Repository(String reponame) throws SQLException, ClassNotFoundException, ConfigurationException {
		ConnContentProvider cp = new ConnContentProvider();
		ConnPref pref = cp.getPref(reponame);
		if(null == pref) {
			throw new ConfigurationException("no such connection: "+reponame);
		}
		String password;
		if(pref.getAskpass()) {
			password = new PasswordDialog(new Shell()).ask();
		} else {
			password = pref.getPassword();
		}

		System.setProperty(CertAuthFactory.CONFIG_KEYSTORE_PATH, pref.getKeystore());
		System.setProperty(CertAuthFactory.CONFIG_KEYSTORE_PWD,password);
		System.setProperty(CertAuthFactory.CONFIG_TRUSTSTORE_PATH, pref.getKeystore());
		System.setProperty(CertAuthFactory.CONFIG_TRUSTSTORE_PWD,password);

		Class.forName("org.postgresql.Driver");	//FIXME have preference for it, and move it to module initialisation
		con = DriverManager.getConnection(pref.getUrl(), pref.getUsername(), password);
		((AbstractJdbc2Connection)con).autoCommit=false;
		PreparedStatement psSetRole = con.prepareStatement("set role "+pref.getRole());
		//psSetRole.setString(1,pref.getRole());
		System.out.println("executing " + psSetRole);
		psSetRole.execute();
		psSetRole.close();
	}
	
	public void close() {
		System.out.println("closing "+con);
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * rollback and close
	 */
	public void forget() {
		
		try {
			con.rollback();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	protected String getModelProperty(IArchimateModel model, String propname) {
		EList<IProperty> props = model.getProperties();
		System.out.println("model props:"+props);
		String value = null;
		for(IProperty prop : props) {
			if(prop.getKey().equals(propname)) {
				value = prop.getValue();
			}
		}
		return value;
	}
	
	protected String getModelVersion(IArchimateModel model) {
		return getModelProperty(model, "modelVersion");
	}

	protected List<String> getBaseVersions(IArchimateModel model) {
		EList<IProperty> props = model.getProperties();
		List<String> vers = new ArrayList<String>();
		for(IProperty prop : props) {
			if(prop.getKey().equals("baseVersion")) {
				vers.add(prop.getValue());
			}
		}
		return vers;
	}
	
	protected int addVersion(String name, String description, String aclname) throws SQLException {
		int acl = getAclByName(aclname);
		PreparedStatement psAddVersion = con.prepareStatement("insert into version_view (name,description, acl) values (?,?,?)");
		psAddVersion.setString(1,name);
		psAddVersion.setString(2,description);
		psAddVersion.setInt(3,acl);
		psAddVersion.execute();
		PreparedStatement psGetVersionByName = con.prepareStatement("select id from version_view where name = ?");
		psGetVersionByName.setString(1, name);
		ResultSet rs = psGetVersionByName.executeQuery();
		boolean havenext = rs.next();
		assert(havenext != false);
		System.out.println("have next "+havenext);
		int r = rs.getInt("id");
		System.out.println("result="+r);
		rs.close();
		return r;
	}
	
	private int getAclByName(String name) throws SQLException {
		PreparedStatement psGetAclByName = con.prepareStatement("select id from acl where name = ?");
		psGetAclByName.setString(1, name);
		ResultSet rs = psGetAclByName.executeQuery();
		boolean havenext = rs.next();
		assert(havenext != false);
		int r = rs.getInt("id");
		rs.close();
		return r;
	}
	
	private void addBaseVersions(int currversion,List<String> baseversions) throws SQLException {
		PreparedStatement psAddBaseVersion = con.prepareStatement("insert into version_hierarchy_view (parent, child) values (?,?)");
		for (String bv: baseversions) {
			int parent = getVersionByName(bv);
			psAddBaseVersion.setInt(1, parent);
			psAddBaseVersion.setInt(2, currversion);
			psAddBaseVersion.execute();
		}
		psAddBaseVersion.close();
	}
	
	private int getVersionByName(String name) throws SQLException {
		PreparedStatement psGetAclByName = con.prepareStatement("select id from version_view where name = ?");
		psGetAclByName.setString(1, name);
		ResultSet rs = psGetAclByName.executeQuery();
		boolean havenext = rs.next();
		assert(havenext != false);
		int r = rs.getInt("id");
		rs.close();
		return r;
	}


	
	public void checkin(IArchimateModel model) throws SQLException {
		String version = getModelVersion(model);
		String name = model.getName();
		String aclname = getModelProperty(model,"modelAcl");
		basevers = getBaseVersions(model);
		versionid = addVersion(version, name,aclname);
		addBaseVersions(versionid,basevers);
		PersistedObject po = new PersistedObject(con, versionid, model,null);
		po.persist();
	}

}
