package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ImageUtils {

    // Ensure this directory exists in the actual filesystem
    private static final String SNACK_IMAGE_DIR = "data/snack/";

    public static String copyImage(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }

        Path destDir = Paths.get(SNACK_IMAGE_DIR);
        if (!Files.exists(destDir)) {
            Files.createDirectories(destDir);
        }

        // Generate unique filename to avoid overwrites
        String originalName = sourceFile.getName();
        String extension = "";
        int i = originalName.lastIndexOf('.');
        if (i > 0) {
            extension = originalName.substring(i);
        }
        
        String newFilename = UUID.randomUUID().toString() + extension;
        Path destPath = destDir.resolve(newFilename);

        Files.copy(sourceFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path to be saved in DB
        return SNACK_IMAGE_DIR + newFilename;
    }
}
