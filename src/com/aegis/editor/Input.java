package com.aegis.editor;

import java.io.*;

public class Input {

    public static final int KEY_ARROW_UP    = 1000;
    public static final int KEY_ARROW_DOWN  = 1001;
    public static final int KEY_ARROW_LEFT  = 1002;
    public static final int KEY_ARROW_RIGHT = 1003;
    public static final int KEY_HOME        = 1004;
    public static final int KEY_END         = 1005;
    public static final int KEY_PAGE_UP     = 1006;
    public static final int KEY_PAGE_DOWN   = 1007;
    public static final int KEY_DELETE      = 1008;
    public static final int KEY_CTRL_S      = 19;
    public static final int KEY_CTRL_Q      = 17;
    public static final int KEY_CTRL_F      = 6;
    public static final int KEY_BACKSPACE   = 127;
    public static final int KEY_ENTER       = 13;
    public static final int KEY_ESC         = 27;
    public static final int KEY_CTRL_ALT_F  = 9000;
    public static final int KEY_RESIZE      = 9001;

    private final InputStream in;

    public Input() {
        this.in = System.in;
    }

    public int readKey() throws IOException {
        int c = in.read();
        if (c == -1) return KEY_ESC;

        if (c == KEY_ESC) {
            int avail = in.available();
            if (avail == 0) return KEY_ESC;

            int c1 = in.read();
            if (c1 == -1) return KEY_ESC;

            if (c1 == '[') {
                int c2 = in.read();
                if (c2 == -1) return KEY_ESC;

                if (c2 >= '0' && c2 <= '9') {
                    int c3 = in.read();
                    if (c3 == -1) return KEY_ESC;

                    if (c3 == '~') {
                        switch (c2) {
                            case '1': return KEY_HOME;
                            case '3': return KEY_DELETE;
                            case '4': return KEY_END;
                            case '5': return KEY_PAGE_UP;
                            case '6': return KEY_PAGE_DOWN;
                            case '7': return KEY_HOME;
                            case '8': return KEY_END;
                            default:  return KEY_ESC;
                        }
                    } else if (c3 == ';') {
                        int mod = in.read();
                        int arrow = in.read();
                        if (mod == '3' && arrow == 'f') return KEY_CTRL_ALT_F;
                        if (mod == '3' && arrow == 'F') return KEY_CTRL_ALT_F;
                        return KEY_ESC;
                    }
                    return KEY_ESC;
                }

                switch (c2) {
                    case 'A': return KEY_ARROW_UP;
                    case 'B': return KEY_ARROW_DOWN;
                    case 'C': return KEY_ARROW_RIGHT;
                    case 'D': return KEY_ARROW_LEFT;
                    case 'H': return KEY_HOME;
                    case 'F': return KEY_END;
                    default:  return KEY_ESC;
                }
            } else if (c1 == 'O') {
                int c2 = in.read();
                if (c2 == -1) return KEY_ESC;
                switch (c2) {
                    case 'H': return KEY_HOME;
                    case 'F': return KEY_END;
                    case 'P': return KEY_ESC;
                    case 'Q': return KEY_ESC;
                    case 'R': return KEY_ESC;
                    case 'S': return KEY_ESC;
                    default:  return KEY_ESC;
                }
            } else if (c1 == 'f' || c1 == 'F') {
                return KEY_CTRL_ALT_F;
            }
            return KEY_ESC;
        }
        return c;
    }

    public String readLine(String prompt, Terminal terminal, Renderer renderer, int rows, int cols) throws IOException {
        StringBuilder input = new StringBuilder();
        String error = null;
        while (true) {
            renderer.beginFrame();
            renderer.renderPromptOverlay(rows, cols, prompt, input.toString(), error);
            renderer.endFrame();
            error = null;
            int key = readKey();
            if (key == KEY_ENTER) {
                return input.toString();
            } else if (key == KEY_ESC) {
                return null;
            } else if (key == KEY_BACKSPACE || key == 8) {
                if (input.length() > 0) {
                    input.deleteCharAt(input.length() - 1);
                }
            } else if (key >= 32 && key < 127) {
                input.append((char) key);
            } else if (key > 127) {
                input.append((char) key);
            }
        }
    }
}
