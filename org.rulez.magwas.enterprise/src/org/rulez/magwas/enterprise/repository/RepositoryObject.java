package org.rulez.magwas.enterprise.repository;

import java.io.Serializable;
import java.util.ArrayList;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "object")
public class RepositoryObject {
	@Id
	String id;
	String version;	
	String parent;
	String name;
	String documentation;
	String type;
	String source;
	String target;
	String element;
	String font;
	String fontcolor;
	String textalignment;
	String fillcolor;
	
	public RepositoryObject() {

	}

	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getVersion() {
		return version;
	}



	public void setVersion(String version) {
		this.version = version;
	}



	/**
	 * @return the parent
	 */
	public String getParent() {
		return parent;
	}


	/**
	 * @param parent the parent to set
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the documentation
	 */
	public String getDocumentation() {
		return documentation;
	}

	/**
	 * @param documentation the documentation to set
	 */
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
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
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the element
	 */
	public String getElement() {
		return element;
	}

	/**
	 * @param element the element to set
	 */
	public void setElement(String element) {
		this.element = element;
	}

	/**
	 * @return the font
	 */
	public String getFont() {
		return font;
	}

	/**
	 * @param font the font to set
	 */
	public void setFont(String font) {
		this.font = font;
	}

	/**
	 * @return the fontcolor
	 */
	public String getFontcolor() {
		return fontcolor;
	}

	/**
	 * @param fontcolor the fontcolor to set
	 */
	public void setFontcolor(String fontcolor) {
		this.fontcolor = fontcolor;
	}

	/**
	 * @return the textalignment
	 */
	public String getTextalignment() {
		return textalignment;
	}

	/**
	 * @param textalignment the textalignment to set
	 */
	public void setTextalignment(String textalignment) {
		this.textalignment = textalignment;
	}

	/**
	 * @return the fillcolor
	 */
	public String getFillcolor() {
		return fillcolor;
	}

	/**
	 * @param fillcolor the fillcolor to set
	 */
	public void setFillcolor(String fillcolor) {
		this.fillcolor = fillcolor;
	}


}
