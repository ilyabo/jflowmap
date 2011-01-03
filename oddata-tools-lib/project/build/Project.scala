import sbt.{Path, DefaultProject, ProjectInfo}

class Project(info: ProjectInfo) extends DefaultProject(info) with ProguardProject {

  val scalatest = "org.scalatest" % "scalatest" % "1.2"

  override def allDependencyJars = (super.allDependencyJars +++
    Path.fromFile(buildScalaInstance.compilerJar) +++
    Path.fromFile(buildScalaInstance.libraryJar)
  )

  override def proguardOptions = List(
    "-keep class oddata.** { *; }",
    "-keep class countries.** { *; }",
    "-keep class geosearch.** { *; }",
    "-keep interface scala.ScalaObject"
  )

}
