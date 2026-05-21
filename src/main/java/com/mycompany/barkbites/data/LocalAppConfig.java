package com.mycompany.barkbites.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Small local (per-machine) config store to reduce setup.
 *
 * Stored at: {user.home}/.barkbites/app.properties
 * This file should NOT be committed to the repo.
 */
public final class LocalAppConfig {

    private static final String DIR_NAME = ".barkbites";
    private static final String FILE_NAME = "app.properties";

    private LocalAppConfig() {
    }

    public static Path configFilePath() {
        return Path.of(System.getProperty("user.home"), DIR_NAME, FILE_NAME);
    }

    public static Properties load() {
        Properties props = new Properties();
        Path file = configFilePath();
        if (!Files.isRegularFile(file)) {
            return props;
        }
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (IOException ignored) {
        }
        return props;
    }

    public static void save(Properties props) throws IOException {
        Path file = configFilePath();
        Files.createDirectories(file.getParent());
        try (OutputStream out = Files.newOutputStream(file)) {
            props.store(out, "BarkBites local config (do not commit)");
        }
    }
}
