package com.aegis.editor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private String filename;

    public FileManager(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean fileExists() {
        if (filename == null) return false;
        return Files.exists(Paths.get(filename));
    }

    public List<String> load() throws IOException {
        if (filename == null) {
            return new ArrayList<>();
        }
        Path path = Paths.get(filename);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public void save(Buffer buffer) throws IOException {
        if (filename == null) throw new IOException("No filename specified.");
        Path path = Paths.get(filename);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        List<String> lines = buffer.getAllLines();
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                if (i < lines.size() - 1) {
                    writer.newLine();
                }
            }
        }
        buffer.clearModified();
    }
}
