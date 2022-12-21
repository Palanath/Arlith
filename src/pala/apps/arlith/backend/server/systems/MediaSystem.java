package pala.apps.arlith.backend.server.systems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.gids.GIDProvider;
import pala.apps.arlith.backend.networking.BlockException;
import pala.apps.arlith.backend.networking.Connection;
import pala.apps.arlith.backend.networking.UnknownCommStateException;
import pala.apps.arlith.backend.server.ArlithServer;

/**
 * Stores and tracks all of the media for a {@link ArlithServer}.
 * 
 * @author Palanath
 *
 */
public class MediaSystem {
	private final File location;
	private final GIDProvider provider = new GIDProvider();

	/**
	 * <p>
	 * Creates a new {@link MediaSystem} with the specified {@link File} location as
	 * the storage directory. This location is typically a subdirectory under the
	 * {@link ArlithServer}'s root data directory named something such as
	 * <code>media</code>, but can be anywhere. It should be consistent between runs
	 * of the same server.
	 * </p>
	 * <p>
	 * This constructor does not load up any media from the hard disk when it's
	 * invoked. Loading is done only when media is requested.
	 * </p>
	 * 
	 * @param location The {@link File} specifying the directory where media is
	 *                 stored for the server.
	 */
	public MediaSystem(File location) {
		this.location = location;
	}

	public enum MediaType {
		PFP, COMMUNITY_BACKGROUND, COMMUNITY_ICON, EMOJI;

		private final String subdir;

		private MediaType() {
			subdir = name().toLowerCase();
		}

		private MediaType(String subdir) {
			this.subdir = subdir;
		}

		/**
		 * Gets the subdirectory pathname that media of this type is stored in. Media is
		 * stored in {@link MediaSystem#location}/[user-id]/{@link #getSubdir()}, where
		 * [user-id] is the {@link GID} of the user that uploaded the media, in hex. See
		 * {@link MediaSystem#getPreciseDir(File, GID, MediaType)} for more details.
		 * 
		 * @return The subdirectory path fragment for this type of media.
		 */
		private String getSubdir() {
			return subdir;
		}
	}

	/**
	 * <p>
	 * Uploads the specified media, of the specified type, by the specified user.
	 * The media can be later retrieved using {@link #getMedia(GID, MediaType, GID)}
	 * and can be deleted using {@link #deleteMedia(GID, MediaType, GID)}.
	 * </p>
	 * <p>
	 * Once the media is uploaded, a GID is returned which can be used to refer to
	 * the media alongside the ID of the uploader user and the type of media.
	 * </p>
	 * <p>
	 * {@link #getPreciseDir(File, GID, MediaType)} is used to pick the directory
	 * where the media is stored
	 * </p>
	 * 
	 * @param userID       The {@link GID} of the user that uploaded this media.
	 * @param type         The type of media being uploaded. This will change what
	 *                     directory, under the uploading user's media directory,
	 *                     the media actually gets stored in.
	 * @param variableSize Determines whether the media in the {@link Connection} is
	 *                     variable in size.
	 * @param media        A connection from which the media will be read. The media
	 *                     is read via
	 *                     {@link Connection#readVariableBlock(pala.apps.arlith.backend.streams.OutputStream)}
	 *                     if <code>variableSize</code> is <code>true</code> and is
	 *                     read via {@link Connection#readBlockLong()} if
	 *                     <code>variableSize</code> is <code>false</code>.
	 * @return A {@link GID} referring to the media.
	 * @throws Exception If an {@link IOException},
	 *                   {@link UnknownCommStateException}, or
	 *                   {@link BlockException} occurs.
	 */
	public GID uploadMedia(GID userID, MediaType type, boolean variableSize, Connection media) throws Exception {
		File dir = getPreciseDir(location, userID, type);
		dir.mkdirs();
		GID g = provider.generateGid();
		File m = new File(dir, g.getHex() + ".png");// For now, everything will just have the png extension.
		// This is stupid, but I'm doing it so that I can see image previews on my hard
		// drive, in case for some reason I need to find something for someone.
		// Later, I will probably have them just be extensionless (or have some global
		// extension), or have them be better in some other way.

		try (FileOutputStream fos = new FileOutputStream(m)) {
			if (variableSize)
				media.readVariableBlock(pala.apps.arlith.backend.streams.OutputStream.fromJavaOutputStream(fos));
			else
				fos.write(media.readBlockLong());
		} catch (IOException | UnknownCommStateException e) {
			throw e;
		}

		return g;
	}

	/**
	 * Returns a {@link File} pointing to the media requested if such media was
	 * found. Otherwise, returns <code>null</code>.
	 * 
	 * @param userID   The {@link GID} of the user that uploaded the media.
	 * @param type     The type of media.
	 * @param mediaKey The media's media key.
	 * @return The {@link File} pointing to the media, if it exists. Otherwise,
	 *         <code>null</code>.
	 */
	public File getMedia(GID userID, MediaType type, GID mediaKey) {
		File media = getPreciseFile(location, userID, type, mediaKey);
		return media.isFile() ? media : null;
	}

	/**
	 * Note that this will not delete <i>references</i> to the media in Application.
	 * 
	 * @param userID   The {@link GID} of the user that uploaded the media.
	 * @param type     The type of media.
	 * @param mediaKey The media's media key.
	 * @return <code>true</code> if the media was found, <code>false</code>
	 *         otherwise.
	 */
	public boolean deleteMedia(GID userID, MediaType type, GID mediaKey) {
		File f = getMedia(userID, type, mediaKey);
		return f != null && f.delete();
	}

	/**
	 * Collects a new {@link Set} of all the media of the specified type uploaded by
	 * the user with the specified {@link GID} that has not yet been
	 * {@link #deleteMedia(GID, MediaType, GID) deleted}.
	 * 
	 * @param userID The GId of the user to query the media of.
	 * @param type   The type of media to query.
	 * @return A new, modifiable {@link Set} containing all of the {@link File}s of
	 *         the media uploaded. Changing the {@link Set} itself does not affect
	 *         the media at all.
	 */
	public Set<File> listMedia(GID userID, MediaType type) {
		File[] f = getPreciseDir(location, userID, type).listFiles();
		if (f == null)
			return new HashSet<>();
		else {
			Set<File> files = new HashSet<>();
			for (File a : f)
				files.add(a);
			return files;
		}
	}

	/**
	 * <p>
	 * Gets the folder in which a piece of media would be stored given the media
	 * directory (the value of {@link #location} for a given {@link MediaSystem}),
	 * the {@link GID} of the user that uploads the media, and the {@link MediaType
	 * type of media} being uploaded.
	 * </p>
	 * <p>
	 * The {@link GID#getHex() hex form} of the user's {@link GID} is used in the
	 * actual pathname.
	 * </p>
	 * 
	 * @param rootMediaDirectory The directory that the {@link MediaSystem} storing
	 *                           the file is operating in. This is the {@link File}
	 *                           provided to {@link #MediaSystem(File)}, and is the
	 *                           value of {@link #location}, for a given
	 *                           {@link MediaSystem}.
	 * @param userID             The {@link GID} of the user uploading the media.
	 * @param mediaType          The type of media being uploaded.
	 * @return A {@link File} object pointing to the directory that the media would
	 *         be stored inside.
	 */
	private static File getPreciseDir(File rootMediaDirectory, GID userID, MediaType mediaType) {
		return new File(rootMediaDirectory, userID.getHex() + '/' + mediaType.getSubdir());
	}

	private static File getPreciseFile(File rootMediaDirectory, GID userID, MediaType mediaType, GID mediaKey) {
		return new File(getPreciseDir(rootMediaDirectory, userID, mediaType), mediaKey.getHex() + ".png");
	}

}
