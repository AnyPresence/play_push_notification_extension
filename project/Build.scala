import sbt._
import Keys._
import play.Project._
import com.github.hexx.GithubRepoPlugin._

object ApplicationBuild extends Build {

  val appName         = "play_push_notification_extension"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(

    "se.radley" %% "play-plugins-salat" % "1.3.0",
    "org.mongodb" % "mongo-java-driver" % "2.10.1",

    // log4j and bcprov needed by javapns
    "log4j" % "log4j" % "1.2.15", 
    "org.bouncycastle" % "bcprov-jdk15" % "1.46",

    // javapns not published to mvn central or other mvn repo
    "com.google.code" % "javapns" % "2.2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(

    localRepo := Path.userHome / "github" / "play_api_maven_repo",

    githubRepo := "git@github.com:AnyPresence/play_api_maven_repo.git",
    
    routesImport += "se.radley.plugin.salat.Binders._", templatesImport += "org.bson.types.ObjectId",
    
    resolvers ++= Seq( 
      "AnyPresence Repository" at "http://AnyPresence.github.io/play_api_maven_repo",
      "Maven Central Server" at "http://repo1.maven.org/maven2",
      "Novus Release Repository" at "http://repo.novus.com/releases/"
    )

  ).settings(githubRepoSettings: _*)

}
