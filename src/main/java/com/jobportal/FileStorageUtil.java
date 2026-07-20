package com.jobportal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

/** Small, shared helper for files kept inside the deployed web application. */
public final class FileStorageUtil {
    private FileStorageUtil() { }

    public static String save(Part part, ServletContext context, String folder,
                              Set<String> allowedExtensions, long maxBytes) throws IOException {
        if (part == null || part.getSize() == 0) {
            throw new IllegalArgumentException("Please choose a file to upload");
        }
        if (part.getSize() > maxBytes) {
            throw new IllegalArgumentException("The file must be smaller than 5 MB");
        }

        String originalName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
        int dot = originalName.lastIndexOf('.');
        String extension = dot >= 0 ? originalName.substring(dot).toLowerCase(Locale.ROOT) : "";
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("That file type is not allowed");
        }

        String directoryPath = context.getRealPath("/" + folder);
        if (directoryPath == null) {
            throw new IOException("File uploads require an exploded web application directory");
        }
        File directory = new File(directoryPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Unable to create the upload directory");
        }

        String storedName = UUID.randomUUID() + extension;
        part.write(new File(directory, storedName).getAbsolutePath());
        return storedName;
    }

    public static String originalName(Part part) {
        if (part == null || part.getSize() == 0 || part.getSubmittedFileName() == null) {
            throw new IllegalArgumentException("Please choose a file to upload");
        }
        return Paths.get(part.getSubmittedFileName()).getFileName().toString();
    }
}
