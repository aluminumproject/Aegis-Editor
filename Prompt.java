package com.aegis.editor;

import java.io.IOException;

public class Prompt {

    private final Terminal terminal;
    private final Renderer renderer;
    private final Input    input;

    public Prompt(Terminal terminal, Renderer renderer, Input input) {
        this.terminal = terminal;
        this.renderer = renderer;
        this.input    = input;
    }

    public String ask(String promptText) throws IOException {
        return input.readLine(promptText, terminal, renderer,
                terminal.getRows(), terminal.getCols());
    }

    public char askQuitDialog(Renderer renderer) throws IOException {
        int rows = terminal.getRows();
        int cols = terminal.getCols();
        renderer.beginFrame();
        renderer.renderQuitDialog(rows, cols);
        renderer.endFrame();
        while (true) {
            int key = input.readKey();
            if (key == 'y' || key == 'Y') return 'y';
            if (key == 'n' || key == 'N') return 'n';
            if (key == 'c' || key == 'C') return 'c';
            if (key == Input.KEY_ESC)     return 'c';
        }
    }
}
