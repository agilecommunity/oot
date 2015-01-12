name := "oot-webapp"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
    javaJdbc
  , javaEbean
  , cache
  , filters
  , "org.avaje.ebeanorm" % "avaje-ebeanorm-api" % "3.1.1"
  , "ws.securesocial" %% "securesocial" % "2.1.4"
  , "eu.medsea.mimeutil" % "mime-util" % "2.1.1"
  , "net.lingala.zip4j" % "zip4j" % "1.3.2"
  , "postgresql" % "postgresql" % "9.1-901-1.jdbc4"
  , "joda-time" % "joda-time" % "2.3"
  , "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.4.0"
  , "commons-io" % "commons-io" % "2.4"
  , "commons-codec" % "commons-codec" % "1.9"
  , "org.easytesting" % "fest-assert-core" % "2.0M10" % "test"
  , "pl.pragmatists" % "JUnitParams" % "1.0.2" % "test"
  , "info.cukes" % "cucumber-java" % "1.1.6" % "test"
  , "info.cukes" % "cucumber-junit" % "1.1.6" % "test"
  , "org.seleniumhq.selenium" % "selenium-java" % "2.44.0" % "test"
  , "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.44.0" % "test"
)

play.Project.playJavaSettings

playAssetsDirectories <+= baseDirectory / "ui"

// テストの設定
// cucumberの使うフィーチャファイルを対象から除外
unmanagedResourceDirectories in Test <+= baseDirectory( _ / "test" / "features" / "scenarios" )

// testからcucumberのテストを除外
testOptions in Test := Seq(Tests.Filter(s => !(s startsWith "feature")))

javaOptions in Test += "-XX:-UseSplitVerifier"

javaOptions in Test += "-Dconfig.file=conf/unit-test.conf"

Keys.fork in Test := true

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
  val driverOptions = System.getProperty("selenium.driver") match {
    case s:String => "-Dselenium.driver=" + s
    case _ => ""
  }
  val baseUrlOptions = System.getProperty("selenium.baseUrl") match {
    case s:String => "-Dselenium.baseUrl=" + s
    case _ => ""
  }
  val jvmOptions = Seq(cucumberOpts, "-Dlogger.resource=logger-test-features.xml") :+ driverOptions :+ baseUrlOptions
  logger.info("jvmOptions: "  + jvmOptions)
  val cucumberRunner = "features.RunCucumber"
  val classPassArgs = (fullClasspath in Test).value.map(_.data).mkString(";")
  val forkResult: Int = Fork.java(ForkOptions(runJVMOptions=jvmOptions), Seq("-cp", classPassArgs, "org.junit.runner.JUnitCore", cucumberRunner))
  forkResult match {
    case 0 => 0
    case _ => sys.error("run cucumber failed") // cucumberが失敗したらタスクも失敗するように(結構強引)
  }
}
