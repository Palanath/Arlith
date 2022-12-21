package pala.apps.arlith.backend.streams;

import java.io.IOException;
import java.net.Socket;

public interface IOStream extends InputStream, OutputStream {

	static int kilobytes(int kb) {
		return kb * 1024;
	}

	static int megabytes(int mb) {
		return kilobytes(mb) * 1024;
	}

	static int gigabytes(int gb) {
		return megabytes(gb) * 1024;
	}

	static int kb(int kb) {
		return kilobytes(kb);
	}

	static int mb(int mb) {
		return megabytes(mb);
	}

	static int gb(int gb) {
		return gigabytes(gb);
	}

	@Override
	default void close() throws IOException {
		InputStream.super.close();
		OutputStream.super.close();
	}

	static IOStream fromIOStreams(InputStream in, OutputStream out) {
		return new IOStream() {

			@Override
			public void write(int b) throws IOException {
				out.write(b);
			}

			@Override
			public void write(byte[] arr) throws IOException {
				out.write(arr);
			}

			@Override
			public void write(byte[] arr, int offset, int length) throws IOException {
				out.write(arr, offset, length);
			}

			@Override
			public void flush() throws IOException {
				out.flush();
			}

			@Override
			public void close() throws IOException {
				in.close();
				out.close();
			}

			@Override
			public int read() throws IOException {
				return in.read();
			}

			@Override
			public int read(byte[] arr) throws IOException {
				return in.read(arr);
			}

			@Override
			public int read(byte[] bytes, int offset, int len) throws IOException {
				return in.read(bytes, offset, len);
			}
		};
	}

	static IOStream fromIOStreams(java.io.InputStream in, java.io.OutputStream out) {
		return fromIOStreams(InputStream.fromJavaInputStream(in), OutputStream.fromJavaOutputStream(out));
	}

	static IOStream fromSocket(Socket socket) throws IOException {
		return fromIOStreams(socket.getInputStream(), socket.getOutputStream());
	}
}
