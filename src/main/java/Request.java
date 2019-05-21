import lombok.Data;

import java.util.Map;

@Data
class Request {

	private String exampleUrl;
	private String serverUrl;
	private String actionUrl;
	private String host;
	private Map<String, String> requestParameters;

	private String responseTagName;

}