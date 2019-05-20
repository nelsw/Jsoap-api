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

public class Tests {

	private final Handler handler = new Handler();
	private final MockLambda context = new MockLambda();

	private Request request;

	@Before
	public void before() {
		request = new Request();
		request.setExampleUrl("");
		request.setServerUrl("");
		request.setActionUrl("");
		Map<String, String> requestParameters = new HashMap<>();
		requestParameters.put("listZipCodeList", "20910 25414");
		request.setRequestParameters(requestParameters);
		request.setResponseTagName("lizZipCodeList");
	}

	@Test
	public void foo() throws Exception {
		File tempFile = File.createTempFile("request", ".xml");
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

	}

	@Test
	public void invalidExampleUrl_handleRequest_returnNull() {

	}

	@Test
	public void invalidServerUrlOrActionUrl_handleRequest_returnNull() {

	}

}