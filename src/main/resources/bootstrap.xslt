<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/html/head">
    <head>
      <xsl:apply-templates select="@*|node()" />
      <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous" />
    </head>
  </xsl:template>

  <xsl:param name="prevPartLink" />
  <xsl:param name="prevPartTitle" />
  <xsl:param name="nextPartLink" />
  <xsl:param name="nextPartTitle" />

  <xsl:template match="/html/body">
    <body>
      <div class="container">
        <xsl:apply-templates select="./node()" />
        <nav aria-label="Page navigation example">
          <ul class="pagination justify-content-center">
            <xsl:if test="$prevPartLink">
              <li class="page-item">
                <a class="page-link" href="{$prevPartLink}">
                  <xsl:text>🠸 </xsl:text>
                  <xsl:value-of select="$prevPartTitle" />
                </a>
              </li>
            </xsl:if>
            <xsl:if test="$nextPartLink">
              <li class="page-item">
                <a class="page-link" href="{$nextPartLink}">
                  <xsl:value-of select="$nextPartTitle" />
                  <xsl:text> 🠺</xsl:text>
                </a>
              </li>
            </xsl:if>
          </ul>
        </nav>
      </div>
    </body>
  </xsl:template>

</xsl:stylesheet>