import lambda.MockLambda;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class Tests {

	private final Handler handler = new Handler();
	private MockLambda context;

	private Request input;

	@Before
	public void before() {
		input = new Request();
		input.setExampleUrl("https://graphical.weather.gov/xml/docs/SOAP_Requests/LatLonListZipCode.xml");
		input.setEndpointUrl("https://graphical.weather.gov:443/xml/SOAP_server/ndfdXMLserver.php");
		input.setActionUrl("https://graphical.weather.gov:443/xml/DWMLgen/wsdl/ndfdXML.wsdl#LatLonListZipCode");
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
	public void invalidExampleUrl_handleRequest_returnNull() {

	}

	@Test
	public void invalidServerUrlOrActionUrl_handleRequest_returnNull() {

	}

}