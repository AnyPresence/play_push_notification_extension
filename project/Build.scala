import sbt._
import Keys._
import PlayProject._
import java.io.File
import java.util.Date
import play.core._
import play.core.Router.RoutesCompiler

object ApplicationBuild extends Build {

    val appName         = "play-push-notification-extension"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "com.anypresence.play" % "morphia-bootstrap_2.9.1" % "1.0-SNAPSHOT",
      "com.google.code.morphia"    % "morphia"               % "1.00-SNAPSHOT",
      "com.google.code.morphia"    % "morphia-logging-slf4j" % "0.99",
      "com.google.code.morphia"    % "morphia-validation"    % "0.99" 
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
      resolvers ++= Seq("Morphia" at "http://morphia.googlecode.com/svn/mavenrepo/"): _*).settings(
        // Ensure that the application.conf and routes files are not included in the compiled jar file
        // when it is packaged
	defaultExcludes in Compile in unmanagedResources := "*.conf" || "routes" || "Route*"
      ).settings(
        // Ensure that the application.conf and routes files ARE included for testing
        defaultExcludes in Test in unmanagedResources := ""
      ).settings(
          // Exclude all compiled routing classes when the application is packaged
          mappings in (Compile,packageBin) ~= { (ms: Seq[(File, String)]) =>
            val routesStraightFilter = """(Routes\$?)(\$anon.*)?.class""".r
            val controllerRoutesFilter = """([\w\$/]*)controllers/routes([\w\$]*).class""".r
            val routesRefFilter = """([\w\$/]*)routes(\$ref|\$javascript)?.class""".r
            val refReverseFilter = """([\w\$/]*)ref/Reverse([\w\$]*).class""".r
            val controllerReverseFilter = """controllers/([\w\$/]*)Reverse([\w\$]*).class""".r
            val otherRoutesFilter  = """controllers/([\w\$/]*)routes.class""".r
            ms filter {                
                case (file, toPath) => {
                    println(file.toString)
                    println(toPath)
                    val routesMatch = toPath match {
                        case routesStraightFilter(_,_) => true
                        case controllerRoutesFilter(_,_) => true
                        case routesRefFilter(_,_) => true
                        case refReverseFilter(_,_) => true
                        case controllerReverseFilter(_,_) => true
                        case otherRoutesFilter(_) => true
                        case _ => false }
                    !routesMatch && toPath != "routes" && toPath != "application.conf"
                }
            }
        }
)


    

}

