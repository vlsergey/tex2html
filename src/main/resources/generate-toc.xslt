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
]]></style>
    </head>
  </xsl:template>

  <xsl:template match="add-contents-line">
    <a name="{@id}" />
  </xsl:template>

  <xsl:template match="table-of-contents">
    <h1 id="table-of-contents">
      <xsl:value-of select="@localized-label" />
    </h1>
    <xsl:for-each
      select="/html/body//*[name()='h1' or name()='h2' or name()='h3' or name()='h4' or name()='add-contents-line'][@data-command-name != 'chapter*']">
      <xsl:choose>
        <xsl:when test="name()='add-contents-line'">
          <div class="toc-item-h{@level}">
            <a>
              <xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="@id" /></xsl:attribute>
              <xsl:apply-templates select="./node()" />
            </a>
          </div>
        </xsl:when>
        <xsl:otherwise>
          <div class="toc-item-{name()}">
            <a>
              <xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="@id" /></xsl:attribute>
              <xsl:apply-templates select="./node()" />
            </a>
          </div>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>