<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/html">
    <html>
      <head>
        <meta charset="utf-8" />
        <style type="text/css">
          <xsl:text><![CDATA[
span.nonbreaking-interword-space {
  white-space: nowrap;
}
]]></xsl:text>
        </style>
      </head>
      <xsl:apply-templates select="/html/body" />
    </html>
  </xsl:template>

</xsl:stylesheet>