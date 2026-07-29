# Aegis Editor V0.1

A full-screen terminal text editor written in pure Java 17, built for Linux and Windows Terminal. Inspired by the simplicity of GNU Nano while remaining a completely original implementation.

---

## Features

- Full-screen ANSI terminal rendering
- Black background with white inverse header and footer
- UTF-8 file support
- Efficient frame-buffer rendering
- Cursor movement with arrow keys, Home, End, Page Up/Down
- Character insertion, deletion, backspace, and Enter
- Open existing files or create new ones
- Save files (`CTRL+S`)
- Quit with unsaved-changes dialog (`CTRL+Q`)
- Search (`CTRL+ALT+F`) with inline highlighting
- Status bar showing filename, modified indicator, line/column, and status messages
- Linux and Windows Terminal support

---

## Requirements

- Java 17 or higher
- A terminal with ANSI escape code support (Linux, macOS, Windows Terminal)

---

## Build

### Linux / macOS

```bash
chmod +x build.sh
./build.sh
```

### Windows

```bat
build.bat
```

The build scripts compile all sources and produce `aegise.jar`.

---

## Usage

```bash
# Open an existing file
java -jar aegise.jar filename.txt

# Create a new file (opens blank buffer, prompts for name on save)
java -jar aegise.jar newfile.txt
```

---

## Key Bindings

| Key           | Action               |
|---------------|----------------------|
| Arrow keys    | Move cursor          |
| Home / End    | Line start / end     |
| Page Up/Down  | Scroll by page       |
| Enter         | Insert new line      |
| Backspace     | Delete previous char |
| Delete        | Delete current char  |
| CTRL+S        | Save file            |
| CTRL+Q        | Quit                 |
| CTRL+ALT+F    | Search               |
| ESC           | Cancel / clear search|

### Quit Dialog (when file is modified)

```
Save changes before quitting?
[Y] Yes
[N] No
[C] Cancel
```

---

## Project Structure

```
AegisEditor/
├── src/com/aegis/editor/
│   ├── Main.java         — Entry point
│   ├── Editor.java       — Core editor controller
│   ├── Terminal.java     — Terminal raw mode and size detection
│   ├── Renderer.java     — ANSI rendering abstraction
│   ├── Buffer.java       — Text buffer (line-based)
│   ├── FileManager.java  — File I/O abstraction
│   ├── Input.java        — Key input and escape sequence parsing
│   ├── Prompt.java       — Overlay prompt dialogs
│   └── Search.java       — Search abstraction with highlighting
├── README.md
├── LICENSE
├── build.sh
└── build.bat
```

---

## Architecture

Aegis Editor follows a clean object-oriented design with strict separation of concerns:

- **Terminal** — Manages raw mode, terminal size detection (Unix `stty` / Windows `mode con`), and low-level output.
- **Renderer** — All ANSI escape sequences flow through here. Frame buffering ensures flicker-free redraws.
- **Buffer** — A `List<StringBuilder>` line-array with O(1) single-character operations. Tracks the modified flag.
- **FileManager** — Reads and writes UTF-8 files with `java.nio`. Completely decoupled from the editor.
- **Input** — Reads raw bytes from stdin and maps escape sequences to named key constants.
- **Prompt** — Renders overlay dialogs (text prompt, quit dialog) using the Renderer.
- **Search** — Stateless search over the Buffer; highlights matches via Renderer.
- **Editor** — Orchestrates all components: processes keys, manages cursor/scroll state, delegates to subsystems.

---

## License

MIT — see `LICENSE`.

---

## Donate

If you find Aegis Editor useful, consider supporting development:

**Donate (BTC):**

```
bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh
```

Your support helps keep the Aluminum Project open-source and actively maintained. Thank you!

---

## Aluminum Project™

Aegis Editor is part of the **AsOS \ AegisOS** 
