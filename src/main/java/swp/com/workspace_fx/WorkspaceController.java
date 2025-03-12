package swp.com.workspace_fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import swp.com.workspace_fx.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.Predicate;

public class WorkspaceController {
    @FXML
    private TextField sourceDirectoryField;

    @FXML
    private TextField destinationDirectoryField;

    @FXML
    private TextField filterPatternField;

    @FXML
    private ListView<String> fileListView;

    @FXML
    private Label statusLabel;

    private File sourceDirectory;
    private File destinationDirectory;
    private ObservableList<String> fileList = FXCollections.observableArrayList();
    private final Logger logger = Logger.getInstance();

    @FXML
    public void initialize() {
        fileListView.setItems(fileList);
        fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logger.info("Application initialized");
    }

    @FXML
    protected void onSelectSourceDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Source Directory");
        sourceDirectory = directoryChooser.showDialog(null);

        if (sourceDirectory != null) {
            sourceDirectoryField.setText(sourceDirectory.getAbsolutePath());
            logger.info("Source directory selected: " + sourceDirectory.getAbsolutePath());
            updateFileList();
        }
    }

    @FXML
    protected void onSelectDestinationDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Destination Directory");
        destinationDirectory = directoryChooser.showDialog(null);

        if (destinationDirectory != null) {
            destinationDirectoryField.setText(destinationDirectory.getAbsolutePath());
            logger.info("Destination directory selected: " + destinationDirectory.getAbsolutePath());
        }
    }

    @FXML
    protected void onApplyFilter() {
        String filter = filterPatternField.getText();
        logger.info("Applying filter: " + (filter.isEmpty() ? "<empty>" : filter));
        updateFileList();
    }

    @FXML
    protected void onCopyFiles() {
        if (sourceDirectory == null || destinationDirectory == null) {
            String message = "Please select source and destination directories";
            statusLabel.setText(message);
            logger.error(message);
            return;
        }

        List<String> selectedFiles = fileListView.getSelectionModel().getSelectedItems();
        if (selectedFiles.isEmpty()) {
            String message = "No files selected for copying";
            statusLabel.setText(message);
            logger.error(message);
            return;
        }

        logger.info("Starting copy operation for " + selectedFiles.size() + " files");
        int copiedFiles = 0;
        for (String fileName : selectedFiles) {
            Path sourcePath = Paths.get(sourceDirectory.getPath(), fileName);
            Path destPath = Paths.get(destinationDirectory.getPath(), fileName);

            try {
                // Create parent directories if they don't exist
                Path parent = destPath.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }

                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                copiedFiles++;
                logger.info("Copied file: " + fileName);
            } catch (IOException e) {
                String message = "Error copying file: " + fileName;
                statusLabel.setText(message + " - " + e.getMessage());
                logger.error(message, e);
                return;
            }
        }

        String message = "Successfully copied " + copiedFiles + " files";
        statusLabel.setText(message);
        logger.info(message);
    }

    private void updateFileList() {
        fileList.clear();

        if (sourceDirectory == null) {
            return;
        }

        try {
            String filterPattern = filterPatternField.getText().trim();
            Predicate<Path> filter = path -> {
                if (filterPattern.isEmpty()) {
                    return true;
                }
                String fileName = path.getFileName().toString().toLowerCase();
                return fileName.contains(filterPattern.toLowerCase()) ||
                        fileName.matches(filterPattern);
            };

            Files.walkFileTree(sourceDirectory.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    Path relativePath = sourceDirectory.toPath().relativize(file);
                    if (filter.test(relativePath)) {
                        fileList.add(relativePath.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            if (fileList.isEmpty()) {
                String message = "No files match the filter criteria";
                statusLabel.setText(message);
                logger.info(message);
            } else {
                String message = fileList.size() + " files found";
                statusLabel.setText(message);
                logger.info(message);
            }
        } catch (IOException e) {
            String message = "Error reading directory";
            statusLabel.setText(message + ": " + e.getMessage());
            logger.error(message, e);
        }
    }
}