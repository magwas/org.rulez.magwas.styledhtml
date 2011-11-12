package org.rulez.magwas.enterprise.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import uk.ac.bolton.archimate.model.IAccessRelationship;
import uk.ac.bolton.archimate.model.IArchimateDiagramModel;
import uk.ac.bolton.archimate.model.IArchimateModel;
import uk.ac.bolton.archimate.model.IBounds;
import uk.ac.bolton.archimate.model.IDiagramModel;
import uk.ac.bolton.archimate.model.IDiagramModelArchimateConnection;
import uk.ac.bolton.archimate.model.IDiagramModelArchimateObject;
import uk.ac.bolton.archimate.model.IDiagramModelBendpoint;
import uk.ac.bolton.archimate.model.IDiagramModelConnection;
import uk.ac.bolton.archimate.model.IDiagramModelContainer;
import uk.ac.bolton.archimate.model.IDiagramModelObject;
import uk.ac.bolton.archimate.model.IDiagramModelReference;
import uk.ac.bolton.archimate.model.IDocumentable;
import uk.ac.bolton.archimate.model.IFolder;
import uk.ac.bolton.archimate.model.IFolderContainer;
import uk.ac.bolton.archimate.model.IFontAttribute;
import uk.ac.bolton.archimate.model.IIdentifier;
import uk.ac.bolton.archimate.model.IInterfaceElement;
import uk.ac.bolton.archimate.model.INameable;
import uk.ac.bolton.archimate.model.IProperties;
import uk.ac.bolton.archimate.model.IProperty;
import uk.ac.bolton.archimate.model.IRelationship;
import uk.ac.bolton.archimate.model.ITextContent;

public class PersistedObject {

	private String id=null;
	private String name=null;
	private String documentation=null;
	private String type=null;
	private Integer subtype=null;
	private String source=null,target=null, element=null; //these are references to other objects
	private String font=null, fontcolor=null,fillcolor=null; //graphical properties
	private Integer x1=null, x2=null, y1=null, y2=null;
	private String key=null, value=null;

	private Connection con;
	private Integer versionid;
	private PersistedObject parent;

	private List<String> ovrvars;
	
	private List<String> dependents = new ArrayList<String>();//List of ids of objects depending on this
	private Boolean persisted=false;
	static HashMap<String, PersistedObject> objdir = null;
	static List<PersistedObject> properties = new ArrayList<PersistedObject>();
	
	public PersistedObject(Connection connection, Integer version, EObject node, PersistedObject parent) throws SQLException {
		super();
		if (null == objdir) {
			objdir = new HashMap<String, PersistedObject>();
		}
		ovrvars = new ArrayList<String>();
		con=connection;
		versionid=version;
		this.parent=parent;
		parseNode(node);
	}
	
	
	private void setInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
		if(null == value) {
			stmt.setNull(index, Types.INTEGER);
		} else {
			stmt.setInt(index, value);
		}
	}

	public void persist() throws SQLException {
		if (persisted) {
			return;
		}
		persistSQL();
		persisted = true;
		for(PersistedObject prop : properties) {
			prop.persist();
		}
		for(String dependent : dependents) {
			PersistedObject dobj = objdir.get(dependent);
			dobj.persist();
		}
	}
	
	private void persistSQL() throws SQLException {
		if (null != id) {
			PreparedStatement psInsertObject = con.prepareStatement(
					"insert into object_view " +
					"(version, id, parent, name, documentation, type, source, target, element, font, fontcolor, subtype, fillcolor)" +
					"values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
			psInsertObject.setInt(1, versionid);
			psInsertObject.setString(2, id);
			psInsertObject.setString(3, parent.getId());
			psInsertObject.setString(4, name);
			psInsertObject.setString(5, documentation);
			psInsertObject.setString(6, type);
			psInsertObject.setString(7, source);
			psInsertObject.setString(8, target);
			psInsertObject.setString(9, element);
			psInsertObject.setString(10, font);
			psInsertObject.setString(11, fontcolor);
			setInt(psInsertObject,12,subtype);
			psInsertObject.setString(13, fillcolor);
			System.out.println("executing "+psInsertObject);
			psInsertObject.execute();
			psInsertObject.close();
		} else {
			//property
			PreparedStatement psInsertObject = con.prepareStatement(
					"insert into property_view " +
					"(version, parent, type, key, value, x1, y1, x2, y2)" +
					"values (?,?,?,?,?,?,?,?,?)");
			psInsertObject.setInt(1, versionid);
			psInsertObject.setString(2, parent.getId());
			psInsertObject.setString(3, type);
			psInsertObject.setString(4, key);
			psInsertObject.setString(5, value);
			setInt(psInsertObject,6,x1);
			setInt(psInsertObject,7,y1);
			setInt(psInsertObject,8,x2);
			setInt(psInsertObject,9,y2);
			System.out.println("executing "+psInsertObject);
			psInsertObject.execute();
			psInsertObject.close();
		}
		con.commit();
	}

	public void ready() {
		if (null == parent) {
			assert(null != id);			
			parent = this;
		}
		if (null == id){
			parent.addToProperties(this);
		} else {
			parent.setDependent(id);
			objdir.put(id, this);
		}
	}

	public void addToProperties(PersistedObject o) {
		properties.add(o);
	}
	public void setDependent(String id) {
		dependents.add(id);
	}
	
	public void parseNode(EObject node) throws SQLException {
		setType(node.eClass().getName());
		String id=null;
		if(node instanceof IIdentifier){
			id = ((IIdentifier) node).getId();
			setId(id);
		}

		if(node instanceof INameable) {
			setName(((INameable) node).getName());
		}
		
		if(node instanceof IDocumentable) {
			setDocumentation(((IDocumentable) node).getDocumentation());
		}
		if(node instanceof ITextContent) {
			setDocumentation(((ITextContent) node).getContent());
		}
		
		if(node instanceof IArchimateModel) {
			setDocumentation(((IArchimateModel) node).getPurpose());
			String modelversion = ((IArchimateModel) node).getVersion();
			if(! modelversion.equals("2.0.0")) {
				//FIXME appropriate warning here
			}
		}
		if(node instanceof IProperty) {
			setKey(((IProperty) node).getKey());
			setValue(((IProperty) node).getValue());
		}
		if(node instanceof IBounds) {
			setX1(((IBounds) node).getX());
			setY1(((IBounds) node).getY());
			setX2(((IBounds) node).getWidth());
			setY2(((IBounds) node).getHeight());
		}
		if(node instanceof IArchimateDiagramModel) {
			setSubtype(((IArchimateDiagramModel) node).getViewpoint());
		}
		
		if(node instanceof IInterfaceElement) {
			setSubtype(((IInterfaceElement) node).getInterfaceType());
		}

		if(node instanceof IDiagramModel) {
			setSubtype(((IDiagramModel) node).getConnectionRouterType());
		}

		if(node instanceof IDiagramModelArchimateConnection) {
			setElement(((IDiagramModelArchimateConnection) node).getRelationship().getId());
		}

		if(node instanceof IDiagramModelArchimateObject) {
			setElement(((IDiagramModelArchimateObject) node).getArchimateElement().getId());
			setSubtype(((IDiagramModelArchimateObject) node).getType());
		}
		
		if(node instanceof IDiagramModelBendpoint) {
			//FIXME figure it out float weight = ((IDiagramModelBendpoint) node).getWeight();
			setX1(((IDiagramModelBendpoint) node).getStartX());
			setY1(((IDiagramModelBendpoint) node).getStartY());
			setX2(((IDiagramModelBendpoint) node).getEndX());
			setY2(((IDiagramModelBendpoint) node).getEndY());			
		}
		
		if(node instanceof IFontAttribute) {
			setFont(((IFontAttribute) node).getFont());
			setFontcolor(((IFontAttribute) node).getFontColor());
			setSubtype(((IFontAttribute) node).getTextAlignment());			
		}
		
		if(node instanceof IDiagramModelObject) {
			//FIXME IBounds bounds = ((IDiagramModelObject) node).getBounds();
			EList<IDiagramModelConnection> scs = ((IDiagramModelObject) node).getSourceConnections();
			for (IDiagramModelConnection sc: scs) {
				//addNode(sc,id);
			}
			EList<IDiagramModelConnection> tcs = ((IDiagramModelObject) node).getTargetConnections();
			for (IDiagramModelConnection tc : tcs) {
				//FIXME addNode(tc,id);		
			}
			setFillcolor(((IDiagramModelObject) node).getFillColor());
		}

		if(node instanceof IDiagramModelReference) {
			setElement(((IDiagramModelReference) node).getReferencedModel().getId());
		}
		
		if(node instanceof IRelationship) {
			setSource(((IRelationship) node).getSource().getId());
			setTarget(((IRelationship) node).getTarget().getId());
		}
		if(node instanceof IAccessRelationship) {
			setSubtype(((IAccessRelationship) node).getAccessType());
		}

		/*
		 *          here comes the multiple-value stuff
		 */
		if(node instanceof IDiagramModelConnection) {
			setDocumentation(((IDiagramModelConnection) node).getText());
			//FIXME int textpos = ((IDiagramModelConnection) node).getTextPosition();
			setSource(((IDiagramModelConnection) node).getSource().getId());
			setTarget(((IDiagramModelConnection) node).getTarget().getId());
			EList<IDiagramModelBendpoint> bendpoints = ((IDiagramModelConnection) node).getBendpoints();
			for (IDiagramModelBendpoint bp: bendpoints) {
				//addNode(bp,id);
			}
			// FIXME int linewith = ((IDiagramModelConnection) node).getLineWidth();
			// FIXME String linecolor = ((IDiagramModelConnection) node).getLineColor()
			setType(((IDiagramModelConnection) node).getType());
		}
		
		if(node instanceof IProperties) {
			EList<IProperty> pl = ((IProperties) node).getProperties();
			for (IProperty prop : pl) {
				//addNode(prop,id);
			}
		}
		if(node instanceof IDiagramModelContainer) {
			EList<IDiagramModelObject> children = ((IDiagramModelContainer) node).getChildren();
			for (IDiagramModelObject child : children) {
				new PersistedObject(con, versionid, child, this);
			}
		}
	
		if(node instanceof IFolderContainer) {
			EList<IFolder> fl = ((IFolderContainer) node).getFolders();
			for (IFolder folder : fl) {
				new PersistedObject(con, versionid, folder, this);
			}
		}
		if(node instanceof IFolder) {
			EList<EObject> fl = ((IFolder) node).getElements();
			for (EObject kid : fl) {
				new PersistedObject(con, versionid, kid, this);
			}
			System.out.println("foldertype name="+ ((IFolder) node).getType().getName());
			System.out.println("foldertype literal="+ ((IFolder) node).getType().getLiteral());
			id = ((IFolder) node).getType().getName(); //This is intentional, to make merging models easier. FIXME make sure not getLiteral is the right method
		}
		ready();
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		q("this.id");
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		q("this.name");
		this.name = name;
	}
	public String getDocumentation() {
		return documentation;
	}
	public void setDocumentation(String documentation) {
		q("this.documentation");
		this.documentation = documentation;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		q("this.type");
		this.type = type;
	}
	public Integer getSubtype() {
		return subtype;
	}
	public void setSubtype(Integer subtype) {
		q("this.subtype");
		this.subtype = subtype;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		q("this.source");
		this.source = source;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		q("this.target");
		this.target = target;
	}
	public String getElement() {
		return element;
	}
	public void setElement(String element) {
		q("this.element");
		this.element = element;
	}
	public String getFont() {
		return font;
	}
	public void setFont(String font) {
		q("this.font");
		this.font = font;
	}
	public String getFontcolor() {
		return fontcolor;
	}
	public void setFontcolor(String fontcolor) {
		q("this.fontcolor");
		this.fontcolor = fontcolor;
	}
	public String getFillcolor() {
		return fillcolor;
	}
	public void setFillcolor(String fillcolor) {
		q("this.fillcolor");
		this.fillcolor = fillcolor;
	}
	public Integer getX1() {
		return x1;
	}
	public void setX1(Integer x1) {
		q("this.x1");
		this.x1 = x1;
	}
	public Integer getX2() {
		return x2;
	}
	public void setX2(Integer x2) {
		q("this.x2");
		this.x2 = x2;
	}
	public Integer getY1() {
		return y1;
	}
	public void setY1(Integer y1) {
		q("this.y1");
		this.y1 = y1;
	}
	public Integer getY2() {
		return y2;
	}
	public void setY2(Integer y2) {
		q("this.y2");
		this.y2 = y2;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		q("this.key");
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		q("this.value");
		this.value = value;
	}
	/*
	 * trace the usage of overloaded variables. This is a debug tool
	 */

	private void q(String name) {
		if (ovrvars.contains(name)) {
			throw new Error("multiple override for "+ name+":"+ovrvars);
		}
	}
	
}
