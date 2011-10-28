<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:archimate="http://www.bolton.ac.uk/archimate" xmlns:set="http://exslt.org/sets" xmlns:fn="http://www.w3.org/2005/xpath-functions">

<xsl:template name="propertysql">
    <property>
        <parent><xsl:value-of select="../@id"/></parent>
        <key><xsl:value-of select="./@key"/></key>
        <value><xsl:value-of select="./@value"/></value>
    </property>
</xsl:template>
<xsl:template name="bendpointsql">
    <bendpoint>
        <parent><xsl:value-of select="../@id"/></parent>
        <startx><xsl:value-of select="./@startx"/></startx>
        <endx><xsl:value-of select="./@endx"/></endx>
        <starty><xsl:value-of select="./@starty"/></starty>
        <endy><xsl:value-of select="./@endy"/></endy>
    </bendpoint>
</xsl:template>
<xsl:template name="boundsql">
    <bounds>
        <parent><xsl:value-of select="../@id"/></parent>
        <x><xsl:value-of select="./@x"/></x>
        <y><xsl:value-of select="./@y"/></y>
        <width><xsl:value-of select="./@width"/></width>
        <height><xsl:value-of select="./@height"/></height>
    </bounds>
</xsl:template>
<xsl:template name="childsql">
    <object>
        <parent><xsl:value-of select="../@id"/></parent>
        <id><xsl:value-of select="./@id"/></id>
        <name><xsl:value-of select="./@name"/></name>
        <documentation><xsl:value-of select="./content"/></documentation>
        <type><xsl:value-of select="./@xsi:type"/></type>
        <element><xsl:value-of select="../@fillColor"/></element>
    </object>
        <xsl:for-each select="property">
            <xsl:call-template name="propertysql"/>
        </xsl:for-each>
        <xsl:for-each select="child">
            <xsl:call-template name="childsql"/>
        </xsl:for-each>
        <xsl:for-each select="bounds">
            <xsl:call-template name="boundsql"/>
        </xsl:for-each>
        <xsl:for-each select="sourceConnection">
            <xsl:call-template name="elementsql"/>
        </xsl:for-each>
</xsl:template>
<xsl:template name="elementsql">
    <object>
        <id><xsl:value-of select="./@id"/></id>
        <name><xsl:value-of select="./@name"/></name>
        <documentation><xsl:value-of select="./documentation"/></documentation>
        <type><xsl:value-of select="./@xsi:type"/></type>
        <parent><xsl:value-of select="../@id"/></parent>
        <source><xsl:value-of select="./@source"/></source>
        <target><xsl:value-of select="./@target"/></target>
    </object>
        <xsl:for-each select="property">
            <xsl:call-template name="propertysql"/>
        </xsl:for-each>
        <xsl:for-each select="child">
            <xsl:call-template name="childsql"/>
        </xsl:for-each>
        <xsl:for-each select="bendpoint">
            <xsl:call-template name="bendpointsql"/>
        </xsl:for-each>
</xsl:template>
<xsl:template name="foldersql">
    <object>
        <id><xsl:value-of select="./@id"/></id>
        <name><xsl:value-of select="./@name"/></name>
        <documentation><xsl:value-of select="./documentation"/></documentation>
        <type>folder</type>
        <parent><xsl:value-of select="../@id"/></parent>
    </object>
        <xsl:for-each select="property">
            <xsl:call-template name="propertysql"/>
        </xsl:for-each>
        <xsl:for-each select="folder">
            <xsl:call-template name="foldersql"/>
        </xsl:for-each>
        <xsl:for-each select="element">
            <xsl:call-template name="elementsql"/>
        </xsl:for-each>
</xsl:template>
    <xsl:template match="/">
        <xml>
        <object>
        <id><xsl:value-of select="archimate:model/@id"/></id>
        <name><xsl:value-of select="archimate:model/@name"/></name>
        <documentation><xsl:value-of select="archimate:model/purpose"/></documentation>
        <type>model</type>
        </object>
        <xsl:for-each select="/archimate:model/folder">
            <xsl:call-template name="foldersql"/>
        </xsl:for-each>
        </xml>
    </xsl:template>
    
</xsl:stylesheet>
