package com.aegis.editor;

public class Main {

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java -jar aegise.jar [filename]");
            System.exit(1);
        }
        String filename = args.length == 1 ? args[0] : null;
        Terminal terminal = new Terminal();
        try {
            terminal.init();
            Editor editor = new Editor(terminal, filename);
            editor.run();
        } catch (Exception e) {
            terminal.restore();
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            terminal.restore();
        }
    }
}
