package swp.com.workspace_fx;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class FileOperationsTest {

    @TempDir
    Path sourceDir;

    @TempDir
    Path destDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create test files in source directory
        Files.writeString(sourceDir.resolve("test1.txt"), "test content 1");
        Files.writeString(sourceDir.resolve("test2.txt"), "test content 2");
        Files.writeString(sourceDir.resolve("data.csv"), "a,b,c");

        // Create subdirectory with files
        Path subDir = sourceDir.resolve("subdir");
        Files.createDirectory(subDir);
        Files.writeString(subDir.resolve("nested.txt"), "nested content");
        Files.writeString(subDir.resolve("nested.xml"), "<root>data</root>");
    }

    @Test
    void testCopyFiles() throws IOException {
        // Source files
        Path sourcePath1 = sourceDir.resolve("test1.txt");
        Path sourcePath2 = sourceDir.resolve("data.csv");

        // Destination paths
        Path destPath1 = destDir.resolve("test1.txt");
        Path destPath2 = destDir.resolve("data.csv");

        // Copy files
        Files.copy(sourcePath1, destPath1);
        Files.copy(sourcePath2, destPath2);

        // Verify files were copied correctly
        Assertions.assertTrue(Files.exists(destPath1));
        Assertions.assertTrue(Files.exists(destPath2));
        Assertions.assertEquals("test content 1", Files.readString(destPath1));
        Assertions.assertEquals("a,b,c", Files.readString(destPath2));
    }

    @Test
    void testFileFiltering() throws IOException {
        // Test text file filtering
        List<Path> textFiles = Files.walk(sourceDir)
                .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                .toList();

        Assertions.assertEquals(3, textFiles.size());

        // Test pattern matching
        List<Path> dataFiles = Files.walk(sourceDir)
                .filter(path -> path.getFileName().toString().startsWith("data"))
                .toList();

        Assertions.assertEquals(1, dataFiles.size());
        Assertions.assertTrue(dataFiles.get(0).getFileName().toString().equals("data.csv"));
    }

    @Test
    void testNestedDirectoryCopy() throws IOException {
        // Create nested structure in destination
        Files.createDirectories(destDir.resolve("subdir"));

        // Copy file from nested directory
        Path sourceNestedFile = sourceDir.resolve("subdir").resolve("nested.txt");
        Path destNestedFile = destDir.resolve("subdir").resolve("nested.txt");

        Files.copy(sourceNestedFile, destNestedFile);

        Assertions.assertTrue(Files.exists(destNestedFile));
        Assertions.assertEquals("nested content", Files.readString(destNestedFile));
    }
}