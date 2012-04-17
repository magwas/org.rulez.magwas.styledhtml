<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:archimate="http://www.bolton.ac.uk/archimate">

	<xsl:output method="xml" version="1.0" encoding="utf-8" indent="yes" omit-xml-declaration="no"/>

<!--
//archimate:*[@id=//archimate:ArchimateDiagramModel[property[@key='Template']]//archimate:Connection/@relationship]
//archimate:*[@id=//archimate:ArchimateDiagramModel[property[@key='Template']]//archimate:DiagramObject/@archimateElement]
//archimate:ArchimateDiagramModel[property[@key='Template']]//archimate:DiagramObject
-->
	<xsl:variable name="templates" select="//archimate:ArchimateDiagramModel[property[@key='Template']]"/>
	<xsl:variable name="objects" select="//archimate:*[@id=$templates//archimate:DiagramObject/@archimateElement]"/>
	<xsl:variable name="conns" select="//archimate:*[@id=$templates//archimate:Connection/@relationship]"/>
	<xsl:variable name="directions" select="tokenize('source target source',' ')"/>

	<xsl:template match="/">
			<policy name="Generated Policy (new style)">
				<xsl:apply-templates select="$objects" mode="newpolicy"/>
			</policy>
	</xsl:template>
	
	<xsl:template match="archimate:*[@id=$objects/@id]" mode="newpolicy">
		<objectClass name="{@name}" abstract="{property[@key='abstract']}">
			<!--<this><xsl:copy-of select="."/></this>-->
			<description>
				<xsl:copy-of select="documentation"/>
				<xsl:if test="'true' = property[@key='abstract']">
 					<p>This is an abstract class, do not instantiate it in a model</p>
 				</xsl:if>
			</description>
			<xsl:variable name="parent" select="$objects[@id=$conns['archimate:SpecialisationRelationship' = name() and @source = current()/@id]/@target]"/>
			<xsl:choose>
				<xsl:when test="$parent">
					<ancestor class="{$parent/@name}"/>
				</xsl:when>
				<xsl:otherwise>
					<ancestor class="{name()}"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="$conns[@source = current()/@id]" mode="newpolicy">
				<xsl:with-param name="direction" select="1"/><!-- 1=source -->
			</xsl:apply-templates>
			<xsl:apply-templates select="$conns[@target = current()/@id]" mode="newpolicy">
				<xsl:with-param name="direction" select="2"/><!-- 2=target -->
			</xsl:apply-templates>
		</objectClass>
	</xsl:template>

	<xsl:template match="archimate:*[@id=$conns/@id]" mode="newpolicy">
			<xsl:param name="direction"/>
			<xsl:variable name="targetobj">
				<xsl:choose>
					<xsl:when test="1 = $direction">
						<xsl:copy-of select="$objects[@id=current()/@target]"/>
					</xsl:when>
					<xsl:when test="2 = $direction">
						<xsl:copy-of select="$objects[@id=current()/@source]"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:message terminate="yes">Internal error: cannot figure out target for <xsl:copy-of select="."/></xsl:message>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="attname">
				<xsl:choose>
					<xsl:when test="attributes/name">
						<xsl:value-of select="tokenize(attributes/name,'/')[$direction]"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$targetobj/*/@name"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="type">
				<xsl:choose>
					<xsl:when test="attributes/type">
						<xsl:value-of select="tokenize(attributes/type,'/')[$direction]"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="'xs:string'"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="structural" select="tokenize(attributes/structural,'/')[$direction]"/>
			<xsl:variable name="cardinality" select="tokenize(attributes/cardinality,'/')[$direction]"/>
			<xsl:variable name="minOccurs" select="tokenize($cardinality,',')[1]"/>
			<xsl:variable name="maxOccurs" select="tokenize($cardinality,',')[2]"/>


			<property name="{$attname}" type="{$type}" minOccurs="{$minOccurs}" maxOccurs="{$maxOccurs}" structural="{$structural}" >
				<description><xsl:copy-of select="documentation"/></description>
				<default order="0"
					select="//{$targetobj/*/name()}[@id=//{name()}[@{$directions[$direction]}=$id]/@{$directions[$direction+1]}]/@name"
				multi="true">
					<description/>
				</default>

			</property>
	</xsl:template>

<!--
	<xsl:template match="default" mode="policy">
			<default order="{order}" select="{select}" multi="{multi}">
			<description><xsl:copy-of select="../documentation"/></description>
			</default>
	</xsl:template>
-->

</xsl:stylesheet>
