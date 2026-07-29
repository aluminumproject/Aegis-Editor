package com.aegis.editor;

public class Renderer {

    public static final String ANSI_RESET       = "\033[0m";
    public static final String ANSI_CLEAR       = "\033[2J";
    public static final String ANSI_ERASE_LINE  = "\033[2K";
    public static final String ANSI_CURSOR_HIDE = "\033[?25l";
    public static final String ANSI_CURSOR_SHOW = "\033[?25h";
    public static final String ANSI_FG_WHITE    = "\033[97m";
    public static final String ANSI_FG_BLACK    = "\033[30m";
    public static final String ANSI_BG_BLACK    = "\033[40m";
    public static final String ANSI_BG_WHITE    = "\033[107m";
    public static final String ANSI_REVERSE     = "\033[7m";
    public static final String ANSI_BOLD        = "\033[1m";
    public static final String ANSI_FG_YELLOW   = "\033[93m";
    public static final String ANSI_FG_CYAN     = "\033[96m";
    public static final String ANSI_FG_RED      = "\033[91m";
    public static final String ANSI_FG_GREEN    = "\033[92m";
    public static final String ANSI_FG_GRAY     = "\033[90m";
    public static final String ANSI_BG_BLUE     = "\033[44m";

    private final Terminal terminal;
    private final StringBuilder frameBuffer;

    public Renderer(Terminal terminal) {
        this.terminal = terminal;
        this.frameBuffer = new StringBuilder(8192);
    }

    public void beginFrame() {
        frameBuffer.setLength(0);
        frameBuffer.append(ANSI_CURSOR_HIDE);
        frameBuffer.append(ANSI_RESET);
    }

    public void endFrame() {
        frameBuffer.append(ANSI_CURSOR_SHOW);
        terminal.write(frameBuffer.toString());
        terminal.flush();
    }

    public void appendRaw(String s) {
        frameBuffer.append(s);
    }

    public void moveCursor(int row, int col) {
        frameBuffer.append(cursorPos(row, col));
    }

    public void clearLine() {
        frameBuffer.append(ANSI_ERASE_LINE);
    }

    public void renderHeader(int cols) {
        String left  = " Aegis Editor  V0.1";
        String right = "Aegis Project\u2122 ";
        int spaces   = cols - left.length() - right.length();
        if (spaces < 1) spaces = 1;
        String header = left + " ".repeat(spaces) + right;
        if (header.length() > cols) {
            header = header.substring(0, cols);
        }
        frameBuffer.append(cursorPos(1, 1));
        frameBuffer.append(ANSI_REVERSE);
        frameBuffer.append(ANSI_BOLD);
        frameBuffer.append(ANSI_FG_WHITE);
        frameBuffer.append(header);
        frameBuffer.append(ANSI_RESET);
    }

    public void renderFooter(int rows, int cols, String filename, boolean modified,
                             int cursorRow, int cursorCol, int totalLines, String status) {
        frameBuffer.append(cursorPos(rows - 1, 1));
        frameBuffer.append(ANSI_REVERSE);
        frameBuffer.append(ANSI_BOLD);
        frameBuffer.append(ANSI_FG_WHITE);

        String fileLabel = (filename == null ? "[New File]" : filename) + (modified ? " *" : "");
        String posLabel  = "Ln " + cursorRow + ", Col " + cursorCol + "  |  " + totalLines + " lines";
        String statusStr = status != null && !status.isEmpty() ? "  " + status : "";

        int available = cols - fileLabel.length() - posLabel.length() - statusStr.length();
        if (available < 1) available = 1;

        String line = fileLabel + statusStr + " ".repeat(available) + posLabel;
        if (line.length() > cols) {
            line = line.substring(0, cols);
        }
        frameBuffer.append(line);
        frameBuffer.append(ANSI_RESET);

        frameBuffer.append(cursorPos(rows, 1));
        frameBuffer.append(ANSI_REVERSE);
        frameBuffer.append(ANSI_FG_WHITE);

        String keys = " CTRL+S Save | CTRL+Q Quit | CTRL+ALT+F Search";
        if (keys.length() < cols) {
            keys = keys + " ".repeat(cols - keys.length());
        } else if (keys.length() > cols) {
            keys = keys.substring(0, cols);
        }
        frameBuffer.append(keys);
        frameBuffer.append(ANSI_RESET);
    }

    public void renderTextArea(Buffer buffer, int startRow, int endRow, int cols,
                               int viewRowOffset, int viewColOffset, Search search) {
        int textRows = endRow - startRow + 1;
        for (int i = 0; i < textRows; i++) {
            int bufLine = viewRowOffset + i;
            frameBuffer.append(cursorPos(startRow + i, 1));
            frameBuffer.append(ANSI_RESET);
            frameBuffer.append(ANSI_BG_BLACK);
            frameBuffer.append(ANSI_FG_WHITE);
            frameBuffer.append(ANSI_ERASE_LINE);

            if (bufLine < buffer.lineCount()) {
                String line = buffer.getLine(bufLine);
                String rendered = renderLine(line, viewColOffset, cols, search, bufLine);
                frameBuffer.append(rendered);
            } else {
                frameBuffer.append(ANSI_FG_GRAY);
                frameBuffer.append("~");
                frameBuffer.append(ANSI_RESET);
            }
            frameBuffer.append(ANSI_RESET);
        }
    }

    private String renderLine(String line, int colOffset, int cols, Search search, int lineIndex) {
        if (line.isEmpty()) return "";

        String visible;
        if (colOffset < line.length()) {
            visible = line.substring(colOffset);
        } else {
            return "";
        }
        if (visible.length() > cols) {
            visible = visible.substring(0, cols);
        }

        if (search == null || !search.hasActiveSearch()) {
            return escapeAnsiInContent(visible);
        }

        String term = search.getTerm();
        if (term == null || term.isEmpty()) {
            return escapeAnsiInContent(visible);
        }

        StringBuilder sb = new StringBuilder();
        String lowerVisible = visible.toLowerCase();
        String lowerTerm    = term.toLowerCase();
        int idx = 0;
        while (idx < visible.length()) {
            int found = lowerVisible.indexOf(lowerTerm, idx);
            if (found == -1) {
                sb.append(escapeAnsiInContent(visible.substring(idx)));
                break;
            }
            if (found > idx) {
                sb.append(escapeAnsiInContent(visible.substring(idx, found)));
            }
            sb.append(ANSI_BG_BLUE);
            sb.append(ANSI_FG_WHITE);
            sb.append(ANSI_BOLD);
            sb.append(escapeAnsiInContent(visible.substring(found, found + term.length())));
            sb.append(ANSI_RESET);
            sb.append(ANSI_BG_BLACK);
            sb.append(ANSI_FG_WHITE);
            idx = found + term.length();
        }
        return sb.toString();
    }

    private String escapeAnsiInContent(String s) {
        return s.replace("\033", "");
    }

    public void placeCursor(int screenRow, int screenCol) {
        frameBuffer.append(cursorPos(screenRow, screenCol));
        frameBuffer.append(ANSI_CURSOR_SHOW);
    }

    public static String cursorPos(int row, int col) {
        return "\033[" + row + ";" + col + "H";
    }

    public void renderPromptOverlay(int rows, int cols, String promptLine,
                                     String input, String error) {
        int overlayRow = rows / 2;
        renderBoxRow(overlayRow - 1, cols, true, "");
        renderBoxRow(overlayRow,     cols, false, "  " + promptLine + input);
        renderBoxRow(overlayRow + 1, cols, false,
                error != null ? "  " + ANSI_FG_RED + error + ANSI_RESET + ANSI_BG_BLACK : "");
        renderBoxRow(overlayRow + 2, cols, true, "");
        int cursorCol = 3 + promptLine.length() + input.length();
        if (cursorCol > cols) cursorCol = cols;
        frameBuffer.append(cursorPos(overlayRow, cursorCol));
    }

    private void renderBoxRow(int row, int cols, boolean blank, String content) {
        frameBuffer.append(cursorPos(row, 1));
        frameBuffer.append(ANSI_BG_BLACK);
        frameBuffer.append(ANSI_FG_YELLOW);
        if (blank) {
            frameBuffer.append("+");
            frameBuffer.append("-".repeat(cols - 2));
            frameBuffer.append("+");
        } else {
            frameBuffer.append("|");
            frameBuffer.append(ANSI_FG_WHITE);
            String padded = content;
            int inner = cols - 2;
            if (padded.length() < inner) {
                padded = padded + " ".repeat(inner - padded.length());
            } else if (padded.length() > inner) {
                padded = padded.substring(0, inner);
            }
            frameBuffer.append(padded);
            frameBuffer.append(ANSI_FG_YELLOW);
            frameBuffer.append("|");
        }
        frameBuffer.append(ANSI_RESET);
    }

    public void renderQuitDialog(int rows, int cols) {
        int overlayRow = rows / 2 - 2;
        String title   = "  Save changes before quitting?";
        String opt1    = "  [Y] Yes";
        String opt2    = "  [N] No";
        String opt3    = "  [C] Cancel";

        renderBoxRow(overlayRow,     cols, true,  "");
        renderBoxRow(overlayRow + 1, cols, false, title);
        renderBoxRow(overlayRow + 2, cols, false, "");
        renderBoxRow(overlayRow + 3, cols, false, opt1);
        renderBoxRow(overlayRow + 4, cols, false, opt2);
        renderBoxRow(overlayRow + 5, cols, false, opt3);
        renderBoxRow(overlayRow + 6, cols, true,  "");
    }
}
