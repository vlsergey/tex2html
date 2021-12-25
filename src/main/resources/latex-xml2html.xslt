<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="utf-8" indent="no" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/project">
    <html>
      <head>
        <meta charset="utf-8" />
        <style type="text/css">
          <xsl:text><![CDATA[
div.mathjax {
  padding-top: .5em;
  padding-bottom: .5em;
}
span.nonbreaking-interword-space {
  white-space: nowrap;
}
]]></xsl:text>
        </style>
      </head>
      <xsl:apply-templates select="./file/command[@name='document']" />
    </html>
  </xsl:template>

  <xsl:template match="file">
    <xsl:apply-templates select="./node()" />
  </xsl:template>

  <xsl:template match="command[@name='document']">
    <body>
      <xsl:if test="@language-code">
        <xsl:attribute name="lang"><xsl:value-of select="@lang-code" /></xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="content/node()" />
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

  <xsl:template match="command[@name='chapter']">
    <h1>
      <xsl:if test="@language-code">
        <xsl:attribute name="lang"><xsl:value-of select="@language-code" /></xsl:attribute>
      </xsl:if>
      <xsl:value-of select="concat(@localized-label, ' ', @index, '. ')" />
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </h1>
  </xsl:template>

  <xsl:template match="command[@name='section']">
    <h2>
      <xsl:apply-templates select="." mode="section-header-content" />
    </h2>
  </xsl:template>
  <xsl:template match="command[@name='subsection']">
    <h3>
      <xsl:apply-templates select="." mode="section-header-content" />
    </h3>
  </xsl:template>
  <xsl:template match="command[@name='subsubsection']">
    <h4>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </h4>
  </xsl:template>

  <xsl:template match="command" mode="section-header-content">
    <xsl:value-of select="concat(@index, '. ')" />
    <xsl:apply-templates select="./argument[@required='true']/node()" />
  </xsl:template>

  <xsl:template match="command[@name='center']">
    <div style="display: flex; align-items: center; justify-content: center;">
      <xsl:apply-templates select="content/node()" />
    </div>
  </xsl:template>

  <xsl:template match="command[@name='figure' or @name='table']">
    <figure>
      <xsl:attribute name="style">
        <xsl:text>margin-left: auto; margin-right: auto; display: flex; flex-wrap: wrap; align-items: baseline; justify-content: center; width: fit-content; height: fit-content; </xsl:text>
        <xsl:if test="content/include-graphics/@width">
          <xsl:text>width: </xsl:text>
          <xsl:value-of select="content/include-graphics/@width" />
          <xsl:text>;</xsl:text>
        </xsl:if>
      </xsl:attribute>
      <xsl:apply-templates select="@label" mode="own-label" />
      <xsl:apply-templates
        select="./content/*[not( name()='command' and (@name='caption' or @name='centering' or @name='label') )]" />
      <xsl:if test="./content/command[@name='caption']">
        <figcaption style="flex-basis: 100%; text-align: center;">
          <xsl:value-of select="concat(@localized-label, ' ', @index, ' — ')" />
          <xsl:apply-templates
            select="./content/command[@name='caption']/argument[@required='true'][1]/node()" />
        </figcaption>
      </xsl:if>
    </figure>
  </xsl:template>

  <xsl:template match="command[@name='label']">
    <a name="#{argument[@required='true']/text()}" />
  </xsl:template>

  <xsl:template match="command[@name='emph']">
    <em>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </em>
  </xsl:template>

  <xsl:template match="command[@name='foreignlanguage']">
    <span>
      <xsl:attribute name="lang"><xsl:apply-templates mode="language-to-code"
        select="./argument[@required='true'][position()=1]/text()" /></xsl:attribute>
      <xsl:apply-templates select="./argument[@required='true'][position()=2]/node()" />
    </span>
  </xsl:template>

  <xsl:template match="command[@name='href']">
    <a href="{argument[@required='true'][1]/text()}">
      <xsl:apply-templates select="argument[@required='true'][2]/node()" />
    </a>
  </xsl:template>

  <xsl:template match="command[@name='index']">
    <a name="{generate-id(.)}">
      <xsl:comment>
        <xsl:apply-templates select="argument[@required='true']/text()" />
      </xsl:comment>
    </a>
  </xsl:template>

  <xsl:template match="command[@name='No']">
    <xsl:text>№</xsl:text>
  </xsl:template>

  <xsl:template match="command[@name='quote']">
    <blockquote>
      <xsl:apply-templates select="content/node()" />
    </blockquote>
  </xsl:template>

  <xsl:template match="command[@name='ref']">
    <xsl:variable name="labelName" select="argument[@required='true']/text()" />
    <a href="#{$labelName}">
      <xsl:choose>
        <!-- TODO: replace with key-index -->
        <xsl:when test="//command[@label=$labelName][@index]">
          <xsl:value-of select="//command[@label=$labelName]/@index" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat('[', $labelName, ']')" />
        </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>

  <xsl:template match="command[@name='resizebox']">
    <!-- resizebox is not supported so far -->
    <xsl:apply-templates select="argument[@required='true'][3]/node()" />
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
      <xsl:apply-templates select="@label" mode="own-label" />
      <img src="{./argument[@required='true'][2]/include-graphics/@src}" style="width: 100%" />
      <figcaption>
        <xsl:value-of select="concat('(', @box-index, ') ')" />
        <xsl:apply-templates select="./argument[@required='true'][1]/node()[not (@name='label')]" />
      </figcaption>
    </figure>
  </xsl:template>

  <xsl:template match="command[@name='textbf']">
    <b>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </b>
  </xsl:template>

  <xsl:template match="command[@name='textit']">
    <i>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </i>
  </xsl:template>

  <xsl:template match="command[@name='textsc']">
    <span style="font-variant: small-caps;">
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </span>
  </xsl:template>

  <xsl:template match="command[@name='texttt']">
    <tt>
      <xsl:apply-templates select="./argument[@required='true']/node()" />
    </tt>
  </xsl:template>

  <xsl:template match="command[@name='sloppy' or @name='usepackage']" />
  <xsl:template match="command[@name='sloppypar']">
    <xsl:apply-templates select="content/node()" />
  </xsl:template>

  <xsl:key name="printbibliography" match="//printbibliography/*" use="@name" />
  <xsl:template match="cite">
    <xsl:choose>
      <xsl:when test="count(ref) &lt; 2">
        <xsl:variable name="name" select="ref/@name" />
        <xsl:variable name="referencedItem" select="key('printbibliography', $name)" />
        <a href="#{$name}">
          <xsl:text>[</xsl:text>
          <xsl:choose>
            <xsl:when test="$referencedItem/@index">
              <xsl:value-of select="$referencedItem/@index" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$name" />
            </xsl:otherwise>
          </xsl:choose>
          <xsl:text>]</xsl:text>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>[</xsl:text>
        <xsl:for-each select="ref">
          <xsl:sort select="key('printbibliography', @name)/@index" data-type="number" />
          <xsl:variable name="name" select="@name" />
          <xsl:variable name="referencedItem" select="key('printbibliography', @name)" />
          <a href="#{$name}">
            <xsl:choose>
              <xsl:when test="$referencedItem/@index">
                <xsl:value-of select="$referencedItem/@index" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$name" />
              </xsl:otherwise>
            </xsl:choose>
          </a>
          <xsl:if test="position() != last()">
            <xsl:text>, </xsl:text>
          </xsl:if>
        </xsl:for-each>
        <xsl:text>]</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="enumerate">
    <ol>
      <xsl:apply-templates />
    </ol>
  </xsl:template>

  <xsl:template match="include-graphics">
    <img src="{@src}" style="width: 100%" />
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

  <xsl:template match="nonbreaking-fixed-size-space">
    <xsl:text>&#160;</xsl:text>
  </xsl:template>

  <xsl:template match="nonbreaking-interword-space">
    <span class="nonbreaking-interword-space">
      <xsl:text> </xsl:text>
    </span>
  </xsl:template>

  <xsl:template match="printbibliography">
    <h1>Литература</h1>
    <ol>
      <xsl:for-each select="*">
        <li>
          <a name="{@name}" />
          <xsl:copy-of select="node()" />
        </li>
      </xsl:for-each>
    </ol>
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
              <xsl:if test="@colspan">
                <xsl:attribute name="colspan">
                  <xsl:value-of select="@colspan" />
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="@rowspan">
                <xsl:attribute name="rowspan">
                  <xsl:value-of select="@rowspan" />
                </xsl:attribute>
              </xsl:if>
              <xsl:attribute name="style">
                <xsl:value-of select="$paddingLeftStyle" />
                <xsl:value-of select="$paddingRightStyle" />
                <xsl:for-each select="../../columns/column[$cellPos]/@*[name()='text-align']">
                    <xsl:variable name="attrName" select="name()" />
                    <xsl:value-of select="concat(name(), ': ', ., ';')" />
                </xsl:for-each>
                <xsl:text>white-space: nowrap;</xsl:text>
              </xsl:attribute>
              <xsl:apply-templates />
            </td>
          </xsl:for-each>
        </tr>
      </xsl:for-each>
    </table>
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

  <xsl:template match="verbatim">
    <pre>
      <xsl:value-of select="text()" />
    </pre>
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

  <xsl:template match="@label" mode="own-label">
    <a name="{.}" style="vertical-align: top; align-self: start;" />
  </xsl:template>

  <xsl:template match="*" mode="string-join-mode">
    <xsl:apply-templates select="child::node()" mode="string-join-mode" />
  </xsl:template>
  <xsl:template match="text()" mode="string-join-mode">
    <xsl:value-of select="." />
  </xsl:template>
</xsl:stylesheet>