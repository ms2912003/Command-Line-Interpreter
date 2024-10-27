package org.os;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;


 class DirectoryListerTest {

    private Path tempDir;
    private Path hiddenFile;
    private Path visibleFile1;
    private Path visibleFile2;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary directory for testing
        tempDir = Files.createTempDirectory("testDir");

        // Create files in the temporary directory
        hiddenFile = Files.createFile(tempDir.resolve(".hiddenFile"));
        visibleFile1 = Files.createFile(tempDir.resolve("file1.txt"));
        visibleFile2 = Files.createFile(tempDir.resolve("file2.txt"));

        // Redirect output to capture it for testing
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up the temporary directory and files
        Files.deleteIfExists(hiddenFile);
        Files.deleteIfExists(visibleFile1);
        Files.deleteIfExists(visibleFile2);
        Files.deleteIfExists(tempDir);

        // Reset the output stream
        System.setOut(originalOut);
    }

    @Test
    public void testListDirectoryWithoutOptions() {
        String[] parts = {tempDir.toString()};
        CLI.listDirectory(parts); // Replace with the actual class name
        String output = outputStream.toString();

        // Verify the output contains only visible files
        assertTrue(output.contains("file1.txt"));
        assertTrue(output.contains("file2.txt"));
        assertFalse(output.contains(".hiddenFile"));
    }

    @Test
    public void testListDirectoryWithShowAll() {
        String[] parts = {tempDir.toString(), "-a"};
        CLI.listDirectory(parts); // Replace with the actual class name
        String output = outputStream.toString();

        // Verify the output contains hidden and visible files
        assertTrue(output.contains(".hiddenFile"));
        assertTrue(output.contains("file1.txt"));
        assertTrue(output.contains("file2.txt"));
    }

    @Test
    public void testListDirectoryWithReverseOrder() {
        String[] parts = {tempDir.toString(), "-r"};
        CLI.listDirectory(parts); // Replace with the actual class name
        String output = outputStream.toString();

        // Split the output by lines and check the order
        String[] lines = output.split(System.lineSeparator());
        assertEquals("file2.txt", lines[1]); // Assuming output is sorted and reversed
        assertEquals("file1.txt", lines[2]);
    }

    @Test
    public void testListNonExistentDirectory() {
        String[] parts = {"non_existent_directory"};
        CLI.listDirectory(parts); // Replace with the actual class name
        String output = outputStream.toString();

        // Verify the output indicates that the directory cannot be read
        assertTrue(output.contains("No files found or directory cannot be read."));
    }

    @Test
    public void testListEmptyDirectory() throws IOException {
        Path emptyDir = Files.createTempDirectory("emptyDir");
        String[] parts = {emptyDir.toString()};
        CLI.listDirectory(parts); // Replace with the actual class name
        String output = outputStream.toString();

        // Verify the output indicates that the directory is empty
        assertTrue(output.contains("Directory is empty or cannot be read."));

        // Clean up
        Files.delete(emptyDir);
    }
}

class CLIRedirectCommandTest {
    @Test
    void testRedirectCommand() {
        String[] createParts = {"touch", "testFile.txt"};
        CLI.touchFile(createParts);

        String command = "cat testFile.txt > output.txt";
        CLI.redirectCommand(command);

        File outputFile = new File("output.txt");
        assertTrue(outputFile.exists(), "Output file should exist.");

        // Cleanup after test
        new File("testFile.txt").delete();
        outputFile.delete();
    }
}
class CIRemoveFileTest {
    @Test
    void testRemoveFile() {
        String[] createParts = {"touch", "testFile.txt"};
        CLI.touchFile(createParts);

        String[] removeParts = {"rm", "testFile.txt"};
        CLI.removeFile(removeParts);

        File file = new File("testFile.txt");
        assertFalse(file.exists(), "File should be removed.");
    }
}

class CLIDisplayFileTest {
    @Test
    void testDisplayFile() {
        String[] createParts = {"touch", "testFile.txt"};
        CLI.touchFile(createParts);

        try (FileWriter writer = new FileWriter("testFile.txt")) {
            writer.write("Hello World");
        } catch (Exception e) {
            fail("Failed to write to file.");
        }

        String[] displayParts = {"cat", "testFile.txt"};
        CLI.displayFile(displayParts);

        // Validate output (capture System.out if necessary)

        // Cleanup after test
        new File("testFile.txt").delete();
    }
}

class CLIFileUtilsTest {
    private File sourceFile;
    private File destinationFile;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary source file with some content
        sourceFile = File.createTempFile("source", ".txt");
        Files.write(sourceFile.toPath(), "Hello, World!".getBytes());

        // Create a temporary destination file
        destinationFile = File.createTempFile("destination", ".txt");
    }

    @AfterEach
    void tearDown() {
        // Delete the temporary files after each test
        if (sourceFile.exists()) {
            sourceFile.delete();
        }
        if (destinationFile.exists()) {
            destinationFile.delete();
        }
    }

    @Test
    void testCopyFile() throws IOException {
        // Call the copyFile method
        CLI.copyFile(sourceFile, destinationFile);

        // Assert that the destination file exists
        assertTrue(destinationFile.exists(), "Destination file should exist after copying.");

        // Assert that the content of the destination file is correct
        String copiedContent = new String(Files.readAllBytes(destinationFile.toPath()));
        assertEquals("Hello, World!", copiedContent, "Content of the copied file should match the source.");
    }
}

class CLIMoveFileTest {
    @Test
    void testMoveFile() {
        String[] createParts = {"touch", "testFile.txt"};
        CLI.touchFile(createParts);

        String[] moveParts = {"mv", "testFile.txt", "newTestFile.txt"};
        CLI.moveFile(moveParts);

        File oldFile = new File("testFile.txt");
        File newFile = new File("newTestFile.txt");
        assertFalse(oldFile.exists(), "Old file should not exist.");
        assertTrue(newFile.exists(), "New file should be created.");

        // Cleanup after test
        newFile.delete();
    }
}

class CLIPrintWorkingDirectory {

    @Test
    public void testPrintWorkingDirectory() {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            // Act
            CLI.executeCommand("pwd");

            // Assert
            String expectedOutput = System.getProperty("user.dir") + System.lineSeparator();
            assertEquals(expectedOutput, outputStream.toString());
        } finally {
            // Reset the output stream to its original state
            System.setOut(originalOut);
        }
    }
}
class CLIMakeDirectory {
    @Test
    public void testCreateDirectory() {
        String testDirName = "testDir";

        // Act
        CLI.executeCommand("mkdir " + testDirName);

        // Assert
        File dir = new File(testDirName);
        assertTrue(dir.exists(), "Directory should have been created");

        // Cleanup
        dir.delete();
    }
 }

