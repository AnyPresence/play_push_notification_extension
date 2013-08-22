import sbt._
import Keys._
import play.Project._
import com.github.hexx.GithubRepoPlugin._

object ApplicationBuild extends Build {

  val appName         = "play_push_notification_extension"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "se.radley" %% "play-plugins-salat" % "1.2",
    "org.mongodb" % "mongo-java-driver" % "2.10.1",
    "log4j" % "log4j" % "1.2.15" from "http://javapns.googlecode.com/svn/tags/2.2/lib/log4j-1.2.15.jar", 
    "bcprov" % "bcprov" % "146" from "http://javapns.googlecode.com/svn/tags/2.2/lib/bcprov-jdk15-146.jar",
    "code.google.com" % "javapns" % "2.2" from "http://javapns.googlecode.com/svn/tags/2.2/JavaPNS_2.2.jar"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(

    localRepo := Path.userHome / "github" / "play_api_maven_repo",

    githubRepo := "git@github.com:AnyPresence/play_api_maven_repo.git",
    
    routesImport += "se.radley.plugin.salat.Binders._", templatesImport += "org.bson.types.ObjectId",
    
    resolvers ++= Seq( 
      "Novus Release Repository" at "http://repo.novus.com/releases/",
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/" 
    )

  ).settings(githubRepoSettings: _*)

}
