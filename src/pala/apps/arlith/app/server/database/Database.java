package pala.apps.arlith.app.server.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import pala.apps.arlith.app.server.contracts.world.ServerWorld;

/**
 * <p>
 * A {@link Database} represents a storage medium for use by a {@link ServerWorld}
 * API implementation. Each database can store Files and other Databases, each
 * named. {@link Database}s provide the benefit over a filesystem of not
 * requiring explicit file/folder creation.
 * </p>
 * <p>
 * A database file is an arbitrary-size database object that can store arbitrary
 * byte data, analogous to filesystem files. Database files can be written to or
 * read from using {@link #writer(String)} and {@link #appendWriter(String)} or
 * {@link #reader(String)}.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface Database {
	/**
	 * Returns a {@link Database} with the specified name under this database. The
	 * returned database may not necessarily be an immediate child of this
	 * {@link Database} if a <code>/</code> character is used to refer to a nested
	 * child of this database.
	 * 
	 * @param path The path under this database. Analogous to relative paths in the
	 *             filesystem.
	 * @return The new {@link Database}.
	 */
	Database subDatabase(String path);

	/**
	 * Returns an {@link OutputStream} that writes to the specified database file. A
	 * call to this method creates the database file.
	 * 
	 * @param filename The name of the database file to write to.
	 * @return An {@link OutputStream} that writes to the specified database file.
	 * @throws IOException If an {@link IOException} occurs while creating the
	 *                     {@link OutputStream}.
	 */
	OutputStream writer(String filename) throws IOException;

	/**
	 * Returns an {@link OutputStream} that appends to the specified database file.
	 * A call to this method creates the database file.
	 * 
	 * @param filename The name of the database file to write to.
	 * @return An {@link OutputStream} that appends to the specified database file.
	 * @throws IOException If an {@link IOException} occurs while creating the
	 *                     {@link OutputStream}.
	 */
	OutputStream appendWriter(String filename) throws IOException;

	/**
	 * Returns an {@link InputStream} that reads from the beginning of the specified
	 * database file. If the database file does not exist, this method throws an
	 * {@link IOException}.
	 * 
	 * @param filename The name of the database file to read from.
	 * @return A new {@link InputStream} that reads from the specified database
	 *         file.
	 * @throws IOException If an {@link IOException} occurs while creating the
	 *                     {@link InputStream}.
	 */
	InputStream reader(String filename) throws IOException;

	/**
	 * Lists the names of all the files and databases immediately contained inside
	 * this {@link Database}. The returned array has no duplicate entries; if an
	 * immediate child file and database have the same name, the name is only listed
	 * once.
	 * 
	 * @return The names of each file and database immediately contained in this
	 *         {@link Database}.
	 */
	default String[] list() {
		return null;
	}

	/**
	 * Returns the names of each existing database immediately contained in this
	 * {@link Database}.
	 * 
	 * @return An array containing the names of all the sub-databases immediately
	 *         contained in this database.
	 */
	String[] listSubDatabases();

	/**
	 * Lists all the database files immediately inside this {@link Database}.
	 * 
	 * @return An array containing the names of all the database files immediately
	 *         contained in this database.
	 */
	String[] listFiles();

	/**
	 * Creates a filesystem database from the specified directory.
	 * 
	 * @param file A {@link File} representing the directory to create the database
	 *             in.
	 * @return The new {@link Database}.
	 */
	static Database filesystem(File file) {
		return new FilesystemDatabase(file);
	}

	/**
	 * Returns <code>true</code> if there exists a database file with the specified
	 * name in this {@link Database}.
	 * 
	 * @param filename The name of the file.
	 * @return <code>true</code> if there is such a file. <code>false</code>
	 *         otherwise.
	 */
	boolean isFile(String filename);

	/**
	 * Returns <code>true</code> if there exists a sub-database with the specified
	 * name.
	 * 
	 * @param database The name of the database.
	 * @return <code>true</code> if there is such a database. <code>false</code>
	 *         otherwise.
	 */
	boolean isDatabase(String database);

	/**
	 * Deletes any sub-database files with the specified names. If all of the
	 * children of a database are removed, the {@link Database} is automatically
	 * removed as well.
	 * 
	 * @param files The names of the files in this database to remove.
	 * @throws IOException If an {@link IOException} occurs.
	 */
	void clear(String... files) throws IOException;

	RandomAccessFile raf(String filename, String mode) throws IOException;

}
