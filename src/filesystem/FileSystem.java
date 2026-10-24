package filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;


import models.*;

public class FileSystem implements FileManagerService{
    private enum ClipboardMode {COPY, CUT};
    private FolderItem workspace;
    private final ArrayList<FileSystemItem> clipboard = new ArrayList<>();
    private ClipboardMode clipboardMode;

    @Override
    public FolderItem openWorkspace(Path rootFolder) throws IOException{
        if(!Files.isDirectory(rootFolder)){
            throw new IOException("Not a valid Folder: " + rootFolder);
        } 

        workspace = new FolderItem(rootFolder);
        return workspace;
    }

    @Override 
    public ArrayList<FileSystemItem> listContents(Path folder) throws IOException{
        return new FolderItem(folder).getChildren();
    }

    @Override 
    public FileItem createFile(Path folder, String name) throws IOException{
        return new FolderItem(folder).createFile(name);
    }

    @Override 
    public FolderItem createFolder(Path folder, String name) throws IOException{
        return new FolderItem(folder).createFolder(name);
    }

    @Override 
    public void rename(FileSystemItem item, String newName) throws IOException{
        item.renameFile(newName);
    }

    @Override 
    public void move(FileSystemItem item, Path targetFolder) throws IOException{
        item.moveFile(targetFolder);
    }

    @Override
    public void delete(FileSystemItem item) throws IOException{
        item.delete();
    }

    @Override 
    public void open(FileItem item) throws IOException{
        item.open();
    }

    @Override
    public BasicFileAttributes getMetadata(FileSystemItem item) throws IOException{
        return item.getMetadata();
    }

    @Override 
    public void copy(ArrayList<FileSystemItem> items){
        clipboard.clear();
        clipboard.addAll(items);
        clipboardMode = ClipboardMode.COPY;
    }

    @Override 
    public void cut(ArrayList<FileSystemItem> items){
        clipboard.clear();
        clipboard.addAll(items);
        clipboardMode = ClipboardMode.CUT;
    }


    @Override 
    public boolean canPaste(){
        return !clipboard.isEmpty();
    }

    @Override 
    public ArrayList<FileSystemItem> paste(Path targetFolder) throws IOException{
        if(!canPaste()) throw new IllegalStateException("Clipboard is Empty!");

        ArrayList<FileSystemItem> results = new ArrayList<>();
        for(FileSystemItem item : clipboard){
            if(clipboardMode == ClipboardMode.COPY){
                results.add(item.copyTo(targetFolder));
            } else{
                if(!item.getPath().getParent().equals(targetFolder.toAbsolutePath())){
                    item.moveFile(targetFolder);
                }
                results.add(item);
            }
        }
        if(clipboardMode == ClipboardMode.CUT){
            clipboard.clear();
        }
        return results;
    }

    @Override
    public FileSystemItem duplicate(FileSystemItem item) throws IOException{
        Path parent= item.getPath().getParent();
        if (parent==null){
            throw new IOException("Cannot duplicate an item without a parent folder.");
        }
        return item.copyTo(parent);
    }


    @Override
    public ArrayList<FileSystemItem> search(String query) throws IOException{
        if (workspace==null){
            throw new IllegalStateException("Workspace is not opened. Please open a workspace first.");
        }

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be empty.");
        }

        ArrayList<FileSystemItem> results = new ArrayList<>();
        String searchQuery = query.toLowerCase(Locale.ROOT);

        Path workspacePath = workspace.getPath();

        try (Stream<Path> pathStream =Files.walk(workspacePath)) {

            Iterator<Path> iterator = pathStream.iterator();

            while (iterator.hasNext()){
                Path currentPath=iterator.next();

                Path fileNamePath=currentPath.getFileName();

                String nameText=fileNamePath.toString();

                String nameTextLower=nameText.toLowerCase(Locale.ROOT);

                if (nameTextLower.contains(searchQuery)) {
                    if (Files.isDirectory(currentPath, LinkOption.NOFOLLOW_LINKS)) {
                        FolderItem folderResult=new FolderItem(currentPath);

                        results.add(folderResult);
                    } else {
                        FileItem fileResult=new FileItem(currentPath);

                        results.add(fileResult);
                    }
                }
            }
        }

        return results;

    }
}
