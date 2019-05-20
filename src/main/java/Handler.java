import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<Request, String> {

	@Getter(lazy = true)
	private final Map<String, String> requestProperties = requestProperties();

	private LambdaLogger logger;

	@Override
	public String handleRequest(Request input, Context context) {
		if (logger == null) {
			logger = context.getLogger();
		}
		Document request = getRequest(input.getExampleUrl());
		if (request != null) {
			Document response = getResponse(request.toString().getBytes(), input.getServerUrl(), input.getActionUrl());
			if (response != null) {
				return response.selectFirst(input.getResponseTagName()).text();
			}
		}
		return null;
	}

	private Document getRequest(String requestExampleUrl) {
		try {
			File tempFile = File.createTempFile("request", ".xml");
			tempFile.deleteOnExit();
			ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(requestExampleUrl).openStream());
			FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			return Parser.xmlParser().parseInput(new String(Files.readAllBytes(tempFile.toPath())), "");
		} catch (Exception e) {
			logger.log(String.format("Exception occurred while getting request document at %s", requestExampleUrl));
			return null;
		}
	}

	private Document getResponse(byte[] requestBytes, String serverUrl, String actionUrl) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl).openConnection();
			requestProperties.forEach(connection::setRequestProperty);
			connection.setRequestProperty("Content-Length", String.valueOf(requestBytes.length));
			connection.setRequestProperty("SOAPAction", actionUrl);
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(requestBytes);
			outputStream.close();
			InputStream inputStream = connection.getInputStream();
			Document responseDocument = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), "");
			inputStream.close();
			connection.disconnect();
			return Parser.xmlParser().parseInput(responseDocument.html(), "");
		} catch (Exception e) {
			logger.log(String.format("Exception occurred while getting response document for %s", actionUrl));
			return null;
		}
	}

	private Map<String, String> requestProperties() {
		Map<String, String> map = new HashMap<>();
		map.put("Content-Type", "text/xml;charset=utf-8");
		map.put("User-Agent", "Apache-HttClient/4.1.1");
		map.put("Accept-Encoding", "gzip,deflate");
		map.put("Connection", "Keep-Alive");
		return map;
	}

}