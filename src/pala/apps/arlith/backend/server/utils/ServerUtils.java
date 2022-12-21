package pala.apps.arlith.backend.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONParser;
import pala.libs.generic.json.JSONValue;
import pala.libs.generic.streams.CharacterStream;

public final class ServerUtils {
	private ServerUtils() {
	}

	public static JSONObject read(InputStream in) {
		return (JSONObject) new JSONParser()
				.parse(CharacterStream.from(new InputStreamReader(in, StandardCharsets.UTF_8)));
	}

	public static void write(OutputStream out, JSONValue json) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		writer.append(json.toString());
		writer.flush();
	}

	public static void writeConcise(OutputStream out, JSONValue json) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		writer.append(JSONValue.toStringShort(json));
		writer.flush();
	}
}
