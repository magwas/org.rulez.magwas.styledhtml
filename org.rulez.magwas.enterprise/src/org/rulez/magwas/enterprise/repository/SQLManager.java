package org.rulez.magwas.enterprise.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.swt.widgets.Shell;
import org.postgresql.jdbc2.AbstractJdbc2Connection;

import uk.ac.bolton.archimate.model.IArchimateFactory;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IArchimateModelElement;
import uk.ac.bolton.archimate.model.IArchimatePackage;
import uk.ac.bolton.archimate.model.IIdentifier;
import uk.ac.bolton.archimate.model.impl.ArchimateFactory;
import uk.ac.bolton.archimate.model.impl.ArchimatePackage;

public class SQLManager implements PersistenceManager {

	Connection con;
	IArchimateModel model;

	
	public SQLManager(String reponame) throws ConfigurationException {
		super();
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

		try {
			Class.forName("org.postgresql.Driver"); 	//FIXME have preference for it, and move it to module initialisation		
			con = DriverManager.getConnection(pref.getUrl(), pref.getUsername(), password);
			((AbstractJdbc2Connection)con).autoCommit=false;
			PreparedStatement psSetRole = con.prepareStatement("set role "+pref.getRole());
			//psSetRole.setString(1,pref.getRole());
			System.out.println("executing " + psSetRole);
			psSetRole.execute();
			psSetRole.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new ConfigurationException("cannot find jdbc driver "+reponame);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ConfigurationException("error with database: "+reponame);
		}

	}
	public void close() {
		System.out.println("closing "+con);
		try {
			con.commit();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void forget() {
		
		try {
			con.rollback();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	

	public Integer addVersion(String name, List<String> basevers, String description, String aclname) {
		int acl;
		try {
			acl = getAclByName(aclname);
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
			addBaseVersions(r,basevers);
			return r;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Error("error with database");
		}
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
		PreparedStatement psGetVersionByame = con.prepareStatement("select id from version_view where name = ?");
		psGetVersionByame.setString(1, name);
		System.out.println("executing"+psGetVersionByame+";");
		ResultSet rs = psGetVersionByame.executeQuery();
		boolean havenext = rs.next();
		assert(havenext != false);
		int r = rs.getInt("id");
		rs.close();
		return r;
	}


	private Object deserialize(String attname,String value) {
		if (attname.equals("file")) {
			return new File(value);
		}
		return value;
	}
	
	public EObject loadObject(String id, Integer version) {
		try {
			PreparedStatement psSelectObject = con.prepareStatement(
					"select parent,type from object_view where version=? and id=?");
			psSelectObject.setInt(1, version);
			psSelectObject.setString(2, id);
			System.out.println("executing "+ psSelectObject+";");
			ResultSet rs = psSelectObject.executeQuery();
			System.out.println("resultset="+rs);
			boolean n = rs.next();
			System.out.println("next="+n);
			assert(n);
			System.out.println("resultset="+rs);
			String parent = rs.getString("parent");
			String type = rs.getString("type");
			EClass eclass = (EClass) IArchimatePackage.eINSTANCE.getEClassifier(type);
			EObject instance = EcoreUtil.create((EClass) eclass);
			PreparedStatement psSelectObjectAttributes = con.prepareStatement(
					"select name,value from object_attribute_view where version=? and parent=?");
			psSelectObjectAttributes.setInt(1, version);
			psSelectObjectAttributes.setString(2, id);
			rs.close();
			psSelectObject.close();
			ResultSet rsa = psSelectObjectAttributes.executeQuery();
			while (rsa.next()) {
				String name = rsa.getString("name");
				String value = rsa.getString("value");
				Object val = deserialize(name,value);
				System.out.println("deserializing ("+name+","+value+") to '"+val+"'("+val.getClass()+")");
				EStructuralFeature feature = eclass.getEStructuralFeature(name);
				instance.eSet(feature, val);
			}
			System.out.println("einstance = "+instance);
			return instance;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Error("SQL problem");
		}
		
	}
	
	@Override
	public void saveObject(EObject node, Integer version) {
		IArchimateModel thismodel = ((IArchimateModelElement) node).getArchimateModel();
		if(null == model) {
			model = thismodel;
		} else if (thismodel != model){
			throw new Error("adding an object from a different model");
		}
		//This saves the object and its attributes
		assert(node instanceof IIdentifier);
		String id = ((IIdentifier) node).getId();
		try {
			PreparedStatement psInsertObject = con.prepareStatement(
					"insert into object_view " +
					"(version, id, parent, type)" +
					" values (?,?,?,?)");
			psInsertObject.setInt(1, version);
			psInsertObject.setString(2, id);
			String parentid;
			if(null == node.eContainer()) {
				parentid = id;
			} else {
				parentid = ((IIdentifier) node.eContainer()).getId();
			}
			
			psInsertObject.setString(3, parentid );
			psInsertObject.setString(4, node.eClass().getName());
			System.out.println(node.eAdapters());
			System.out.println("executing "+psInsertObject+";");
			psInsertObject.execute();
			psInsertObject.close();
			PreparedStatement psInsertObjectAttribute = con.prepareStatement(
					"insert into object_attribute_view " +
					"(version, parent, name, value)" +
					" values (?,?,?,?)");
			EList<EAttribute> attrs = node.eClass().getEAllAttributes();
			for (EAttribute attr : attrs) {
				System.out.println(attr.getName()+"="+node.eGet(attr));
				psInsertObjectAttribute.setInt(1, version);
				psInsertObjectAttribute.setString(2, id);
				psInsertObjectAttribute.setString(3,attr.getName());
				EClassifier attrtype = attr.getEType();
				psInsertObjectAttribute.setString(4,node.eGet(attr).toString());
				psInsertObjectAttribute.execute();
			}
			psInsertObjectAttribute.close();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new Error("SQL problem");
		}
	}
	private void setSQLInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
		if(null == value) {
			stmt.setNull(index, Types.INTEGER);
		} else {
			stmt.setInt(index, value);
		}
	}
	private void setSQLFloat(PreparedStatement stmt, int index, Float value) throws SQLException {
		if(null == value) {
			stmt.setNull(index, Types.FLOAT);
		} else {
			stmt.setFloat(index, value);
		}
	}

/*
 * 			//property

			PreparedStatement psInsertObject = con.prepareStatement(
					"insert into property_view " +
					"(version, parent, type, key, value, x1, y1, x2, y2, weight)" +
					"values (?,?,?,?,?,?,?,?,?,?)");
			psInsertObject.setInt(1, versionid);
			psInsertObject.setString(3, type);
			//System.out.println("executing "+psInsertObject+";");
			psInsertObject.execute();
			psInsertObject.close();
		}
	}
*/
	
	private String loadObjectSQL(String nodeid, int versionid) throws SQLException {
		PreparedStatement psSelectObject = con.prepareStatement(
				"select " +
				"(parent, name, documentation, type, source, target, element, font, fontcolor," +
				"subtype, fillcolor, linetext, alignment, textposition, linewidth)" +
				"from object_view where id = ? and version = ?");		
		psSelectObject.setString(1, nodeid);
		psSelectObject.setInt(2, versionid);
		ResultSet qr = psSelectObject.executeQuery();
		
		assert(true == qr.next());
		String parentid = qr.getString("parent");
		String type = qr.getString("type");
		return parentid;		
	}
}
