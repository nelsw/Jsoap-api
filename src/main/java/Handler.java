import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import javax.net.ssl.HttpsURLConnection;
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

public class Handler implements RequestHandler<Request, String> {

	private LambdaLogger logger;

	@Override
	public String handleRequest(Request input, Context context) {
		if (logger == null) {
			logger = context.getLogger();
		}
		Document request = getRequest(input.getExampleUrl());
		if (request != null) {
			System.out.println(request);
			Document response = getResponse(request.toString().getBytes(), input.getServerUrl(), input.getActionUrl(), input.getHost());
			if (response != null) {
				System.out.println(response);
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

	private Document getResponse(byte[] requestBytes, String serverUrl, String actionUrl, String host) {
		try {
			HttpsURLConnection connection = (HttpsURLConnection) new URL(serverUrl).openConnection();
			connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.151 Safari/535.19");
			connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Length", String.valueOf(requestBytes.length));
			connection.setRequestProperty("SOAPAction", actionUrl);
			connection.setRequestProperty("Host", host);
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(10_000);
			connection.setReadTimeout(10_000);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(requestBytes);
			outputStream.close();
			InputStream inputStream = connection.getInputStream();
			Document responseDocument = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), "");
			System.out.println(responseDocument.toString());
			inputStream.close();
			connection.disconnect();
			return Parser.xmlParser().parseInput(responseDocument.html(), "");
		} catch (Exception e) {
			logger.log(String.format("Exception occurred while getting response document for %s", actionUrl));
			e.printStackTrace();
			return null;
		}
	}

}