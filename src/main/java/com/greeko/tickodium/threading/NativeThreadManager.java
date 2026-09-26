package com.greeko.tickodium.threading;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class NativeThreadManager {

    static {
        loadNativeLibrary();
    }

    private static void loadNativeLibrary() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            String libName;

            // Detect OS to load the correct C++ compiled file
            if (osName.contains("win")) {
                libName = "tickodium_native.dll";
            } else if (osName.contains("nix") || osName.contains("nux")) {
                libName = "libtickodium_native.so";
            } else {
                throw new UnsupportedOperationException("Unsupported OS: " + osName);
            }

            // Extract the library from the resources folder inside the JAR
            try (InputStream input = NativeThreadManager.class.getResourceAsStream("/natives/" + libName)) {
                if (input == null) {
                    throw new RuntimeException("Native library not found in resources: /natives/" + libName);
                }

                File tempFile = File.createTempFile("tickodium_native_", libName.substring(libName.lastIndexOf('.')));
                tempFile.deleteOnExit();

                Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                // Load the extracted C++ native library
                System.load(tempFile.getAbsolutePath());
                System.out.println("[Tickodium] Successfully loaded native C++ library.");
            }
        } catch (Exception e) {
            System.err.println("[Tickodium] Failed to load native C++ library!");
            e.printStackTrace();
        }
    }

    // Native method declaration (implemented in C++)
    public static native void processShaderAsync();
}
