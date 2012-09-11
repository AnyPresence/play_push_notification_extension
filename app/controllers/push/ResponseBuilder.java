package controllers.push;

import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class ResponseBuilder {

	public static Result okJsonResponse(boolean success) {
		return okJsonResponse(success, null);
	}
	
	public static Result okJsonResponse(boolean success, String errorMessage) {
		return Controller.ok(jsonString(success, errorMessage)).as("application/json");
	}
	
	public static Result badRequestJsonResponse(boolean success, String errorMessage) {
		return Controller.badRequest(jsonString(success, errorMessage)).as("application/json");
	}
	
	public static String jsonString(boolean success, String errorMessage) {
		ObjectNode node = Json.newObject();
		node.put("success", success);
		if (errorMessage != null) {
			node.put("error", errorMessage);
		}
		return Json.stringify(node);
	}
	
}
