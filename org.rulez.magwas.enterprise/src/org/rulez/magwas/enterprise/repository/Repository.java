package org.rulez.magwas.enterprise.repository;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.widgets.Shell;

import com.archimatetool.model.IArchimateModel;
import com.archimatetool.model.IProperty;

public class Repository {

	Connection con;
	
	protected void finalize() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
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
	}
	
	protected String getModelVersion(IArchimateModel model) {
		EList<IProperty> props = model.getProperties();
		String currentversion = null;
		for(IProperty prop : props) {
			if(prop.getKey() == "modelVersion") {
				currentversion = prop.getValue();
			}
		}
		return currentversion;
	}

	protected List<String> getBaseVersions(IArchimateModel model) {
		EList<IProperty> props = model.getProperties();
		List<String> vers = new ArrayList<String>();
		for(IProperty prop : props) {
			if(prop.getKey() == "baseVersion") {
				vers.add(prop.getValue());
			}
		}
		return vers;
	}
	
	protected int addVersion(String name, String description) throws SQLException {
		PreparedStatement pstmt = con.prepareStatement("insert into version_view (name,description) values (?,?)");
		pstmt.setString(1,name);
		pstmt.setString(2,description);
		pstmt.execute();
		return 0;
	}
	
	public void checkin(IArchimateModel model) {
	}

}
