<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- 
Put in header of XML:
<?xml-stylesheet type="text/xsl" href="rmdtemplate.xsl"?>
-->

<xsl:template match="/">
  <html>
  <body>

    <h2>General imageset info</h2>
    <ul>
      <li/>Resolution X: <xsl:value-of select="ost/imageset/resX"/>
      <li/>Resolution Y: <xsl:value-of select="ost/imageset/resY"/>
      <li/>Resolution Z: <xsl:value-of select="ost/imageset/resZ"/>
    </ul>
    <ul>
      <li/>Timestep: <xsl:value-of select="ost/imageset/timestep"/>s
      <li/>NA: <xsl:value-of select="ost/imageset/NA"/>
      <li/>Objective: <xsl:value-of select="ost/imageset/objective"/>x
      <li/>optivar: <xsl:value-of select="ost/imageset/optivar"/>x
      <li/>campix: <xsl:value-of select="ost/imageset/campix"/>
      <li/>slicespacing: <xsl:value-of select="ost/imageset/slicespacing"/>um
      <li/>description: <xsl:value-of select="ost/imageset/description"/>
    </ul>

    <h2>General channel info</h2>
    <table border="1">
    <tr bgcolor="#9acd32">
      <th align="left">Channel</th>
      <th align="left">Binning</th>
      <th align="left">DispX</th>
      <th align="left">DispY</th>
    </tr>
    <xsl:for-each select="ost/imageset/channel">
    <tr>
      <td><xsl:value-of select="@name"/></td>
      <td><xsl:value-of select="binning"/></td>
      <td><xsl:value-of select="dispX"/></td>
      <td><xsl:value-of select="dispY"/></td>
    </tr>
    </xsl:for-each>
    </table>


<!-- <table><tr> -->

    <xsl:for-each select="ost/imageset/channel">
<!-- <td valign="top"> -->

      <h2>
        <xsl:value-of select="@name"/>
      </h2>

      <table border="1">
      <tr bgcolor="#9acd32">
        <th align="left">Frame</th>
        <th align="left">Date</th>
        <th align="left">Temperature1</th>
        <th align="left">Temperature2</th>
        <th align="left">Exposure</th>
<!-- could take first element and enumerate -->
      </tr>
      <xsl:for-each select="frame">
      <xsl:sort select="./@frame" order="ascending"/>

      <xsl:variable name="T" select="temperature1"/>
      <xsl:choose>
        <xsl:when test="temperature1=0">
        <tr bgcolor="#FF9999">
<!-- call here? -->
          <td><xsl:value-of select="@frame"/></td>
          <td><xsl:value-of select="date"/></td>
          <td><xsl:value-of select="temperature1"/></td>
          <td><xsl:value-of select="temperature2"/></td>
          <td><xsl:value-of select="exposuretime"/></td>
        </tr>
        </xsl:when>
        <xsl:otherwise>
        <tr>
          <td><xsl:value-of select="@frame"/></td>
          <td><xsl:value-of select="date"/></td>
          <td><xsl:value-of select="temperature1"/></td>
          <td><xsl:value-of select="temperature2"/></td>
          <td><xsl:value-of select="exposuretime"/></td>
        </tr>
        </xsl:otherwise>
      </xsl:choose>
      </xsl:for-each>
      </table>
<!-- </td> -->
    </xsl:for-each>
<!-- </tr></table> -->
  </body>
  </html>
</xsl:template>
</xsl:stylesheet>