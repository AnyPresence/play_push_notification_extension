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
		  
		  TestServer ts = testServer(PORT);
		  
		  running(ts, new Runnable() {
		      public void run() {
		         assertThat(
		           WS.url(TEST_SERVER_URL + "/push/subscribe")
		             .setHeader("User-Agent", "iPhone")
		             .setHeader("Content-Type", "application/json")
		          	 .post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAA\" }").get().getStatus()
			).isEqualTo(OK);
		      }
		  });

	  
	  }
	  
	  
	
}
