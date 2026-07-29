package com.aegis.editor;

import java.util.ArrayList;
import java.util.List;

public class Buffer {

    private final List<StringBuilder> lines;
    private boolean modified;

    public Buffer() {
        this.lines    = new ArrayList<>();
        this.modified = false;
        lines.add(new StringBuilder());
    }

    public Buffer(List<String> content) {
        this.lines    = new ArrayList<>();
        this.modified = false;
        if (content == null || content.isEmpty()) {
            lines.add(new StringBuilder());
        } else {
            for (String s : content) {
                lines.add(new StringBuilder(s));
            }
        }
    }

    public int lineCount() {
        return lines.size();
    }

    public String getLine(int index) {
        if (index < 0 || index >= lines.size()) return "";
        return lines.get(index).toString();
    }

    public int lineLength(int index) {
        if (index < 0 || index >= lines.size()) return 0;
        return lines.get(index).length();
    }

    public void insertChar(int row, int col, char c) {
        if (row < 0 || row >= lines.size()) return;
        StringBuilder sb = lines.get(row);
        int safeCol = Math.min(col, sb.length());
        sb.insert(safeCol, c);
        modified = true;
    }

    public void insertString(int row, int col, String s) {
        if (row < 0 || row >= lines.size()) return;
        StringBuilder sb = lines.get(row);
        int safeCol = Math.min(col, sb.length());
        sb.insert(safeCol, s);
        modified = true;
    }

    public void deleteChar(int row, int col) {
        if (row < 0 || row >= lines.size()) return;
        StringBuilder sb = lines.get(row);
        if (col >= 0 && col < sb.length()) {
            sb.deleteCharAt(col);
            modified = true;
        }
    }

    public void backspaceChar(int row, int col) {
        if (row < 0 || row >= lines.size()) return;
        if (col <= 0) {
            if (row == 0) return;
            StringBuilder prev = lines.get(row - 1);
            StringBuilder curr = lines.get(row);
            prev.append(curr);
            lines.remove(row);
            modified = true;
            return;
        }
        StringBuilder sb = lines.get(row);
        if (col - 1 < sb.length()) {
            sb.deleteCharAt(col - 1);
            modified = true;
        }
    }

    public void insertNewline(int row, int col) {
        if (row < 0 || row >= lines.size()) return;
        StringBuilder sb = lines.get(row);
        int safeCol = Math.min(col, sb.length());
        String rest = sb.substring(safeCol);
        sb.delete(safeCol, sb.length());
        lines.add(row + 1, new StringBuilder(rest));
        modified = true;
    }

    public List<String> getAllLines() {
        List<String> result = new ArrayList<>(lines.size());
        for (StringBuilder sb : lines) {
            result.add(sb.toString());
        }
        return result;
    }

    public boolean isModified() {
        return modified;
    }

    public void clearModified() {
        modified = false;
    }
}
