package controllers.push;

import java.util.HashSet;
import java.util.Set;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	private static Set<String> set = new HashSet<String>();

	public static Result index() {
		return ok("OK, i got your message").as("text/plain");
	}

	public static Result register(String id) {
		Logger.debug("Register called with registration : "
				+ request().headers());
		if (!set.contains(id)) {
			set.add(id);
		}
		return ok("OK, you're registered with ID " + id).as("text/plain");
	}

	public static Result unregister(String id) {
		Logger.debug("Unregister called with headers : " + request().headers());
		if (set.contains(id)) {
			set.remove(id);
		}
		return ok("OK, you're unregistered with ID " + id).as("text/plain");
	}



}