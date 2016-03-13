name := "scala"

version := "1.0"

scalaVersion := "2.11.7"

val json4sNative = "org.json4s" %% "json4s-native" % "3.2.11"
val json4sExt = "org.json4s" % "json4s-ext_2.11" % "3.3.0"


val jsoup = "org.jsoup" % "jsoup" % "1.8.3"

val dispatch = "net.databinder.dispatch" % "dispatch-core_2.11" % "0.11.3"
val dispatchJson = "net.databinder.dispatch" % "dispatch-json4s-native_2.11" % "0.11.3"
val configFile = "com.typesafe" % "config" % "1.2.1"

libraryDependencies += json4sNative
libraryDependencies += jsoup
libraryDependencies += dispatch
libraryDependencies += dispatchJson
libraryDependencies += configFile
libraryDependencies += json4sExt
