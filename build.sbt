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
  , "com.google.code.gson" % "gson" % "2.2.4"
  , "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
  , "joda-time" % "joda-time" % "2.3"
  , "info.cukes" % "cucumber-java" % "1.1.6"
  , "info.cukes" % "cucumber-junit" % "1.1.6"
  , "org.seleniumhq.selenium" % "selenium-java" % "2.41.0"
  , "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.41.0"
)

play.Project.playJavaSettings

playAssetsDirectories <+= baseDirectory / "ui"

// テストの設定
// cucumberの使うフィーチャファイルを対象から除外
unmanagedResourceDirectories in Test <+= baseDirectory( _ / "test" / "features" / "scenarios" )
