package swp.com.workspace_fx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class FileFilterTest {

    @TempDir
    Path tempDir;

    @Test
    void testSimpleFilter() throws IOException {
        // Create test files
        Files.writeString(tempDir.resolve("doc1.txt"), "content");
        Files.writeString(tempDir.resolve("doc2.txt"), "content");
        Files.writeString(tempDir.resolve("image.jpg"), "content");
        Files.writeString(tempDir.resolve("data.csv"), "content");

        // Define filter for .txt files
        String filterPattern = ".+\\.txt";
        Predicate<Path> filter = path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.matches(filterPattern);
        };

        // Find matching files
        List<Path> matchingFiles = Files.list(tempDir)
                .filter(filter)
                .toList();

        assertEquals(2, matchingFiles.size());
        assertTrue(matchingFiles.stream().allMatch(p -> p.toString().endsWith(".txt")));
    }

    @Test
    void testContainsFilter() throws IOException {
        // Create test files
        Files.writeString(tempDir.resolve("document1.txt"), "content");
        Files.writeString(tempDir.resolve("mydocument.pdf"), "content");
        Files.writeString(tempDir.resolve("image.jpg"), "content");
        Files.writeString(tempDir.resolve("docdata.csv"), "content");

        // Define filter for files containing "doc"
        String filterPattern = "doc";
        Predicate<Path> filter = path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.contains(filterPattern.toLowerCase());
        };

        // Find matching files
        List<Path> matchingFiles = Files.list(tempDir)
                .filter(filter)
                .toList();

        assertEquals(3, matchingFiles.size());
        assertTrue(matchingFiles.stream().anyMatch(p -> p.getFileName().toString().equals("document1.txt")));
        assertTrue(matchingFiles.stream().anyMatch(p -> p.getFileName().toString().equals("mydocument.pdf")));
        assertTrue(matchingFiles.stream().anyMatch(p -> p.getFileName().toString().equals("docdata.csv")));
    }

    @Test
    void testEmptyFilter() throws IOException {
        // Create test files
        Files.writeString(tempDir.resolve("doc.txt"), "content");
        Files.writeString(tempDir.resolve("image.jpg"), "content");
        Files.writeString(tempDir.resolve("data.csv"), "content");

        // Define empty filter (should match all)
        String filterPattern = "";
        Predicate<Path> filter = path -> {
            if (filterPattern.isEmpty()) {
                return true;
            }
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.contains(filterPattern.toLowerCase());
        };

        // Find matching files
        List<Path> matchingFiles = Files.list(tempDir)
                .filter(filter)
                .toList();

        assertEquals(3, matchingFiles.size());
    }

    @Test
    void testWildcardFilter() throws IOException {
        // Create test files
        Files.writeString(tempDir.resolve("report_2023.xlsx"), "content");
        Files.writeString(tempDir.resolve("report_2024.xlsx"), "content");
        Files.writeString(tempDir.resolve("data_2023.csv"), "content");
        Files.writeString(tempDir.resolve("report_summary.pdf"), "content");

        // Define filter for report*.xlsx files
        String filterPattern = "report.*\\.xlsx";
        Predicate<Path> filter = path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.matches(filterPattern);
        };

        // Find matching files
        List<Path> matchingFiles = Files.list(tempDir)
                .filter(filter)
                .toList();

        assertEquals(2, matchingFiles.size());
        assertTrue(matchingFiles.stream().anyMatch(p -> p.getFileName().toString().equals("report_2023.xlsx")));
        assertTrue(matchingFiles.stream().anyMatch(p -> p.getFileName().toString().equals("report_2024.xlsx")));
    }

    @Test
    void testCaseInsensitiveFilter() throws IOException {
        // Create test files
        Files.writeString(tempDir.resolve("README.md"), "content");
        Files.writeString(tempDir.resolve("readme.txt"), "content");
        Files.writeString(tempDir.resolve("ReadMe.adoc"), "content");
        Files.writeString(tempDir.resolve("other.txt"), "content");

        // Define filter for readme files (case insensitive)
        String filterPattern = "readme";
        Predicate<Path> filter = path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.contains(filterPattern.toLowerCase());
        };

        // Find matching files
        List<Path> matchingFiles = Files.list(tempDir)
                .filter(filter)
                .toList();

        assertEquals(3, matchingFiles.size());
        assertTrue(matchingFiles.stream().anyMatch(p -> p.getFileName().toString().equals("README.md")));
        assertTrue(matchingFiles.stream().anyMatch(p -> p.getFileName().toString().equals("readme.txt")));
        assertTrue(matchingFiles.stream().anyMatch(p -> p.getFileName().toString().equals("ReadMe.adoc")));
    }

    @Test
    void testSpecialCharactersFilter() throws IOException {
        // Create test files
        Files.writeString(tempDir.resolve("file(1).txt"), "content");
        Files.writeString(tempDir.resolve("file[2].txt"), "content");
        Files.writeString(tempDir.resolve("file{3}.txt"), "content");
        Files.writeString(tempDir.resolve("normal.txt"), "content");

        // Define filter for files with special characters using escaping
        String filterPattern = "file\\(.+\\)\\.txt";
        Predicate<Path> filter = path -> {
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.matches(filterPattern);
        };

        // Find matching files
        List<Path> matchingFiles = Files.list(tempDir)
                .filter(filter)
                .toList();

        assertEquals(1, matchingFiles.size());
        assertTrue(matchingFiles.stream().anyMatch(p -> p.getFileName().toString().equals("file(1).txt")));
    }

    @Test
    void testNestedDirectoryFilter() throws IOException {
        // Create nested directory structure
        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Files.writeString(subDir.resolve("nested.txt"), "content");
        Files.writeString(tempDir.resolve("root.txt"), "content");

        // Count all text files recursively
        long textFileCount = Files.walk(tempDir)
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> p.toString().endsWith(".txt"))
                .count();

        assertEquals(2, textFileCount);
    }
}