package com.aegis.editor;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Terminal {

    private int rows;
    private int cols;
    private Process sttyProcess;
    private boolean rawMode = false;
    private PrintStream out;

    public Terminal() {
        this.out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    public void init() throws IOException {
        enableRawMode();
        detectSize();
    }

    private void enableRawMode() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            enableRawModeWindows();
        } else {
            enableRawModeUnix();
        }
        rawMode = true;
    }

    private void enableRawModeUnix() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("stty", "raw", "-echo");
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.start();
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}
    }

    private void enableRawModeWindows() throws IOException {
        // Windows Terminal supports VT processing natively; set minimal config
    }

    public void restore() {
        if (!rawMode) return;
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            try {
                ProcessBuilder pb = new ProcessBuilder("stty", "sane");
                pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
                pb.start().waitFor();
            } catch (Exception ignored) {}
        }
        rawMode = false;
        write(Renderer.ANSI_RESET);
        write(Renderer.ANSI_CLEAR);
        write(Renderer.cursorPos(1, 1));
        flush();
    }

    private void detectSize() throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            detectSizeWindows();
        } else {
            detectSizeUnix();
        }
        if (rows <= 0) rows = 24;
        if (cols <= 0) cols = 80;
    }

    private void detectSizeUnix() {
        try {
            ProcessBuilder pb = new ProcessBuilder("stty", "size");
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            String line = new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
            p.waitFor();
            if (line != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    rows = Integer.parseInt(parts[0]);
                    cols = Integer.parseInt(parts[1]);
                }
            }
        } catch (Exception ignored) {}

        if (rows <= 0 || cols <= 0) {
            write(Renderer.cursorPos(999, 999));
            write("\033[6n");
            flush();
            try {
                StringBuilder sb = new StringBuilder();
                InputStream in = System.in;
                int c;
                while ((c = in.read()) != 'R') {
                    if (c == 27 || c == '[') continue;
                    sb.append((char) c);
                }
                String[] parts = sb.toString().split(";");
                if (parts.length == 2) {
                    rows = Integer.parseInt(parts[0].trim());
                    cols = Integer.parseInt(parts[1].trim());
                }
            } catch (Exception ignored) {}
        }
    }

    private void detectSizeWindows() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "mode con"});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (line.startsWith("lines:")) {
                    rows = Integer.parseInt(line.replaceAll("[^0-9]", "").trim());
                } else if (line.startsWith("columns:")) {
                    cols = Integer.parseInt(line.replaceAll("[^0-9]", "").trim());
                }
            }
        } catch (Exception ignored) {}
    }

    public void write(String s) {
        out.print(s);
    }

    public void flush() {
        out.flush();
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public void refreshSize() {
        try {
            detectSize();
        } catch (IOException ignored) {}
    }
}
