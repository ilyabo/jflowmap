<?xml version="1.0" encoding="utf-8" ?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="filterMinWeight" select="5000"/>
  
    <xsl:output method="xml" indent="yes" name="xml"/>
    
    <xsl:template match="@*|node()" priority="2">
      <xsl:choose>

        <xsl:when test="local-name()='edge' and  *[local-name()='data']/. &lt; $filterMinWeight" />

        <xsl:when test="local-name()='node'   
          and not (../*[local-name()='edge'
          and (*[local-name()='data']/. &gt;= $filterMinWeight) 
          and (@source=current()/@id or @target=current()/@id)])
          "/>

        <xsl:otherwise>
          <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
          </xsl:copy>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="in_edge">
       <xsl:value-of select="." />
    </xsl:template>

</xsl:stylesheet>
