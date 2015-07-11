lazy val root = (project in file(".")).
  settings(
    name := "clicktalk-match",
    version := "0.0.1",
    libraryDependencies += "com.twitter" %% "finagle-httpx" % "6.26.0",
    libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.11",
    libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"
  )
