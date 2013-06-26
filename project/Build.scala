import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play_push_notification_extension"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "se.radley" %% "play-plugins-salat" % "1.2",
    "org.mongodb" % "mongo-java-driver" % "2.10.1" 
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    routesImport += "se.radley.plugin.salat.Binders._", templatesImport += "org.bson.types.ObjectId",
    resolvers ++= Seq( 
      "Novus Release Repository" at "http://repo.novus.com/releases/",
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/" 
    )
    
  )

}
