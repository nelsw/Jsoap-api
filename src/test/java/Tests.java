import lambda.MockLambda;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Tests {

	private final Handler handler = new Handler();
	private MockLambda context;

	private Request input;

	@Before
	public void before() {
		input = new Request();
		input.setExampleUrl("https://graphical.weather.gov/xml/docs/SOAP_Requests/LatLonListZipCode.xml");
		input.setEndpointUrl("https://graphical.weather.gov:443/xml/SOAP_server/ndfdXMLserver.php");
		Map<String, String> requestParameters = new HashMap<>();
		requestParameters.put("listZipCodeList", "20910 25414");
		input.setRequestParameters(requestParameters);
		input.setResponseTagName("listlatlonout");
		input.setEncodedResponseTagName("latlonlist");
		input.setCharsetName("ISO-8859-1");
		input.setHost("graphical.weather.gov:443");
		context = new MockLambda();
	}

	@Test
	public void validRequest_handRequest_returnValidJson() {
		assertNotNull(handler.handleRequest(input, context));
	}

	@Test
	public void invalidEndpointUrl_handleRequest_returnNull() {
		input.setEndpointUrl(null);
		assertNull(handler.handleRequest(input, context));
	}

	@Test
	public void invalidActionUrl_handleRequest_returnNull() {
		input.getRequestParameters().put("", "");
		assertNull(handler.handleRequest(input, context));
	}

	@Test(expected = NullPointerException.class)
	public void nullRequest_handleRequest_throwsException() {
		handler.handleRequest(null, context);
	}

}