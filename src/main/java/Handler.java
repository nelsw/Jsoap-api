import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.jsoap.Jsoap;
import org.jsoap.Request;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class Handler implements RequestStreamHandler {

    private final Gson gson;
    private final Type type;

    public Handler() {
        gson = new Gson();
        type = new TypeToken<Map<String, Object>>(){}.getType();
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            Map<String, Object> m = gson.fromJson(reader, type);
            String body
                    = (Boolean) m.get("isBase64Encoded")
                    ? new String(Base64.getDecoder().decode(m.get("body").toString().getBytes()))
                    : m.get("body").toString();
            String json = Jsoap.getInstance().send(body);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("body", json);
            jsonObject.addProperty("isBase64Encoded", false);
            JsonObject headers = new JsonObject();
            headers.addProperty("Access-Control-Allow-Origin", "*");
            jsonObject.add("headers", headers);
            jsonObject.addProperty("statusCode", json.startsWith("error=") ? 400 : 200);
            OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
            writer.write(jsonObject.toString());
            writer.close();
        }
    }

}