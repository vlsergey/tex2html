<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" version="5" encoding="utf-8" doctype-system="about:legacy-compat"
    omit-xml-declaration="yes" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/html">
    <html>
      <head>
        <meta charset="utf-8" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous" />
        <style type="text/css">
          <xsl:text><![CDATA[
div.mathjax {
  padding-top: .5em;
  padding-bottom: .5em;
}

span.nonbreaking-interword-space {
  white-space: nowrap;
}

div.toc-item-h2 {
  padding-left: 2em;
}
div.toc-item-h3 {
  padding-left: 4em;
}
div.toc-item-h4 {
  padding-left: 6em;
}
div.toc-item-h5 {
  padding-left: 8em;
}
div.toc-item-h6 {
  padding-left: 10em;
}
]]></xsl:text>
        </style>
      </head>
      <xsl:apply-templates select="/html/body" />
    </html>
  </xsl:template>

  <xsl:template match="/html/body">
    <body>
      <div class="container">
        <xsl:apply-templates select="./node()" />
      </div>
      <script>
        <xsl:text>
window.MathJax = {
  loader: {
    load: ['[tex]/tagformat'],
  },
  options: {
    processHtmlClass: 'mathjax'
  },
  tex: {
    packages: {
      '[+]': ['base', 'ams', 'physics', 'textcomp']
    },
    inlineMath: [['$', '$']],
  },
};
</xsl:text>
      </script>
      <script src="https://polyfill.io/v3/polyfill.min.js?features=es6" />
      <script id="MathJax-script" async="yes" src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js" />
    </body>
  </xsl:template>

  <xsl:template match="table-of-contents">
    <h1>
      <xsl:value-of select="@localized-label" />
    </h1>
    <xsl:for-each select="/html/body//*[name()='h1' or name()='h2' or name()='h3' or name()='h4']">
      <div class="toc-item-{name()}">
        <a>
          <xsl:attribute name="href">
            <xsl:text>#</xsl:text>
            <xsl:value-of select="@id" />
          </xsl:attribute>
          <xsl:apply-templates select="./node()" />
        </a>
      </div>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="tex-formula-block">
    <div class="mathjax" style="text-align: center;">
      <xsl:text>$$</xsl:text>
      <xsl:apply-templates select="." mode="string-join-mode" />
      <xsl:text>$$</xsl:text>
    </div>
  </xsl:template>

  <xsl:template match="tex-formula-inline">
    <span class="mathjax">
      <xsl:value-of select="concat('$', text(), '$')" />
    </span>
  </xsl:template>

  <xsl:template match="tex-formula-multline">
    <div class="mathjax" style="text-align: center;">
      <xsl:text>$$\begin{eqnarray}</xsl:text>
      <xsl:apply-templates select="." mode="string-join-mode" />
      <xsl:text>\end{eqnarray}$$</xsl:text>
    </div>
  </xsl:template>

</xsl:stylesheet>