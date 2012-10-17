package controller.push;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.HeaderNames.CONTENT_TYPE;
import static play.mvc.Http.HeaderNames.USER_AGENT;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.net.UnknownHostException;

import junit.framework.Assert;
import models.push.Channel;
import models.push.Device;
import models.push.DeviceType;

import org.junit.Before;
import org.junit.Test;

import play.libs.WS;
import play.libs.WS.Response;
import play.libs.WS.WSRequestHolder;

import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.Mongo;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest(Push.class)
//@PowerMockIgnore("javax.management.*")
public class PubSubControllerTest {
	
	private static final int PORT = 3333;
	private static final long TIMEOUT = 10000L;
	private static final String TEST_SERVER_URL = "http://localhost:" + PORT;

	private DB db;
	
	@Before
	public void discardData() throws UnknownHostException {	
		
		String device = "{ " + 
					    "      		\"_id\" : ObjectId(\"507e0c1403644c224d976b1d\"), " + 
					    "      		\"className\" : \"models.push.Device\",  " +
					    "      		\"token\" : \"AAB\",  " +
					    "      		\"type\" : \"ios\",  " +
					    "      		\"channels\" : [ { \"$ref\" : \"channels\", \"$id\" : ObjectId(\"507e0c1403644c224d976b1e\") } ],  " + 
					    "      		\"createdAt\" : ISODate(\"2012-10-17T01:38:28.163Z\"),  " +
					    "      		\"updatedAt\" : ISODate(\"2012-10-17T01:38:28.228Z\")  " +
					    "}";
		String device2 = "{ " + 
					    "      		\"_id\" : ObjectId(\"507e0c1403644c224d976b2e\"), " + 
					    "      		\"className\" : \"models.push.Device\",  " +
					    "      		\"token\" : \"token2\",  " +
					    "      		\"type\" : \"ios\",  " +
					    "      		\"channels\" : [ { \"$ref\" : \"channels\", \"$id\" : ObjectId(\"507e0c1403644c224d976b1e\") } ],  " + 
					    "      		\"createdAt\" : ISODate(\"2012-10-17T01:38:28.163Z\"),  " +
					    "      		\"updatedAt\" : ISODate(\"2012-10-17T01:38:28.228Z\")  " +
					    "}";
		String device3 = "{ " + 
					    "      		\"_id\" : ObjectId(\"507e0c1403644c224d976b3f\"), " + 
					    "      		\"className\" : \"models.push.Device\",  " +
					    "      		\"token\" : \"token3\",  " +
					    "      		\"type\" : \"android\",  " +
					    "      		\"channels\" : [ { \"$ref\" : \"channels\", \"$id\" : ObjectId(\"507e0c1403644c224d976b1e\") } ],  " + 
					    "      		\"createdAt\" : ISODate(\"2012-10-17T01:38:28.163Z\"),  " +
					    "      		\"updatedAt\" : ISODate(\"2012-10-17T01:38:28.228Z\")  " +
					    "}";
		
		String channel = "{ " + 
				         " 		\"_id\" : ObjectId(\"507e0c1403644c224d976b1e\"), " + 
				         " 		\"className\" : \"models.push.Channel\",  " +
				         " 		\"name\" : \"my_channel\",  " +
				         " 		\"devices\" : [ { \"$ref\" : \"devices\", \"$id\" : ObjectId(\"507e0c1403644c224d976b1d\") }, { \"$ref\" : \"devices\", \"$id\" : ObjectId(\"507e0c1403644c224d976b2e\") }, { \"$ref\" : \"devices\", \"$id\" : ObjectId(\"507e0c1403644c224d976b3f\") } ], " + 
				         " 		\"createdAt\" : ISODate(\"2012-10-17T01:38:28.168Z\"),  " +
				         " 		\"updatedAt\" : ISODate(\"2012-10-17T01:38:28.227Z\")  " +
				         "}";
		
		
		db = Mongo.connect(new DBAddress("localhost", 27017, "push-notification-extensions-test"));
		
		db.getCollection("channels").drop();
		db.getCollection("devices").drop();
		db.eval("db.devices.save(" + device + ")");
		db.eval("db.devices.save(" + device2 + ")");
		db.eval("db.devices.save(" + device3 + ")");
		db.eval("db.channels.save(" + channel + ")");
		
		Assert.assertEquals(1, sizeOfCollection("channels"));
		Assert.assertEquals(3, sizeOfCollection("devices"));
	}
	
	private WSRequestHolder subscribe() {
		return WS.url(TEST_SERVER_URL + "/push/subscribe").setHeader(CONTENT_TYPE, "application/json");
	}
	
	private WSRequestHolder unsubscribe() {
		return WS.url(TEST_SERVER_URL + "/push/unsubscribe").setHeader(CONTENT_TYPE, "application/json");
	}
	
	private WSRequestHolder publish() {
		return WS.url(TEST_SERVER_URL + "/push/publish").setHeader(CONTENT_TYPE, "application/json");
	}
	
	private long sizeOfCollection(String collectionName) {
		return db.getCollection(collectionName).count();
	}
	
	private int numberOfDevicesSubscribedToChannel(String channelName) {
		return Channel.findByName(channelName).devices.size();
	}
	
	private int numberOfChannelsSubscribedToByDevice(String token, DeviceType deviceType) {
		return Device.findByTokenAndType(token, deviceType).channels.size();
	}
	
	private boolean channelDoesNotExist(String channelName) {
		return Channel.findByName(channelName) == null;
	}
	
	private boolean deviceDoesNotExist(String deviceToken, DeviceType deviceType) {
		return Device.findByTokenAndType(deviceToken, deviceType) == null;
	}
	
	@Test
	public void testUnsubscribeBadRequest() { 
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{}")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{\"deviceToken\" : \"AAA\" }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{\"channel\" : \"my_channel\" }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"deviceToken\" : 1,  \"channel\" : \"my_channel\" }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"deviceToken\" : \"AAA\",  \"channel\" : 1 }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
			}
		});
	}
	
	@Test
	public void testSubscribeBadRequest() { 
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{}")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{\"deviceToken\" : \"AAA\" }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{\"channel\" : \"my_channel\" }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"deviceToken\" : 1,  \"channel\" : \"my_channel\" }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"deviceToken\" : \"AAA\",  \"channel\" : 1 }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
			}
		});
	}
	
	@Test 
	public void testNoUserAgent() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAA\" }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
			}
		});
	}
	
	@Test
	public void testUnsubscribeWithNonExistentDevice() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAA\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(1L);
				assertThat(sizeOfCollection("devices")).isEqualTo(3L);
				assertThat(deviceDoesNotExist("AAA", DeviceType.ios)).isTrue();
				assertThat(numberOfDevicesSubscribedToChannel("my_channel")).isEqualTo(3);
			}
		});
		
	}
	
	@Test
	public void testUnsubscribeWithNonExistentChannel() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel2\" , \"deviceToken\" : \"AAB\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(1L);
				assertThat(sizeOfCollection("devices")).isEqualTo(3L);
				assertThat(channelDoesNotExist("my_channel_2")).isTrue();
				assertThat(numberOfChannelsSubscribedToByDevice("AAB", DeviceType.ios)).isEqualTo(1);
			}
		});
	}
	
	@Test
	public void testUnsubscribeWithNonExistentChannelAndNonExistentDevice() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel2\" , \"deviceToken\" : \"AAC\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(1L);
				assertThat(sizeOfCollection("devices")).isEqualTo(3L);
				assertThat(channelDoesNotExist("my_channel2")).isTrue();
				assertThat(deviceDoesNotExist("AAC", DeviceType.ios)).isTrue();
			}
		});
	}
	
	@Test
	public void testUnsubscribeSuccessful() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAB\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(1L);
				assertThat(sizeOfCollection("devices")).isEqualTo(3L);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel")).isEqualTo(2);
				assertThat(numberOfChannelsSubscribedToByDevice("AAB", DeviceType.ios)).isEqualTo(0);
			}
		});
	}
	
	@Test
	public void testSubscribe() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAA\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(1L);
				assertThat(sizeOfCollection("devices")).isEqualTo(4L);
				assertThat(numberOfChannelsSubscribedToByDevice("AAA", DeviceType.ios)).isEqualTo(1);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel")).isEqualTo(4);
			}
		});
	}
	
	@Test 
	public void testSubscribeTwice() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAB\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(1L);
				assertThat(sizeOfCollection("devices")).isEqualTo(3L);
				assertThat(numberOfChannelsSubscribedToByDevice("AAB", DeviceType.ios)).isEqualTo(1);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel")).isEqualTo(3);
			}
		});
	}
	
	@Test
	public void testSubscribeBadDeviceType() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "buuuuuhh???")
							.post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAB\" }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				assertThat(sizeOfCollection("channels")).isEqualTo(1L);
				assertThat(sizeOfCollection("devices")).isEqualTo(3L);
				assertThat(numberOfChannelsSubscribedToByDevice("AAB", DeviceType.ios)).isEqualTo(1);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel")).isEqualTo(3);
			}
		});
	}
	
	@Test
	public void testSubscribeAndroid() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "android")
							.post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"2923923\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(1L);
				assertThat(sizeOfCollection("devices")).isEqualTo(4L);
				assertThat(numberOfChannelsSubscribedToByDevice("2923923", DeviceType.android)).isEqualTo(1);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel")).isEqualTo(4);
			}
		});
	}
	
	@Test
	public void testSubscribeWithDifferentDeviceTypeHavingSameToken() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "android")
							.post("{ \"channel\" : \"my_channel\" , \"deviceToken\" : \"AAB\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(1L);
				assertThat(sizeOfCollection("devices")).isEqualTo(4L);
				assertThat(numberOfChannelsSubscribedToByDevice("AAB", DeviceType.android)).isEqualTo(1);
				assertThat(numberOfChannelsSubscribedToByDevice("AAB", DeviceType.ios)).isEqualTo(1);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel")).isEqualTo(4);
			}
		});
	}
	
	@Test
	public void testSubscribeToNonExistentChannel() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel2\" , \"deviceToken\" : \"AAB\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(2L);
				assertThat(sizeOfCollection("devices")).isEqualTo(3L);
				assertThat(numberOfChannelsSubscribedToByDevice("AAB", DeviceType.ios)).isEqualTo(2);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel")).isEqualTo(3);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel2")).isEqualTo(1);
			}
		});
	}
	
	@Test
	public void testSubscribeToNonExistentChannelWithNonExistentDevice() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel2\" , \"deviceToken\" : \"AAA\" }")
							.get()
							.getStatus()
				).isEqualTo(OK);
				assertThat(sizeOfCollection("channels")).isEqualTo(2L);
				assertThat(sizeOfCollection("devices")).isEqualTo(4L);
				assertThat(numberOfChannelsSubscribedToByDevice("AAB", DeviceType.ios)).isEqualTo(1);
				assertThat(numberOfChannelsSubscribedToByDevice("AAA", DeviceType.ios)).isEqualTo(1);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel")).isEqualTo(3);
				assertThat(numberOfDevicesSubscribedToChannel("my_channel2")).isEqualTo(1);
			}
		});
	}
	
	@Test
	public void testNonJsonRequest() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(CONTENT_TYPE, "application/xml")
							.setHeader(USER_AGENT, "iPhone")
							.post("<some_xml></some_xml>")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				assertThat(
						unsubscribe()
							.setHeader(CONTENT_TYPE, "application/xml")
							.setHeader(USER_AGENT, "iPhone")
							.post("<some_xml></some_xml>")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				assertThat(
						publish()
							.setHeader(CONTENT_TYPE, "application/xml")
							.setHeader(USER_AGENT, "iPhone")
							.post("<some_xml></some_xml>")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
			}
		});
	}
	
	@Test
	public void testMalformedJsonRequest() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				assertThat(
						subscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("non-json post body")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				assertThat(
						unsubscribe()
							.setHeader(USER_AGENT, "iPhone")
							.post("non-json post body")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				assertThat(
						publish()
							.setHeader(USER_AGENT, "iPhone")
							.post("non-json post body")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
			}
		});
	}
	
	@Test
	public void testPublishInputs() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				//NO CHANNEL
				assertThat(publish()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"badge\": 0, \"alert\" : \"Push notification alert text\", \"messagePayload\" : { \"some_key\" : \"some_value\" } }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				//NO BADGE
				assertThat(publish()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel\", \"alert\" : \"Push notification alert text\", \"messagePayload\" : { \"some_key\" : \"some_value\" } }")
							.get(TIMEOUT)
							.getStatus()
				).isEqualTo(OK);
				
				//NON-INTEGER BADGE
				assertThat(publish()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel\", \"badge\": \"non-integer-value\", \"alert\" : \"Push notification alert text\", \"messagePayload\" : { \"some_key\" : \"some_value\" } }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				//NO ALERT
				assertThat(publish()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel\", \"badge\": 0, \"messagePayload\" : { \"some_key\" : \"some_value\" } }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				// NO MESSAGE PAYLOAD
				assertThat(publish()
							.setHeader(USER_AGENT, "iPhone")
							.post("{ \"channel\" : \"my_channel\",  \"badge\": 0, \"alert\" : \"Push notification alert text\" }")
							.get()
							.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				// NON-STRING MESSAGE CHANNEL
				assertThat(publish()
						.setHeader(USER_AGENT, "iPhone")
						.post("{ \"channel\" : 1,  \"badge\": 0, \"alert\" : \"Push notification alert text\", \"messagePayload\" : { \"some_key\" : \"some_value\" } }")
						.get()
						.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				// NON-JSON-OBJECT MESSAGE PAYLOAD
				assertThat(publish()
						.setHeader(USER_AGENT, "iPhone")
						.post("{ \"channel\" : \"my_channel\",  \"badge\": 0, \"alert\" : \"Push notification alert text\", \"messagePayload\" : \"1\" }")
						.get()
						.getStatus()
				).isEqualTo(BAD_REQUEST);
				
				// NON-STRING ALERT
				assertThat(publish()
						.setHeader(USER_AGENT, "iPhone")
						.post("{ \"channel\" : \"my_channel\",  \"badge\": 0, \"alert\" : 1, \"messagePayload\" : { \"some_key\" : \"some_value\" } }")
						.get()
						.getStatus()
				).isEqualTo(BAD_REQUEST);
			}
		});
	}
	
	@Test
	public void testPublishToNonExistentChannel() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				Response response = publish()
										.setHeader(USER_AGENT, "iPhone")
										.post("{ \"channel\" : \"non_existent_channel\",  \"badge\": 0, \"alert\" : \"Push notification alert text\", \"messagePayload\" : { \"some_key\" : \"some_value\" } }")
										.get();
				assertThat(response.getStatus()).isEqualTo(OK);
				assertThat(response.getBody()).isEqualTo("{\"success\":false,\"error\":\"Invalid channel\"}");
			}
		});
	}
	
	@Test
	public void testPublishSuccessful() {
		running(testServer(PORT), new Runnable() {
			public void run() {
				Response response = publish()
										.post("{ \"channel\" : \"my_channel\",  \"badge\": 0, \"alert\" : \"Push notification alert text\", \"messagePayload\" : { \"some_key\" : \"some_value\" } }")
										.get(TIMEOUT);
				assertThat(response.getStatus()).isEqualTo(OK);
				assertThat(response.getBody()).isEqualTo("{\"success\":true}");
			}
		});
	}
	
	// TODO: write publishing test cases!
	
}
