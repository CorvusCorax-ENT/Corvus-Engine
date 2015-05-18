package corvus.corax.util;

import java.io.InputStream;
import java.net.URL;

public class Resources {

	public static final URL fetch(String path) {
		return Resources.class.getResource("/"+path);
	}

	public static final InputStream fetchStream(String path) {
		return Resources.class.getResourceAsStream("/"+path);
	}

	public static final String fetchAsExternal(String path) {
		return Resources.class.getResource("/"+path).toExternalForm();
	}
}
