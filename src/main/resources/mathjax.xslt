<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/html/head">
    <head>
      <xsl:apply-templates select="@*|node()" />
      <style type="text/css"><![CDATA[ 
div.mathjax {
  padding-top: .5em;
  padding-bottom: .5em;
}
]]></style>
    </head>
  </xsl:template>

  <xsl:template match="/html/body">
    <body>
      <xsl:apply-templates select="@*|node()" />
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