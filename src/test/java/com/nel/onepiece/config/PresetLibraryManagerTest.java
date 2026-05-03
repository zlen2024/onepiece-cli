package com.nel.onepiece.config;

import com.nel.onepiece.model.presets.PresetLibrary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PresetLibraryManagerTest {

    @Test
    void createsDefaultsAndPersists(@TempDir Path tempHome) throws Exception {
        String oldHome = System.getProperty("user.home");
        System.setProperty("user.home", tempHome.toString());
        try {
            ConfigManager configManager = new ConfigManager();
            PresetLibraryManager manager = new PresetLibraryManager();
            inject(manager, "configManager", configManager);

            PresetLibrary library = manager.loadOrCreate();
            assertNotNull(library);
            assertTrue(Files.exists(manager.presetsFilePath()));
            assertTrue(!library.getAgents().isEmpty());
            assertTrue(!library.getSkills().isEmpty());
            assertTrue(!library.getMcps().isEmpty());

            manager.exportTemplates(library);
            assertTrue(Files.exists(manager.templatesDirPath().resolve("index.json")));
        } finally {
            if (oldHome != null) {
                System.setProperty("user.home", oldHome);
            }
        }
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}

