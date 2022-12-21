package pala.apps.arlith.frontend.server.contracts.coldstorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONParser;
import pala.libs.generic.json.JSONValue;
import pala.libs.generic.streams.CharacterStream;

public interface FilesystemStorageObject extends Snapshottable, AbstractFilesystemStorageObject {

	@Override
	default void save() {
		if (!getStorageFile().getParentFile().isDirectory())
			getStorageFile().getParentFile().mkdirs();
		JSONObject snapshot = snapshot();
		try (PrintWriter pw = new PrintWriter(getStorageFile())) {
			pw.print(JSONValue.toStringShort(snapshot));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	default boolean restore() throws IllegalArgumentException {
		if (!getStorageFile().isFile())
			return false;
		try {
			restore((JSONObject) new JSONParser()
					.parse(CharacterStream.from(new InputStreamReader(new FileInputStream(getStorageFile())))));
			return true;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	default void deleteFile() {
		File sf = getStorageFile();
		if (!sf.delete())
			try {
				System.out.println("Failed to delete file: " + sf.getCanonicalPath());
			} catch (IOException e) {
				System.out.println("Failed to delete file: " + sf.getAbsolutePath()
						+ "\n\tALSO failed to get the canonical path when printing the error message. Stacktrace for canonical path get failure:");
				e.printStackTrace();
			}
	}

}
