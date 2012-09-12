import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play-push-notification-extension"
    val appVersion      = "1.0-SNAPSHOT"


    val appDependencies = Seq(
      "com.anypresence.play" % "morphia-bootstrap_2.9.1" % "1.0-SNAPSHOT",
      "com.google.code.morphia"    % "morphia"               % "1.00-SNAPSHOT",
      //"com.google.code.morphia"    % "morphia"               % "0.99",
      "com.google.code.morphia"    % "morphia-logging-slf4j" % "0.99",
      "com.google.code.morphia"    % "morphia-validation"    % "0.99" 
      //"net.vz.mongodb.jackson" %% "play-mongo-jackson-mapper" % "1.0.0" 
      //"com.google.code" % "morphia" % "0.99" from "http://morphia.googlecode.com/svn/mavenrepo/",
      //"cglib" % "cglib-nodep" % "2.1_3",
      //"com.thoughtworks.proxytoys" % "proxytoys" % "1.0" from "http://morphia.googlecode.com/svn/mavenrepo/"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
      resolvers ++= Seq("Morphia" at "http://morphia.googlecode.com/svn/mavenrepo/"): _*
    )

}
