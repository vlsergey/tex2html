<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="tiny">
    <span class="tiny">
      <xsl:apply-templates select="@*|node()" />
    </span>
  </xsl:template>

  <xsl:template match="/html/head">
    <head>
      <xsl:apply-templates select="@*|node()" />
      <style type="text/css"><![CDATA[ 
span.tiny {
  font-size: 50%
}
]]></style>
    </head>
  </xsl:template>

</xsl:stylesheet>