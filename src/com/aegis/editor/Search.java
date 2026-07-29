package com.aegis.editor;

public class Search {

    private String term;
    private boolean active;
    private int lastFoundRow;
    private int lastFoundCol;

    public Search() {
        this.term         = null;
        this.active       = false;
        this.lastFoundRow = 0;
        this.lastFoundCol = 0;
    }

    public void setTerm(String term) {
        this.term   = term;
        this.active = term != null && !term.isEmpty();
        this.lastFoundRow = 0;
        this.lastFoundCol = 0;
    }

    public void clear() {
        this.term         = null;
        this.active       = false;
        this.lastFoundRow = 0;
        this.lastFoundCol = 0;
    }

    public boolean hasActiveSearch() {
        return active && term != null && !term.isEmpty();
    }

    public String getTerm() {
        return term;
    }

    public int[] findNext(Buffer buffer, int fromRow, int fromCol) {
        if (!hasActiveSearch()) return null;
        String lower = term.toLowerCase();
        int totalLines = buffer.lineCount();

        int startRow = fromRow;
        int startCol = fromCol + 1;

        for (int pass = 0; pass < 2; pass++) {
            int r = startRow;
            while (r < totalLines) {
                String line = buffer.getLine(r).toLowerCase();
                int col = (r == startRow && pass == 0) ? startCol : 0;
                int idx = line.indexOf(lower, col);
                if (idx >= 0) {
                    lastFoundRow = r;
                    lastFoundCol = idx;
                    return new int[]{r, idx};
                }
                r++;
            }
            startRow = 0;
            startCol = 0;
        }
        return null;
    }

    public int getLastFoundRow() { return lastFoundRow; }
    public int getLastFoundCol() { return lastFoundCol; }
}
