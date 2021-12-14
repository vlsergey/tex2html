<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" version="5" encoding="utf-8"
    omit-xml-declaration="yes" doctype-system="about:legacy-compat"
    indent="yes" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/project">
    <html>
      <xsl:apply-templates
        select="command[@name='document']" />
    </html>
  </xsl:template>

  <xsl:template match="command[@name='document']">
    <body>
      <xsl:apply-templates select="content/node()" />
    </body>
  </xsl:template>

</xsl:stylesheet>
