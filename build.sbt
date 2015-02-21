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
  , "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.5.1"
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

javaOptions in Test ++= Seq("-XX:-UseSplitVerifier")

// 設定ファイルの指定
val appOptions = System.getProperty("config.file") match {
  case s:String => "-Dconfig.file=" + s
  case _ => "-Dconfig.file=conf/unit-test.conf"
}

javaOptions in Test += appOptions

//javaOptions in Test ++= Seq("-Ddefault.db.driver=org.h2.Driver", "-Ddefault.db.url=jdbc:h2:mem:test")

Keys.fork in Test := true

// cucumberタスクの定義
lazy val cucumberTask = InputKey[Int]("cucumber", "Run Cucumber tests.")

cucumberTask := {
  import java.io.File
  val logger = streams.value.log
  logger.info("Running Cucumber tests.")
  logger.info("- os.name: " + System.getProperty("os.name").toLowerCase())
  val args: Seq[String] = Def.spaceDelimited("<arg>").parsed
  val cucumberOpts = args.isEmpty match {
    case false => "\"-Dcucumber.options=" + args.map("--tags " + _).mkString(" ") + "\""
    case true => ""
  }
  logger.info("- cucumber.options: "  + cucumberOpts)
  val seleniumDriverOptions = System.getProperty("selenium.driver") match {
    case s:String => "-Dselenium.driver=" + s
    case _ => ""
  }
  val seleniumBaseUrlOptions = System.getProperty("selenium.baseUrl") match {
    case s:String => "-Dselenium.baseUrl=" + s
    case _ => ""
  }
  val configFileOption = System.getProperty("config.file") match {
    case s:String => "-Dconfig.file=" + s
    case _ => "-Dconfig.file=conf/unit-test.conf"
  }
  val appOptions = Seq("-Dlogger.resource=logger-test-features.xml", configFileOption)
  val jvmOptions = appOptions :+ cucumberOpts :+ seleniumDriverOptions :+ seleniumBaseUrlOptions
  logger.info("- jvmOptions: "  + jvmOptions)
  val cucumberRunner = "features.RunCucumber"
  val classPathArgs = (fullClasspath in Test).value.map(_.data).mkString(File.pathSeparator)
  //val forkResult: Int = Fork.java(ForkOptions(runJVMOptions=jvmOptions), Seq("-cp", classPathArgs, "org.junit.runner.JUnitCore", cucumberRunner))
  val commandStr = "java " + jvmOptions.mkString(" ") + Seq("-cp", classPathArgs, "org.junit.runner.JUnitCore", cucumberRunner).mkString(" ")
  val forkResult = Process(commandStr) ! logger
  forkResult match {
    case 0 => 0
    case _ => sys.error("run cucumber failed") // cucumberが失敗したらタスクも失敗するように(結構強引)
  }
}
