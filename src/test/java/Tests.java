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
	private final MockLambda context = new MockLambda();

	private Request input;

	@Before
	public void before() {
		input = new Request();
		input.setExampleUrl("https://graphical.weather.gov/xml/docs/SOAP_Requests/LatLonListZipCode.xml");
		input.setServerUrl("https://graphical.weather.gov:443/xml/SOAP_server/ndfdXMLserver.php");
		input.setActionUrl("https://graphical.weather.gov:443/xml/DWMLgen/wsdl/ndfdXML.wsdl#LatLonListZipCode");
		Map<String, String> requestParameters = new HashMap<>();
		requestParameters.put("listZipCodeList", "20910 25414");
		input.setRequestParameters(requestParameters);
		input.setResponseTagName("listlatlonout");
		input.setHost("graphical.weather.gov:443");
	}

	@Test
	public void foo() throws Exception {
		File tempFile = File.createTempFile("input", ".xml");
		tempFile.deleteOnExit();

		URL url = new URL("https://graphical.weather.gov/xml/docs/SOAP_Requests/LatLonListZipCode.xml");

		ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());

		FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

		fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

		Document document = Parser.xmlParser().parseInput(new String(Files.readAllBytes(tempFile.toPath())), "");

		System.out.println(document);

		String value = document.selectFirst("listZipCodeList").text();

		System.out.println(value);
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