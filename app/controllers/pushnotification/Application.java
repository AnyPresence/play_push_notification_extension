package controllers.pushnotification;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javapns.Push;
import javapns.notification.PushedNotifications;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

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

	public static Result sendAndroidMessageToAllRegistrants() {
		for (String deviceId : set) {

			try {
				// Content-Type:application/json
				// Authorization:key=AIzaSyAQgYAOflhyu9ZuV4EFIh0Rcu6UVoS2Aj0
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost request = new HttpPost(
						"https://android.googleapis.com/gcm/send");

				String json = "{ \"data\" : { \"key\" : \"value\" }, \"registration_ids\" : [\""
						+ deviceId + "\"] }";

				StringEntity se = new StringEntity(json, HTTP.UTF_8);
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
						"application/json"));

				request.addHeader("Content-Type", "application/json");
				request.addHeader("Authorization",
						"key=AIzaSyAQgYAOflhyu9ZuV4EFIh0Rcu6UVoS2Aj0");
				request.setEntity(se);

				// request.set)(new HttpEntity())
				ResponseHandler<String> handler = new BasicResponseHandler();
				String result = null;
				try {
					result = httpclient.execute(request, handler);
					Logger.info("Got result " + result
							+ " after sending post to " + deviceId);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				httpclient.getConnectionManager().shutdown();

				Logger.info("Push notification successfully sent!");
			} catch (Exception e) {
				Logger.error("Uh-oh, somthing bad happened", e);
			}

		}

		/*
		 * if (result.getMessageId() != null) { String canonicalRegId =
		 * result.getCanonicalRegistrationId(); if (canonicalRegId != null) { //
		 * same device has more than on registration ID: update database } }
		 * else { String error = result.getErrorCodeName(); if
		 * (error.equals(Constants.ERROR_NOT_REGISTERED)) { // application has
		 * been removed from device - unregister database } }
		 */
		return ok("I sent the push notification").as("text/plain");
	}

	public static Result sendMessageToAllRegistrants() {
		/*
		 * for (String deviceId : set) {
		 * 
		 * try { //Content-Type:application/json
		 * //Authorization:key=AIzaSyAQgYAOflhyu9ZuV4EFIh0Rcu6UVoS2Aj0
		 * HttpClient httpclient = new DefaultHttpClient(); HttpPost request =
		 * new HttpPost("https://android.googleapis.com/gcm/send");
		 * 
		 * String json =
		 * "{ \"data\" : { \"key\" : \"value\" }, \"registration_ids\" : [\"" +
		 * deviceId + "\"] }";
		 * 
		 * StringEntity se = new StringEntity(json,HTTP.UTF_8);
		 * se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
		 * "application/json"));
		 * 
		 * request.addHeader("Content-Type", "application/json");
		 * request.addHeader("Authorization",
		 * "key=AIzaSyAQgYAOflhyu9ZuV4EFIh0Rcu6UVoS2Aj0");
		 * request.setEntity(se);
		 * 
		 * //request.set)(new HttpEntity()) ResponseHandler<String> handler =
		 * new BasicResponseHandler(); String result = null; try { result =
		 * httpclient.execute(request, handler); Logger.info("Got result " +
		 * result + " after sending post to " + deviceId); } catch
		 * (ClientProtocolException e) { e.printStackTrace(); } catch
		 * (IOException e) { e.printStackTrace(); }
		 * httpclient.getConnectionManager().shutdown();
		 * 
		 * Logger.info("Push notification successfully sent!"); }
		 * catch(Exception e) { Logger.error("Uh-oh, somthing bad happened", e);
		 * }
		 * 
		 * }
		 */
		try {
			PushedNotifications pn = Push
					.alert("Hello from Java!", new File(
							"/Users/rsnyder/Desktop/pushchat.p12"), "kongkong",
							false,
							"e8337c4294a0eb272f3eacd946e5f06b420fb778e04d9cd99672e52cfbc3fad2");
			PushedNotifications success = pn.getSuccessfulNotifications();
			PushedNotifications fail = pn.getFailedNotifications();

			Logger.info("Successful push notifications : " + success.size());
			Logger.info("Failed push notifications : " + fail.size());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ok("I sent the push notification").as("text/plain");
	}

}