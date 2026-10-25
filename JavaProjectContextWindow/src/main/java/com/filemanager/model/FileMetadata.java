
package com.filemanager.model;

import java.nio.file.Path;
import java.time.Instant;

public class FileMetadata {

    private final String name;
    private final String type;
    private final String extension;
    private final Path path;
    private final long size;
    private final Instant created;
    private final Instant modified;
    private final Instant accessed;
    private final boolean directory;
    private final boolean hidden;
    private final boolean readable;
    private final boolean writable;
    private final boolean executable;

    public FileMetadata(
            String name,
            String type,
            String extension,
            Path path,
            long size,
            Instant created,
            Instant modified,
            Instant accessed,
            boolean directory,
            boolean hidden,
            boolean readable,
            boolean writable,
            boolean executable) {

        this.name = name;
        this.type = type;
        this.extension = extension;
        this.path = path;
        this.size = size;
        this.created = created;
        this.modified = modified;
        this.accessed = accessed;
        this.directory = directory;
        this.hidden = hidden;
        this.readable = readable;
        this.writable = writable;
        this.executable = executable;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getExtension() {
        return extension;
    }

    public Path getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getModified() {
        return modified;
    }

    public Instant getAccessed() {
        return accessed;
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public boolean isExecutable() {
        return executable;
    }
}
