package controller.push;

import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static org.fest.assertions.Assertions.*;

import org.junit.Test;

import play.libs.WS;
import play.test.TestServer;

public class PubSubControllerTest {
	
	  private static final int PORT = 3333;
	  private static final String TEST_SERVER_URL = "http://localhost:" + PORT;
	  

	  @Test
	  public void testSubscribe() {
		  //TestServer ts = testServer(PORT);
		  //start(ts);
		  
		  //Result result = callAction(controllers.push.routes.ref.PubSubController.subscribe());
		  
		  //WS.Response resp = WS.url(TEST_SERVER_URL + "/push/subscribe").post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAA\" }").get().;
		  
		  TestServer ts = testServer(PORT);
		  
		  running(ts, new Runnable() {
		      public void run() {
		    		 /*MorphiaBootstrapPlugin p = Play.application().plugin(MorphiaBootstrapPlugin.class);
		    		 System.out.println("mongodb.uri : " + Play.application().configuration().getString("mongodb.uri"));
		    		 System.out.println("p : " + p);
		    		 FakeRequest req =  fakeRequest(POST, "/push/subscribe");
		    		 req = req.withHeader("Content-Type", "application/json").withHeader("User-Agent", "iPhone").withJsonBody(Json.parse("{ \"channel\" : \"my_channel\", \"deviceToken\" : \"AAA\" }"));
		    		 System.out.println(routeAndCall(req));*/
		    		 
		    		 
		    		 
		    		 
		    		 // Result result = callAction(controllers.push.routes.ref.PubSubController.subscribe());
		    	  //System.out.println(result);
		         assertThat(
		           WS.url(TEST_SERVER_URL + "/push/subscribe")
		             .setHeader("User-Agent", "iPhone")
		             .setHeader("Content-Type", "application/json")
		           	 .post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAA\" }").get().getStatus()
		    	  //System.out.println(WS.url(TEST_SERVER_URL + "/push/subscribe").post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAA\" }").get().getBody());
		         ).isEqualTo(OK);
		      }
		  });

	  
	  }
	  
	  
	
}
