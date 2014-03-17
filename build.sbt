name := "oot-webapp"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    javaJdbc
  , javaEbean
  , cache
  , filters
  , "org.avaje.ebeanorm" % "avaje-ebeanorm-api" % "3.1.1"
  , "org.easytesting" % "fest-assert-core" % "2.0M10" % "test"
  , "ws.securesocial" %% "securesocial" % "2.1.3"
  , "pl.pragmatists" % "JUnitParams" % "1.0.2"
)

play.Project.playJavaSettings

playAssetsDirectories <+= baseDirectory / "ui"
