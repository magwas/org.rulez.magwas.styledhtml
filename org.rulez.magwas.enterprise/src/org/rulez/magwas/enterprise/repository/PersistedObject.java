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
	private String documentation=null, linetext = null;
	private String type=null;
	private Integer subtype=null, alignment=null, textposition=null, linewidth=null;
	private String source=null,target=null, element=null; //these are references to other objects
	private String font=null, fontcolor=null,fillcolor=null; //graphical properties
	private Integer x1=null, x2=null, y1=null, y2=null;
	private String key=null, value=null;
	private Float weight;

	private Connection con;
	private Integer versionid;
	private PersistedObject parent;

	private List<String> ovrvars;
	
	private List<String> dependents = new ArrayList<String>();//List of ids of objects depending on this
	private Boolean persisted=false;
	static HashMap<String, PersistedObject> objdir = null;
	private List<PersistedObject> properties = new ArrayList<PersistedObject>();
	
	public PersistedObject(Connection connection, Integer version, EObject node, PersistedObject parent) throws SQLException {
		super();
		if (null == objdir) {
			objdir = new HashMap<String, PersistedObject>();
		}
		ovrvars = new ArrayList<String>();
		con=connection;
		versionid=version;
		if (null == parent) {
			parent = this;
		}
		this.parent=parent;
		parseNode(node);
	}
	
	public PersistedObject addNode(EObject node, PersistedObject parent) throws SQLException {
		if (node instanceof IIdentifier) {
			if (objdir.containsKey(((IIdentifier) node).getId())) {
				return objdir.get(((IIdentifier) node).getId());
			}			
		}
		return new PersistedObject(con, versionid, node,parent);
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

	public void persist() throws SQLException {
		if (persisted) {
			//System.out.println("already persisted "+this);
			return;
		}
		if(this != parent) {
			parent.persist();
		}
		//System.out.println("persisting "+this);
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
			assert(key==null);
			assert(value==null);
			assert(x1==null);
			assert(y1==null);
			assert(x2==null);
			assert(y2==null);
			assert(weight==null);
			PreparedStatement psInsertObject = con.prepareStatement(
					"insert into object_view " +
					"(version, id, parent, name, documentation, type, source, target, element, font, fontcolor, subtype, fillcolor, linetext, alignment, textposition, linewidth)" +
					" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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
			setSQLInt(psInsertObject,12,subtype);
			psInsertObject.setString(13, fillcolor);
			psInsertObject.setString(14, linetext);
			setSQLInt(psInsertObject,15, alignment);
			setSQLInt(psInsertObject,16, textposition);
			setSQLInt(psInsertObject,17, linewidth);
			//System.out.println("executing "+psInsertObject+";");
			psInsertObject.execute();
			psInsertObject.close();
		} else {
			assert(name==null);
			assert(documentation==null);
			assert(source==null);
			assert(target==null);
			assert(element==null);
			assert(font==null);
			assert(fontcolor==null);
			assert(subtype==null);
			assert(fillcolor==null);
			assert(linetext==null);
			assert(alignment==null);
			assert(textposition==null);
			assert(linewidth==null);
			//property
			PreparedStatement psInsertObject = con.prepareStatement(
					"insert into property_view " +
					"(version, parent, type, key, value, x1, y1, x2, y2, weight)" +
					"values (?,?,?,?,?,?,?,?,?,?)");
			psInsertObject.setInt(1, versionid);
			psInsertObject.setString(2, parent.getId());
			psInsertObject.setString(3, type);
			psInsertObject.setString(4, key);
			psInsertObject.setString(5, value);
			setSQLInt(psInsertObject,6,x1);
			setSQLInt(psInsertObject,7,y1);
			setSQLInt(psInsertObject,8,x2);
			setSQLInt(psInsertObject,9,y2);
			setSQLFloat(psInsertObject,10,weight);
			//System.out.println("executing "+psInsertObject+";");
			psInsertObject.execute();
			psInsertObject.close();
		}
	}

	public void ready() {
		//System.out.println("readying"+this);
		if (null == id){
			assert(parent != this);
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
			setAlignment(((IDiagramModel) node).getConnectionRouterType());
		}

		if(node instanceof IDiagramModelArchimateConnection) {
			setElement(((IDiagramModelArchimateConnection) node).getRelationship().getId());
		}

		if(node instanceof IDiagramModelArchimateObject) {
			setElement(((IDiagramModelArchimateObject) node).getArchimateElement().getId());
			setSubtype(((IDiagramModelArchimateObject) node).getType());
		}
		
		if(node instanceof IDiagramModelBendpoint) {
			setWeight(((IDiagramModelBendpoint) node).getWeight());
			setX1(((IDiagramModelBendpoint) node).getStartX());
			setY1(((IDiagramModelBendpoint) node).getStartY());
			setX2(((IDiagramModelBendpoint) node).getEndX());
			setY2(((IDiagramModelBendpoint) node).getEndY());			
		}
		
		if(node instanceof IFontAttribute) {
			setFont(((IFontAttribute) node).getFont());
			setFontcolor(((IFontAttribute) node).getFontColor());
			setAlignment(((IFontAttribute) node).getTextAlignment());			
		}
		
		if(node instanceof IDiagramModelObject) {
			IBounds bounds = ((IDiagramModelObject) node).getBounds();
			//System.out.println(bounds.eClass().getName());
			addNode(bounds,this);
			EList<IDiagramModelConnection> scs = ((IDiagramModelObject) node).getSourceConnections();
			for (IDiagramModelConnection sc: scs) {
				addNode( sc, this);
			}
			EList<IDiagramModelConnection> tcs = ((IDiagramModelObject) node).getTargetConnections();
			for (IDiagramModelConnection tc : tcs) {
				addNode( tc, this);
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
			setLinetext(((IDiagramModelConnection) node).getText());
			setTextposition(((IDiagramModelConnection) node).getTextPosition());
			setSource(((IDiagramModelConnection) node).getSource().getId());
			setTarget(((IDiagramModelConnection) node).getTarget().getId());
			EList<IDiagramModelBendpoint> bendpoints = ((IDiagramModelConnection) node).getBendpoints();
			for (IDiagramModelBendpoint bp: bendpoints) {
				addNode( bp, this);
			}
			setLinewidth(((IDiagramModelConnection) node).getLineWidth());
			setFillcolor(((IDiagramModelConnection) node).getLineColor());
			String t = ((IDiagramModelConnection) node).getType();
			assert(t == null);
			//setType(t);
		}
		
		if(node instanceof IProperties) {
			EList<IProperty> pl = ((IProperties) node).getProperties();
			for (IProperty prop : pl) {
				addNode( prop, this);
			}
		}
		if(node instanceof IDiagramModelContainer) {
			EList<IDiagramModelObject> children = ((IDiagramModelContainer) node).getChildren();
			for (IDiagramModelObject child : children) {
				addNode( child, this);
			}
		}
	
		if(node instanceof IFolderContainer) {
			EList<IFolder> fl = ((IFolderContainer) node).getFolders();
			for (IFolder folder : fl) {
				addNode( folder, this);
			}
		}
		if(node instanceof IFolder) {
			EList<EObject> fl = ((IFolder) node).getElements();
			for (EObject kid : fl) {
				addNode( kid, this);
			}
			id = ((IFolder) node).getType().getName(); //This is intentional, to make merging models easier.
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
	public void setLinetext(String documentation) {
		q("this.linetext");
		this.linetext = documentation;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		//System.out.println("type="+type);
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
	public void setWeight(Float value) {
		q("this.weight");
		this.weight = value;
	}
	
	public void setAlignment(Integer alignment) {
		q("this.alignment");
		this.alignment = alignment;
	}
	
	public void setTextposition(Integer alignment) {
		q("this.textposition");
		this.textposition = alignment;
	}
	
	public void setLinewidth(Integer alignment) {
		q("this.linewidth");
		this.linewidth = alignment;
	}
	/*
	 * trace the usage of overloaded variables. This is a debug tool
	 */

	private void q(String name) {
		/*
		Throwable t = new Throwable();
		StackTraceElement[] elements = t.getStackTrace();
		int callerline = elements[2].getLineNumber();
		System.out.println(name+":"+callerline);
		*/

		if (ovrvars.contains(name)) {
			throw new Error("multiple override for "+ name+":"+ovrvars);
		}
		ovrvars.add(name);
	}


	@Override
	public String toString() {
		String props = "( ";
		for (PersistedObject p : properties) {
			props = props + p.getType()+":"+p.getKey() + " ";
		}
		props = props + ")";
		return "PersistedObject [id=" + id + ", name=" + name +", key="+key+ ", type="
				+ type + ", subtype=" + subtype + ", parent=" + parent.getId()
				+ ", dependents=" + dependents + ", properties="+ props + "]";
	}
	
}
