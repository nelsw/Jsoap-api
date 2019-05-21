import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
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
			request.outputSettings().prettyPrint(false);
			byte[] b = null;
			try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
				bout.write(request.toString().getBytes());
				b = bout.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Document response = getResponse(b, input.getServerUrl(), input.getActionUrl(), input.getHost());
			if (response != null) {
				System.out.println(response);
				String result = response.selectFirst(input.getResponseTagName()).text();
				System.out.println(result);
				return result;
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
			HttpURLConnection connection = (HttpsURLConnection) new URL(serverUrl).openConnection();
			connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			connection.setRequestProperty("Content-Type", "text/xml;charset=ISO-8859-1");
			connection.setRequestProperty("Content-Length", String.valueOf(requestBytes.length));
//			connection.setRequestProperty("SOAPAction", actionUrl);
			connection.setRequestProperty("Host", host);
//			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("User-Agent", "Apache-HttpClient/4.1.1 (java 1.5)");
//			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestMethod("POST");
//			connection.setConnectTimeout(10_000);
//			connection.setReadTimeout(10_000);
			connection.setDoOutput(true);
			OutputStream outputStream = connection.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			dataOutputStream.write(requestBytes);
			dataOutputStream.flush();
			dataOutputStream.close();
			int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				String s;
				while ((s = bufferedReader.readLine()) != null) {
					System.out.println(s);
				}
				bufferedReader.close();
			}
			InputStream inputStream = connection.getInputStream();
			Document responseDocument = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), "");
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