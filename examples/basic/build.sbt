scalaVersion := "2.11.12"

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0"