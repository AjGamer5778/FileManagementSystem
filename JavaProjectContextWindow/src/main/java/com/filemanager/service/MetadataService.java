
package com.filemanager.service;

import com.filemanager.model.FileMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class MetadataService {

    public FileMetadata read(Path path) throws IOException {

        if (path == null ||
            !Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("File does not exist.");
        }

        BasicFileAttributes attrs = Files.readAttributes(
            path,
            BasicFileAttributes.class,
            LinkOption.NOFOLLOW_LINKS
        );

        Path fileName = path.getFileName();

        String name = fileName == null
            ? path.toString()
            : fileName.toString();

        boolean directory = attrs.isDirectory();
        boolean symbolicLink = attrs.isSymbolicLink();

        String extension = "";

        if (!directory && !symbolicLink) {
            int dot = name.lastIndexOf('.');

            if (dot > 0 && dot < name.length() - 1) {
                extension = name.substring(dot + 1);
            }
        }

        String type;

        if (symbolicLink) {
            type = "Symbolic link";
        } else if (directory) {
            type = "Folder";
        } else if (!extension.isEmpty()) {
            type = extension.toUpperCase() + " file";
        } else {
            type = "File";
        }

        return new FileMetadata(
            name,
            type,
            extension,
            path.toAbsolutePath().normalize(),
            attrs.size(),
            attrs.creationTime().toInstant(),
            attrs.lastModifiedTime().toInstant(),
            attrs.lastAccessTime().toInstant(),
            directory,
            Files.isHidden(path),
            Files.isReadable(path),
            Files.isWritable(path),
            Files.isExecutable(path)
        );
    }
}
