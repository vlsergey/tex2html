<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" version="5" encoding="utf-8" omit-xml-declaration="yes"
    doctype-system="about:legacy-compat" indent="yes" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/project">
    <html>
      <xsl:apply-templates select="command[@name='document']" />
    </html>
  </xsl:template>

  <xsl:template match="command[@name='document']">
    <body>
      <xsl:apply-templates select="content/node()" />
    </body>
  </xsl:template>

  <xsl:template match="command[@name='cite']">
    <a>
      <xsl:attribute name="href">
        <xsl:text>#</xsl:text>
        <xsl:apply-templates select="argument[@required='true']/text()" />
      </xsl:attribute>
      <xsl:text>[</xsl:text>
      <xsl:apply-templates select="argument[@required='true']/text()" />
      <xsl:text>]</xsl:text>
    </a>
  </xsl:template>

  <xsl:template match="command[@name='chapter']">
    <h1>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </h1>
  </xsl:template>

  <xsl:template match="command[@name='emph']">
    <em>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </em>
  </xsl:template>

  <xsl:template match="command[@name='href']">
    <a href="argument[@required='true'][0]/text()">
      <xsl:apply-templates select="argument[@required='true'][1]/node()" />
    </a>
  </xsl:template>

  <xsl:template match="command[@name='ref']">
    <a href="#{argument[@required='true']/text()}">
      <xsl:text>[</xsl:text>
      <xsl:value-of select="argument[@required='true']/text()" />
      <xsl:text>]</xsl:text>
    </a>
  </xsl:template>

  <xsl:template match="command[@name='index']">
    <a name="{generate-id(.)}">
      <xsl:comment>
        <xsl:apply-templates select="argument[@required='true']/text()" />
      </xsl:comment>
    </a>
  </xsl:template>

  <xsl:template match="command[@name='texttt']">
    <tt>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </tt>
  </xsl:template>

  <xsl:template match="tilda">
    <xsl:text>&#160;</xsl:text>
  </xsl:template>

</xsl:stylesheet>
