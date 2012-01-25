<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:rifcs="http://ands.org.au/standards/rif-cs/registryObjects"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns="http://www.openarchives.org/OAI/2.0/"
   exclude-result-prefixes="xs xsi xsl"
   version="2.0">
   <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
   <xsl:template match="/repository-harvest">

      <registryObjects xmlns="http://rdr.unimelb.edu.au/schema/murifcs"
         xsi:schemaLocation="http://rdr.unimelb.edu.au/schema/murifcs murifcs2.xsd"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

         <xsl:for-each select="repository">

            <registryObject>
               <xsl:element name="key">DaRIS:Repository:<xsl:value-of select="id"/></xsl:element>
               <originatingSource>
                  http://services.ands.org.au/sandbox/orca/register_my_data</originatingSource>
               <xsl:element name="collection">
                  <xsl:attribute name="type">repository</xsl:attribute>


                  <!-- Description of repo-->
                  <xsl:for-each select="data-holdings/description">

                     <xsl:element name="description">
                        <xsl:attribute name="type">full</xsl:attribute>
                        <xsl:value-of select="."/>
                     </xsl:element>
                  </xsl:for-each>

                  <!-- identifier of repo-->
                  <xsl:element name="identifier">
                     <xsl:attribute name="type">local</xsl:attribute>
                     <xsl:value-of select="@id"/>
                  </xsl:element>

                  <!-- location of repo -->
                  <xsl:for-each select="location">

                     <xsl:element name="location">
                        <xsl:element name="address">
                           <xsl:element name="physical">
                              <xsl:attribute name="type">streetAddress</xsl:attribute>
                              <xsl:for-each select="institution">
                                 <xsl:element name="addressLine">
                                    <xsl:value-of select="."/>
                                 </xsl:element>
                              </xsl:for-each>
                              <xsl:for-each select="precinct">
                                 <xsl:element name="addressLine">
                                    <xsl:value-of select="."/>
                                 </xsl:element>
                              </xsl:for-each>
                           </xsl:element>
                        </xsl:element>
                     </xsl:element>

                  </xsl:for-each>
                  <!-- relatedObject of repo -->

                  <relatedObject>
                     <xsl:element name="key">DaRIS:person:<xsl:value-of select="//custodian/first/."
                           />:<xsl:value-of select="//custodian/last/."/></xsl:element>
                     <relation type="isManagedBy"/>
                  </relatedObject>

                  <xsl:for-each select="rights/description">
                     <xsl:element name="description">
                        <xsl:attribute name="type">right</xsl:attribute>
                        <xsl:value-of select="."/>
                     </xsl:element>
                  </xsl:for-each>
               </xsl:element>
            </registryObject>
         </xsl:for-each>

         <xsl:for-each select="repository/custodian">
            <registryObject>
               <xsl:element name="key">DaRIS:person:<xsl:value-of select="first/."/>:<xsl:value-of
                     select="last/."/></xsl:element>
               <originatingSource>
                  http://services.ands.org.au/sandbox/orca/register_my_data</originatingSource>

               <xsl:element name="party">
                  <xsl:attribute name="type">person</xsl:attribute>

                  <xsl:for-each select="prefix">
                     <name type="title">
                        <namePart>
                           <xsl:value-of select="."/>
                        </namePart>
                     </name>
                  </xsl:for-each>

                  <xsl:for-each select="first">
                     <name type="given">
                        <namePart>
                           <xsl:value-of select="."/>
                        </namePart>
                     </name>
                  </xsl:for-each>
                  <!--
                  <xsl:for-each select="middle">
                     <name type="given">
                        <namePart>
                           <xsl:value-of select="."/>
                        </namePart>
                     </name>
                  </xsl:for-each>
-->
                  <xsl:for-each select="last">
                     <name type="family">
                        <namePart>
                           <xsl:value-of select="."/>
                        </namePart>
                     </name>
                  </xsl:for-each>

                  <xsl:for-each select="email">
                     <location>
                        <address>
                     <electronic type="email">
                        <value>
                           <xsl:value-of select="."/>
                        </value>
                     </electronic></address>
                     </location>
                  </xsl:for-each>
               </xsl:element>

            </registryObject>
         </xsl:for-each>

         <xsl:for-each select="projects/project">
            <registryObject>
               <xsl:element name="key">DaRIS:project:<xsl:value-of select="id/."/></xsl:element>
               <originatingSource>
                  http://services.ands.org.au/sandbox/orca/register_my_data</originatingSource>

               <xsl:element name="activity">
                  <xsl:attribute name="type">project</xsl:attribute>
                  <name type="primary">
                     <namePart>
                        <xsl:value-of select="name"/>
                     </namePart>
                  </name>

                  <xsl:element name="coverage">
                     <xsl:element name="temporal">

                        <xsl:element name="date">
                           <xsl:attribute name="type">dateFrom</xsl:attribute>
                           <xsl:attribute name="dateFormat">UTC</xsl:attribute>
                           <xsl:value-of select="first-acquisition-date"/>
                        </xsl:element>
                        <xsl:element name="date">
                           <xsl:attribute name="type">dateTo</xsl:attribute>
                           <xsl:attribute name="dateFormat">UTC</xsl:attribute>
                           <xsl:value-of select="last-acquisition-date"/>
                        </xsl:element>
                     </xsl:element>


                  </xsl:element>


                  <xsl:element name="description">
                     <xsl:attribute name="type">full</xsl:attribute>
                     <xsl:value-of select="description"/> Funding: <xsl:for-each select="funding-id"
                        > Organization :<xsl:value-of select="@type"/> id :<xsl:value-of
                           select="text()"/>
                     </xsl:for-each> Ethics: <xsl:for-each select="ethics-id"> Organization
                           :<xsl:value-of select="@type"/> id :<xsl:value-of select="text()"/>
                     </xsl:for-each>
                  </xsl:element>

                  <xsl:element name="description">
                     <xsl:attribute name="type">note</xsl:attribute> number of subjects:
                        <xsl:value-of select="number-of-subjects"/> number of studies: <xsl:value-of
                        select="number-of-studies"/> number of datasets: <xsl:value-of
                        select="number-of-datasets"/> size-of-content: <xsl:value-of
                        select="size-of-content"/><xsl:value-of select="size-of-content/@units"/>
                  </xsl:element>

                  <!-- identifier of project-->
                  <xsl:element name="identifier">
                     <xsl:attribute name="type">local</xsl:attribute>
                     <xsl:value-of select="id/."/>
                  </xsl:element>

                  <xsl:for-each select="keyword">
                     <xsl:element name="identifier">
                        <xsl:attribute name="type">local</xsl:attribute>
                        <xsl:value-of select="text()"/>
                     </xsl:element>
                  </xsl:for-each>


                  <!-- relatedObject of project -->
                  <relatedObject>
                     <xsl:element name="key">DaRIS:person:<xsl:value-of
                           select="//project-owner/first/."/>:<xsl:value-of
                           select="//project-owner/last/."/></xsl:element>
                     <relation type="isOwnedBy"/>
                  </relatedObject>
               </xsl:element>

            </registryObject>
         </xsl:for-each>

         <xsl:for-each select="projects/project/project-owner">
            <registryObject>
               <xsl:element name="key">DaRIS:person:<xsl:value-of select="first/."/>:<xsl:value-of
                     select="last/."/></xsl:element>
               <originatingSource>
                  http://services.ands.org.au/sandbox/orca/register_my_data</originatingSource>

               <xsl:element name="party">
                  <xsl:attribute name="type">person</xsl:attribute>

                  <xsl:for-each select="prefix">
                     <name type="title">
                        <namePart>
                           <xsl:value-of select="."/>
                        </namePart>
                     </name>
                  </xsl:for-each>

                  <xsl:for-each select="first">
                     <name type="given">
                        <namePart>
                           <xsl:value-of select="."/>
                        </namePart>
                     </name>
                  </xsl:for-each>
                  <!--
                  <xsl:for-each select="middle">
                     <name type="middle">
                        <namePart>
                           <xsl:value-of select="."/>
                        </namePart>
                     </name>
                  </xsl:for-each>
-->
                  <xsl:for-each select="last">
                     <name type="family">
                        <namePart>
                           <xsl:value-of select="."/>
                        </namePart>
                     </name>
                  </xsl:for-each>

                  <xsl:for-each select="email">
                     <location>
                        <address>
                     <electronic type="email">
                        <value>
                           <xsl:value-of select="."/>
                        </value>
                     </electronic></address>
                     </location>
                  </xsl:for-each>

               </xsl:element>

               <xsl:element name="description">
                  <xsl:attribute name="type">note</xsl:attribute> owner institution: <xsl:value-of
                     select="institution/name"/>
               </xsl:element>


            </registryObject>
         </xsl:for-each>

      </registryObjects>
   </xsl:template>

</xsl:stylesheet>