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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

public class Handler implements RequestHandler<Request, String> {


	private final String defaultUserAgent;
	private final String tempFilePrefix;
	private final String tempFileSuffix;
	private final String requestExceptionMsg;
	private final String responseExceptionMsg;
	private final String requestMethod;
	private final String encodingKey;
	private final String encodingVal;
	private final String contentTypeKey;
	private final String contentTypeVal;
	private final String contentLengthKey;
	private final String hostKey;
	private final String userAgentKey;
	private final Boolean doOutput;

	private LambdaLogger logger;

	public Handler() {
		defaultUserAgent = "Apache-HttpClient/4.1.1 (java 1.8)";
		tempFilePrefix = "request";
		tempFileSuffix = ".xml";
		requestExceptionMsg = "Exception occurred while getting request document at %s";
		responseExceptionMsg = "Exception occurred while getting response document from %s";
		requestMethod = "POST";
		encodingVal = "gzip,deflate";
		encodingKey = "Accept-Encoding";
		contentTypeKey = "Content-Type";
		contentTypeVal = "text/xml;charset=";
		contentLengthKey = "Content-Length";
		hostKey = "Host";
		userAgentKey = "User-Agent";
		doOutput = true;
	}

	@Override
	public String handleRequest(Request input, Context context) {
		if (logger == null) {
			logger = context.getLogger();
		}
		Document request = getRequest(input.getExampleUrl(), input.getRequestParameters());
		if (request != null) {
			Document response = getResponse(
					request.toString().getBytes(),
					input.getEndpointUrl(),
					input.getHost(),
					input.getCharsetName(),
					Objects.toString(input.getUserAgent(), defaultUserAgent));
			if (response != null) {
				String result = response.selectFirst(input.getResponseTagName()).text();
				if (input.getEncodedResponseTagName() != null) {
					result = Jsoup.parse(result).selectFirst(input.getEncodedResponseTagName()).text();
				}
				return result;
			}
		}
		return null;
	}

	private Document getRequest(String requestExampleUrl, Map<String, String> requestParameters) {
		try {
			File file = File.createTempFile(tempFilePrefix, tempFileSuffix);
			file.deleteOnExit();
			ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(requestExampleUrl).openStream());
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			Document document = Parser.xmlParser().parseInput(new String(Files.readAllBytes(file.toPath())), "");
			document.outputSettings().prettyPrint(false);
			requestParameters.forEach((k, v) -> document.selectFirst(k).text(v));
			return document;
		} catch (Exception e) {
			logger.log(String.format(requestExceptionMsg, requestExampleUrl));
			return null;
		}
	}

	private Document getResponse(byte[] bytes, String endpoint, String host, String charsetName, String agent) {
		try {
			HttpURLConnection conn = (HttpsURLConnection) new URL(endpoint).openConnection();
			conn.setRequestProperty(encodingKey, encodingVal);
			conn.setRequestProperty(contentTypeKey, contentTypeVal + charsetName);
			conn.setRequestProperty(contentLengthKey, String.valueOf(bytes.length));
			conn.setRequestProperty(hostKey, host);
			conn.setRequestProperty(userAgentKey, agent);
			conn.setRequestMethod(requestMethod);
			conn.setDoOutput(doOutput);

			OutputStream dataOutputStream = conn.getOutputStream();
			dataOutputStream.write(bytes);
			dataOutputStream.flush();
			dataOutputStream.close();

			InputStream inputStream = conn.getInputStream();
			Document document = Jsoup.parse(inputStream, Charset.forName(charsetName).name(), endpoint);
			inputStream.close();
			conn.disconnect();
			return document;
		} catch (Exception e) {
			logger.log(String.format(responseExceptionMsg, endpoint));
			return null;
		}
	}

}