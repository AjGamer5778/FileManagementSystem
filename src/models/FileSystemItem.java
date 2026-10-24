package models;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;


public abstract class FileSystemItem{
    protected Path path;

    protected FileSystemItem(Path path){
        this.path = path;
    }

    public String getName(){
        return (path.getFileName().toString());
    }

    public Path getPath(){
        return path;
    }

    public void renameFile(String newName) throws IOException{
        Path newPath = path.resolveSibling(newName);
        Files.move(path, newPath);
        path = newPath;
    }

    public void moveFile(Path targetFolder) throws IOException{
        targetFolder = targetFolder.toAbsolutePath();
        if(targetFolder.startsWith(path)){
            throw new IOException("Cannot Move a folder into Itself");
        }
        
        
        Path newPath = targetFolder.resolve(path.getFileName());
        Files.move(path, newPath);
        path = newPath;
    }

    public BasicFileAttributes getMetadata() throws IOException{
        return Files.readAttributes(path, BasicFileAttributes.class);
    }

    protected Path resolveUniqueTarget(Path targetFolder){
        Path target = targetFolder.resolve(path.getFileName());
        if(!Files.exists(target)) return target;

        String name = getName();
        int dot = name.lastIndexOf(".");
        boolean hasExt = dot > 0 && !Files.isDirectory(path);
        String base = hasExt ? name.substring(0, dot) : name;
        String ext = hasExt ? name.substring(dot) : "";

        int i = 1;
        do {
            String suffix = (i == 1) ? " - Copy" : " - Copy (" + i + ")";
            target = targetFolder.resolve(base + suffix + ext);
            i++;
        } while(Files.exists(target));

        return target;
    }




    public abstract FileSystemItem copyTo(Path targetFolder) throws IOException;
    public abstract void delete() throws IOException;
}