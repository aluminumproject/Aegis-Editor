package com.aegis.editor;

import java.io.IOException;
import java.util.List;

public class Editor {

    private final Terminal    terminal;
    private final Renderer    renderer;
    private final Input       input;
    private final FileManager fileManager;
    private final Prompt      prompt;
    private final Search      search;
    private       Buffer      buffer;

    private int cursorRow    = 0;
    private int cursorCol    = 0;
    private int viewRowOffset = 0;
    private int viewColOffset = 0;

    private boolean running  = true;
    private String  status   = "";

    private static final int HEADER_ROWS = 1;
    private static final int FOOTER_ROWS = 2;

    public Editor(Terminal terminal, String filename) throws IOException {
        this.terminal    = terminal;
        this.renderer    = new Renderer(terminal);
        this.input       = new Input();
        this.fileManager = new FileManager(filename);
        this.prompt      = new Prompt(terminal, renderer, input);
        this.search      = new Search();

        List<String> content = fileManager.load();
        this.buffer = new Buffer(content);
    }

    public void run() throws IOException {
        renderer.beginFrame();
        renderer.appendRaw(Renderer.ANSI_CLEAR);
        renderer.appendRaw(Renderer.ANSI_BG_BLACK);
        renderer.endFrame();

        while (running) {
            render();
            int key = input.readKey();
            processKey(key);
        }
    }

    private int textAreaRows() {
        return terminal.getRows() - HEADER_ROWS - FOOTER_ROWS;
    }

    private void render() {
        int rows = terminal.getRows();
        int cols = terminal.getCols();
        int textStart = HEADER_ROWS + 1;
        int textEnd   = rows - FOOTER_ROWS;

        renderer.beginFrame();
        renderer.appendRaw(Renderer.ANSI_BG_BLACK);
        renderer.renderHeader(cols);
        renderer.renderTextArea(buffer, textStart, textEnd, cols, viewRowOffset, viewColOffset, search);
        renderer.renderFooter(rows, cols, fileManager.getFilename(), buffer.isModified(),
                cursorRow + 1, cursorCol + 1, buffer.lineCount(), status);

        int screenRow = HEADER_ROWS + 1 + (cursorRow - viewRowOffset);
        int screenCol = 1 + (cursorCol - viewColOffset);
        screenRow = Math.max(HEADER_ROWS + 1, Math.min(rows - FOOTER_ROWS, screenRow));
        screenCol = Math.max(1, Math.min(cols, screenCol));
        renderer.placeCursor(screenRow, screenCol);
        renderer.endFrame();
        status = "";
    }

    private void processKey(int key) throws IOException {
        switch (key) {
            case Input.KEY_CTRL_Q:  handleQuit();        break;
            case Input.KEY_CTRL_S:  handleSave();        break;
            case Input.KEY_CTRL_ALT_F:
            case Input.KEY_CTRL_F:  handleSearch();      break;
            case Input.KEY_ARROW_UP:    moveCursorUp();    break;
            case Input.KEY_ARROW_DOWN:  moveCursorDown();  break;
            case Input.KEY_ARROW_LEFT:  moveCursorLeft();  break;
            case Input.KEY_ARROW_RIGHT: moveCursorRight(); break;
            case Input.KEY_HOME:    cursorToLineStart(); break;
            case Input.KEY_END:     cursorToLineEnd();   break;
            case Input.KEY_PAGE_UP:   pageUp();   break;
            case Input.KEY_PAGE_DOWN: pageDown(); break;
            case Input.KEY_ENTER:
                buffer.insertNewline(cursorRow, cursorCol);
                cursorRow++;
                cursorCol = 0;
                scrollIntoView();
                break;
            case Input.KEY_BACKSPACE:
            case 8:
                handleBackspace();
                break;
            case Input.KEY_DELETE:
                handleDelete();
                break;
            case Input.KEY_ESC:
                search.clear();
                break;
            default:
                if (key >= 32 || key == 9) {
                    char c = (char) key;
                    buffer.insertChar(cursorRow, cursorCol, c);
                    cursorCol++;
                    scrollIntoView();
                }
                break;
        }
    }

    private void handleQuit() throws IOException {
        if (!buffer.isModified()) {
            running = false;
            return;
        }
        char choice = prompt.askQuitDialog(renderer);
        if (choice == 'y') {
            try {
                if (fileManager.getFilename() == null) {
                    String name = prompt.ask("Save as: ");
                    if (name == null || name.trim().isEmpty()) {
                        status = "Save cancelled.";
                        return;
                    }
                    fileManager.setFilename(name.trim());
                }
                fileManager.save(buffer);
                running = false;
            } catch (IOException e) {
                status = "Error saving: " + e.getMessage();
            }
        } else if (choice == 'n') {
            running = false;
        }
    }

    private void handleSave() throws IOException {
        if (fileManager.getFilename() == null) {
            String name = prompt.ask("Save as: ");
            if (name == null || name.trim().isEmpty()) {
                status = "Save cancelled.";
                return;
            }
            fileManager.setFilename(name.trim());
        }
        try {
            fileManager.save(buffer);
            status = "Saved: " + fileManager.getFilename();
        } catch (IOException e) {
            status = "Error: " + e.getMessage();
        }
    }

    private void handleSearch() throws IOException {
        String term = prompt.ask("Search: ");
        if (term == null) {
            search.clear();
            status = "Search cancelled.";
            return;
        }
        if (term.isEmpty()) {
            search.clear();
            status = "Search cleared.";
            return;
        }
        search.setTerm(term);
        int[] found = search.findNext(buffer, cursorRow, cursorCol - 1);
        if (found != null) {
            cursorRow = found[0];
            cursorCol = found[1];
            scrollIntoView();
            status = "Found: " + term;
        } else {
            status = "Not found: " + term;
        }
    }

    private void handleBackspace() {
        if (cursorCol == 0 && cursorRow == 0) return;
        if (cursorCol == 0) {
            int prevLineLen = buffer.lineLength(cursorRow - 1);
            buffer.backspaceChar(cursorRow, 0);
            cursorRow--;
            cursorCol = prevLineLen;
        } else {
            buffer.backspaceChar(cursorRow, cursorCol);
            cursorCol--;
        }
        scrollIntoView();
    }

    private void handleDelete() {
        int lineLen = buffer.lineLength(cursorRow);
        if (cursorCol >= lineLen) {
            if (cursorRow < buffer.lineCount() - 1) {
                buffer.backspaceChar(cursorRow + 1, 0);
            }
        } else {
            buffer.deleteChar(cursorRow, cursorCol);
        }
        scrollIntoView();
    }

    private void moveCursorUp() {
        if (cursorRow > 0) {
            cursorRow--;
            clampCursorCol();
            scrollIntoView();
        }
    }

    private void moveCursorDown() {
        if (cursorRow < buffer.lineCount() - 1) {
            cursorRow++;
            clampCursorCol();
            scrollIntoView();
        }
    }

    private void moveCursorLeft() {
        if (cursorCol > 0) {
            cursorCol--;
        } else if (cursorRow > 0) {
            cursorRow--;
            cursorCol = buffer.lineLength(cursorRow);
        }
        scrollIntoView();
    }

    private void moveCursorRight() {
        int lineLen = buffer.lineLength(cursorRow);
        if (cursorCol < lineLen) {
            cursorCol++;
        } else if (cursorRow < buffer.lineCount() - 1) {
            cursorRow++;
            cursorCol = 0;
        }
        scrollIntoView();
    }

    private void cursorToLineStart() {
        cursorCol = 0;
        scrollIntoView();
    }

    private void cursorToLineEnd() {
        cursorCol = buffer.lineLength(cursorRow);
        scrollIntoView();
    }

    private void pageUp() {
        int jump = textAreaRows();
        cursorRow = Math.max(0, cursorRow - jump);
        clampCursorCol();
        scrollIntoView();
    }

    private void pageDown() {
        int jump = textAreaRows();
        cursorRow = Math.min(buffer.lineCount() - 1, cursorRow + jump);
        clampCursorCol();
        scrollIntoView();
    }

    private void clampCursorCol() {
        int lineLen = buffer.lineLength(cursorRow);
        if (cursorCol > lineLen) cursorCol = lineLen;
    }

    private void scrollIntoView() {
        int textRows = textAreaRows();
        int cols     = terminal.getCols();

        if (cursorRow < viewRowOffset) {
            viewRowOffset = cursorRow;
        }
        if (cursorRow >= viewRowOffset + textRows) {
            viewRowOffset = cursorRow - textRows + 1;
        }
        if (cursorCol < viewColOffset) {
            viewColOffset = cursorCol;
        }
        if (cursorCol >= viewColOffset + cols) {
            viewColOffset = cursorCol - cols + 1;
        }
        if (viewRowOffset < 0) viewRowOffset = 0;
        if (viewColOffset < 0) viewColOffset = 0;
    }
}
