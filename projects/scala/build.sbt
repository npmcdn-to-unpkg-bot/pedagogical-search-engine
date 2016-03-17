name := "scala"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.json4s" % "json4s-ext_2.11" % "3.3.0",
  "org.jsoup" % "jsoup" % "1.8.3",
  "net.databinder.dispatch" % "dispatch-core_2.11" % "0.11.3",
  "net.databinder.dispatch" % "dispatch-json4s-native_2.11" % "0.11.3",
  "com.typesafe" % "config" % "1.2.1",

  // Scala Test
  "org.scalactic" %% "scalactic" % "2.2.6",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",

  // Pre-Factored Graph code dependencies
  // todo: review them
  "org.apache.commons" % "commons-lang3" % "3.4",
  "org.json" % "json" % "20131018",
  "junit" % "junit" % "4.12"
)
