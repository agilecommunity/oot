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
  , "eu.medsea.mimeutil" % "mime-util" % "2.1.1"
  , "net.lingala.zip4j" % "zip4j" % "1.3.2"
  , "pl.pragmatists" % "JUnitParams" % "1.0.2"
  , "com.google.code.gson" % "gson" % "2.2.4"
  , "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
  , "joda-time" % "joda-time" % "2.3"
  , "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.4.0"
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

// testからcucumberのテストを除外
testOptions in Test := Seq(Tests.Filter(s => !(s startsWith "feature")))

javaOptions in Test += "-XX:-UseSplitVerifier"

javaOptions in Test += "-Dconfig.file=conf/application.conf"


// cucumberタスクの定義
lazy val cucumberTask = InputKey[Unit]("cucumber", "Run Cucumber tests.")

cucumberTask := {
  val logger = streams.value.log
  logger.info("Running Cucumber tests.")
  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed
  val cucumberOpts = args.isEmpty match {
    case false => "-Dcucumber.options=" + args.map("--tags " + _).mkString(" ")
    case true => ""
  }
  logger.info("Cucumber.Options: "  + cucumberOpts)
  val cucumberRunner = "features.RunCucumber"
  val classPassArgs = (fullClasspath in Test).value.map(_.data).mkString(";")
  Fork.java(ForkOptions(runJVMOptions=Seq(cucumberOpts)), Seq("-cp", classPassArgs, "org.junit.runner.JUnitCore", cucumberRunner))
}

