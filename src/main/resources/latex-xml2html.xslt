<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="utf-8" indent="no" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/project">
    <html>
      <xsl:apply-templates select="./file/command[@name='document']" />
    </html>
  </xsl:template>

  <xsl:template match="file">
    <xsl:apply-templates select="./node()" />
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
      <xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:apply-templates
        select="argument[@required='true']/text()" /></xsl:attribute>
      <xsl:text>[</xsl:text>
      <xsl:apply-templates select="argument[@required='true']/text()" />
      <xsl:text>]</xsl:text>
    </a>
  </xsl:template>

  <xsl:template match="command[@name='chapter']">
    <h1>
      <xsl:text>Глава </xsl:text>
      <xsl:value-of select="@index" />
      <xsl:text>. </xsl:text>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </h1>
  </xsl:template>

  <xsl:template match="command[@name='figure']">
    <figure
      style="margin-left: auto; margin-right: auto; display: flex; flex-wrap: wrap; align-items: baseline; justify-content: center; width: fit-content; height: fit-content;">
      <xsl:apply-templates
        select="./content/*[not( name()='command' and (@name='caption' or @name='centering' or @name='label') )]" />
      <xsl:if test="./content/command[@name='caption']">
        <figcaption style="flex-basis: 100%; text-align: center;">
          <xsl:text>Рис. </xsl:text>
          <xsl:value-of select="@index" />
          <xsl:text> — </xsl:text>
          <xsl:apply-templates
            select="./content/command[@name='caption']/argument[@required='true'][1]/node()" />
        </figcaption>
      </xsl:if>
    </figure>
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
    <xsl:attribute name="lang"><xsl:apply-templates mode="language-to-code"
      select="./argument[@required='true'][position()=1]/text()" /></xsl:attribute>
  </xsl:template>

  <xsl:template match="command[@name='subcaptionbox'][./argument[@required='true'][2]/include-graphics]">
    <figure>
      <xsl:attribute name="style">
        <xsl:text>margin-left: 0; margin-right: 0; </xsl:text>
        <xsl:if test="./argument[@required='true'][2]/include-graphics/@width">
          <xsl:text>width: </xsl:text>
          <xsl:value-of select="./argument[@required='true'][2]/include-graphics/@width" />
          <xsl:text>;</xsl:text>
        </xsl:if>
      </xsl:attribute>
      <img src="{./argument[@required='true'][2]/include-graphics/@src}" style="width: 100%" />
      <figcaption>
        <xsl:text>(</xsl:text>
        <xsl:value-of select="@box-index" />
        <xsl:text>) </xsl:text>
        <xsl:apply-templates select="./argument[@required='true'][1]/node()" />
      </figcaption>
    </figure>
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
      <xsl:copy-of select="text()" />
      <xsl:text>$</xsl:text>
    </span>
  </xsl:template>

  <xsl:template match="item">
    <li>
      <xsl:apply-templates />
    </li>
  </xsl:template>

  <xsl:template match="itemize">
    <ul>
      <xsl:apply-templates />
    </ul>
  </xsl:template>

  <xsl:template match="tabular">
    <xsl:variable name="paddingLeftStyle" select="'padding-left: 6pt;'" />
    <xsl:variable name="paddingRightStyle" select="'padding-right: 6pt;'" />

    <table style="border-collapse: collapse;">
      <xsl:for-each select="./columns">
        <colgroup>
          <xsl:for-each select="./column">
            <col>
              <xsl:attribute name="style">
                <xsl:for-each select="@*[name()='border-left' or name()='border-right']">
                  <xsl:value-of select="concat(name(), ': ', ., ';')" />
                </xsl:for-each>
              </xsl:attribute>
            </col>
          </xsl:for-each>
        </colgroup>
      </xsl:for-each>
      <xsl:for-each select="./row">
        <tr>
          <xsl:attribute name="style">
            <xsl:for-each select="@*[name()='border-top' or name()='border-bottom']">
              <xsl:value-of select="concat(name(), ': ', ., ';')" />
            </xsl:for-each>
          </xsl:attribute>

          <xsl:for-each select="./cell">
            <td>
              <xsl:variable name="cellPos" select="position()" />
              <xsl:attribute name="style">
                <xsl:value-of select="$paddingLeftStyle" />
                <xsl:value-of select="$paddingRightStyle" />
                <xsl:for-each select="../../columns/column[$cellPos]/@*[name()='text-align']">
                    <xsl:variable name="attrName" select="name()" />
                    <xsl:value-of select="concat(name(), ': ', ., ';')" />
                </xsl:for-each>
              </xsl:attribute>
              <xsl:apply-templates />
            </td>
          </xsl:for-each>
        </tr>
      </xsl:for-each>
    </table>
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