import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
class Request {

	String exampleUrl;
	String endpointUrl;
	String host;
	String charsetName;
	String userAgent;

	String responseTagName;
	String encodedResponseTagName;
}