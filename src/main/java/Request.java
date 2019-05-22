import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
class Request {

	String exampleUrl;
	String endpointUrl;
	String host;
	String charsetName;
	String userAgent;

	Map<String, String> requestParameters;

	String responseTagName;
	String encodedResponseTagName;
}