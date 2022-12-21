package pala.apps.arlith.backend.server.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;

public class FilesystemDatabase implements Database {

	private final File dir;

	public FilesystemDatabase(File dir) {
		this.dir = dir;
	}

	@Override
	public Database subDatabase(String path) {
		return new FilesystemDatabase(new File(dir, path));
	}

	@Override
	public OutputStream writer(String filename) throws IOException {
		dir.mkdirs();
		return new FileOutputStream(new File(dir, filename));
	}

	@Override
	public InputStream reader(String filename) throws IOException {
		return new FileInputStream(new File(dir, filename));
	}

	@Override
	public String[] list() {
		return dir.isDirectory() ? dir.list() : new String[0];
	}

	@Override
	public boolean isFile(String filename) {
		return dir.isDirectory() && new File(dir, filename).isFile();
	}

	@Override
	public OutputStream appendWriter(String filename) throws IOException {
		dir.mkdirs();
		return new FileOutputStream(new File(dir, filename), true);
	}

	public RandomAccessFile raf(String filename, String mode) throws IOException {
		return new RandomAccessFile(new File(dir, filename), mode);
	}

	@Override
	public String[] listSubDatabases() {
		return dir.list((dir, name) -> new File(dir, name).isDirectory());
	}

	@Override
	public String[] listFiles() {
		return dir.list((dir, name) -> new File(dir, name).isFile());
	}

	@Override
	public boolean isDatabase(String database) {
		return new File(dir, database).isDirectory();
	}

	@Override
	public void clear(String... files) throws IOException {
		Exception e = null;
		for (String s : files)
			try {
				Files.deleteIfExists(new File(dir, s).toPath());
			} catch (IOException ex) {
				if (e == null)
					e = ex;
				else
					e.addSuppressed(ex);
			}
		try {
			File dir = this.dir;
			while (dir.list().length == 0) {
				Files.delete(dir.toPath());
				dir = dir.getParentFile();
			}
		} catch (IOException ex) {
			if (e == null)
				e = ex;
			else
				e.addSuppressed(ex);
		}
	}

}
