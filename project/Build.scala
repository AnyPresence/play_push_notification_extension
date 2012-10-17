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
      //"com.google.code.morphia"    % "morphia"               % "0.99",
      "com.google.code.morphia"    % "morphia-logging-slf4j" % "0.99",
      "com.google.code.morphia"    % "morphia-validation"    % "0.99" 
      //"net.vz.mongodb.jackson" %% "play-mongo-jackson-mapper" % "1.0.0" 
      //"com.google.code" % "morphia" % "0.99" from "http://morphia.googlecode.com/svn/mavenrepo/",
      //"cglib" % "cglib-nodep" % "2.1_3",
      //"com.thoughtworks.proxytoys" % "proxytoys" % "1.0" from "http://morphia.googlecode.com/svn/mavenrepo/"
    )

    def recursiveListFiles(f: File): Seq[File] = {
      val these = f.listFiles
      these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
    }

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
      resolvers ++= Seq("Morphia" at "http://morphia.googlecode.com/svn/mavenrepo/"): _*).settings(

	//sourceGenerators in Compile := Seq()
        /*sourceGenerators in Test <+= sourceManaged in Test map { dir =>
          def compiler = Router.RoutesCompiler
          compiler.compile(new File("/Users/rsnyder/git/play-push-notification-extension/test/routes"), dir, List() )
          def list = recursiveListFiles(dir)
          list.filter(_.isFile)
         }*/
        ).settings(
        //unmanagedResources in Test := List()
	defaultExcludes in Compile in unmanagedResources := "*.conf" || "routes" || "Route*"
      ).settings(
        defaultExcludes in Test in unmanagedResources := ""
      ).settings(
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

