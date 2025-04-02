# Workspace File Copy Utility

This application allows users to easily copy files between directories with filtering capabilities and a modern UI.

## Features

- Simple and intuitive file copying between directories
- File filtering with pattern matching support
- Multi-file selection
- Detailed operation logging
- Modern, styled interface

## Usage Instructions

1. **Launch the application**: Run the program to open the "File Workspace" window
2. **Select source directory**: Click "Browse" to choose the folder containing files to copy
3. **Select destination directory**: Click "Browse" to select where files will be copied
4. **Filter files** (optional):
   - Enter patterns like `*.txt` or `data*` in the filter field
   - Click "Apply Filter" to show matching files
5. **Select files**: Click on files in the list (use Ctrl/Cmd or Shift for multiple selection)
6. **Copy files**: Click "Copy Selected" to perform the copy operation

## Logging

All operations are logged to a session-specific log file in the `logs` directory:
- `FileWorkspace_session_YYYYMMDD_HHMMSS.log`

## System Requirements

- Java 11 or higher
- At least 100MB of free disk space
- Any operating system supporting Java

## Development

This application is built with:
- JavaFX for the UI
- Standard Java file operations
- Custom logging system
