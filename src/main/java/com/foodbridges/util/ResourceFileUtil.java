package com.foodbridges.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceFileUtil {

    public static File copyResourceToTempFile(String resourcePath, String prefix, String suffix) throws IOException {

        InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath);

        if (in == null) {
            throw new FileNotFoundException("❌ Resource not found in classpath: " + resourcePath +
                    " (Expected under src/main/resources/)");
        }

        Path temp = Files.createTempFile(prefix, suffix);
        temp.toFile().deleteOnExit();

        try (OutputStream out = Files.newOutputStream(temp)) {
            in.transferTo(out);
        } finally {
            in.close();
        }

        return temp.toFile();
    }
}
