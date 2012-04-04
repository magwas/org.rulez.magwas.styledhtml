<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:archimate="http://www.bolton.ac.uk/archimate">

	<xsl:output method="xml" version="1.0" encoding="utf-8" indent="no" omit-xml-declaration="yes"/>

<!--
	This export a csv for each of the objectClasses defined in the policy, if there is any member of them
-->
	<xsl:param name="policy" select="'policy.xml'"/>
	<xsl:param name="targetdir"/>


	<xsl:variable name="policyxml" select="document(concat($targetdir,'/',$policy))"/>
	<xsl:variable name="model" select="/"/>

	<xsl:template match="/">
		<article><title>Object list</title>
			<xsl:apply-templates select="$policyxml//objectClass" mode="csvexport"/>
		</article>
	</xsl:template>

	<xsl:template match="objectClass" mode="csvexport">
		<xsl:variable name="classlist" select="$model//*[local-name()=string(current()/@name)]"/>
		<xsl:variable name="proplist" select="property/@name"/>
		<xsl:if test="$classlist">
		<section><title><xsl:value-of select="@name"/></title>
			<anchor id="{@name}"/>
			<para>
				<link xlink:href="{@name}.csv" xrefstyle="what?">csv download</link>
			</para>
			<xsl:result-document href="{$targetdir}/{@name}.csv">id;<xsl:for-each select="$proplist"><xsl:value-of select="."/>;</xsl:for-each>
				<xsl:text>
</xsl:text>
				<xsl:for-each select="$classlist"><xsl:variable name="cur" select="."/><xsl:value-of select="@parentid"/>;<xsl:for-each select="$proplist"><xsl:value-of select="$cur/*[local-name()=current()]"/>;</xsl:for-each>
					<xsl:text>
</xsl:text>
				</xsl:for-each>
			</xsl:result-document>
			<table>
				<tr>
					<td>id</td>
					<xsl:for-each select="$proplist">
						<td><xsl:value-of select="."/></td>
					</xsl:for-each>
				</tr>
				<xsl:for-each select="$classlist">
					<xsl:variable name="cur" select="."/>
					<tr>
						<td>
						<xsl:value-of select="@parentid"/>
						</td>
						<xsl:for-each select="$proplist">
							<td>
								<xsl:value-of select="$cur/*[local-name()=current()]"/>
							</td>
						</xsl:for-each>
					</tr>
				</xsl:for-each>
			</table>
		</section>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet>
