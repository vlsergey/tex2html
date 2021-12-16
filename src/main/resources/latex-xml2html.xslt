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
      <script>
        <xsl:text>
MathJax = {
  tex: {
    processHtmlClass: 'mathjax', 
    inlineMath: [['$', '$']]
  },
  svg: {
    fontCache: 'global'
  }
};</xsl:text>
      </script>
      <script src="https://polyfill.io/v3/polyfill.min.js?features=es6" />
      <script id="MathJax-script" async="yes" src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js" />
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

  <xsl:template match="command[@name='foreignlanguage']">
    <span>
      <xsl:attribute name="lang">
        <xsl:apply-templates mode="language-to-code"
        select="./argument[@required='true'][position()=1]/text()" />
      </xsl:attribute>
      <xsl:apply-templates select="./argument[@required='true'][position()=2]/node()" />
    </span>
  </xsl:template>

  <xsl:template match="command[@name='href']">
    <a href="{argument[@required='true'][1]/text()}">
      <xsl:apply-templates select="argument[@required='true'][2]/node()" />
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

  <xsl:template match="command[@name='selectlanguage']">
    <xsl:attribute name="lang">
        <xsl:apply-templates mode="language-to-code"
      select="./argument[@required='true'][position()=1]/text()" />
      </xsl:attribute>
  </xsl:template>

  <xsl:template match="command[@name='textit']">
    <i>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </i>
  </xsl:template>

  <xsl:template match="command[@name='texttt']">
    <tt>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </tt>
  </xsl:template>

  <xsl:template match="command[@name='sloppy' or @name='usepackage']" />

  <xsl:template match="tilda">
    <xsl:text>&#160;</xsl:text>
  </xsl:template>

  <xsl:template match="inline-formula">
    <span class="mathjax">
      <xsl:text>$</xsl:text>
      <xsl:copy-of select="text()"/>
      <xsl:text>$</xsl:text>
    </span>
  </xsl:template>

  <xsl:template match="text()" mode='language-to-code'>
    <xsl:choose>
      <xsl:when test=".='english'">
        <xsl:text>en</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="." />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
