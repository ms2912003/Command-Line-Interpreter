package org.os;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

public class CommandLIneInterpreterTest {
    //Redirect Command Test
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

    // Remove File Test
    @Test
    void testRemoveFile() {
        String[] createParts = {"touch", "testFile.txt"};
        CLI.touchFile(createParts);

        String[] removeParts = {"rm", "testFile.txt"};
        CLI.removeFile(removeParts);

        File file = new File("testFile.txt");
        assertFalse(file.exists(), "File should be removed.");
    }

    // Display File Test
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

    // Move File Test
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

    // Print Working Directory Test
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

    // Make Directory Test
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

    // Remove Directory Test
    private Path tempDir;
    private Path nonEmptyDir;
    private Path fileInNonEmptyDir;

    @BeforeEach
    public void setUp() throws IOException {
        // Set up an empty directory
        tempDir = Files.createTempDirectory("testDir");

        // Set up a non-empty directory
        nonEmptyDir = Files.createTempDirectory("nonEmptyDir");
        fileInNonEmptyDir = Files.createFile(nonEmptyDir.resolve("file.txt"));
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up the directories and files created
        Files.deleteIfExists(fileInNonEmptyDir);
        Files.deleteIfExists(nonEmptyDir);
        Files.deleteIfExists(tempDir);
    }

    @Test
    public void testRemoveEmptyDirectory() {
        String[] parts = {"rmdir", tempDir.toString()};
        CLI.removeDirectory(parts); // Replace with the actual class method if needed

        // Assert the directory no longer exists
        assertFalse(Files.exists(tempDir), "Empty directory should be removed successfully.");
    }

    @Test
    public void testRemoveNonEmptyDirectory() {
        String[] parts = {"rmdir", nonEmptyDir.toString()};
        CLI.removeDirectory(parts); // Replace with the actual class method if needed

        // Assert the directory still exists since it was not empty
        assertTrue(Files.exists(nonEmptyDir), "Non-empty directory should not be removed.");
    }

    // Touch Test
    private final String testFileName = "testTouchFile.txt";

    @AfterEach
    public void tearDown2() {
        // Clean up the created file after each test
        File file = new File(testFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testCreateFile() {
        String[] parts = {"touch", testFileName};
        CLI.touchFile(parts); // Replace with the actual class method if needed

        // Assert that the file has been created
        File file = new File(testFileName);
        assertTrue(file.exists(), "File should be created by the touch command.");
    }

    @Test
    public void testFileAlreadyExists() {
        // Create the file before testing
        File file = new File(testFileName);
        file.delete();
        assertFalse(file.exists(), "Ensure test starts with no existing file.");

        String[] parts = {"touch", testFileName};
        CLI.touchFile(parts); // First call to create the file
        CLI.touchFile(parts); // Second call to ensure no error with existing file

        // Assert that the file still exists and is unaffected by a second touch call
        assertTrue(file.exists(), "File should remain after calling touch on an existing file.");
    }

    // Create Directory Test
    private Path originalDir;
    private Path tempDir2;

    @BeforeEach
    public void setUp2() throws Exception {
        // Save the original directory
        originalDir = Paths.get(System.getProperty("user.dir"));

        // Create a temporary directory for testing
        tempDir = Files.createTempDirectory("testCDDir");
    }

    @AfterEach
    public void tearDown3() {
        // Reset to the original directory
        System.setProperty("user.dir", originalDir.toString());

        // Clean up the temporary directory
        tempDir.toFile().delete();
    }

    @Test
    public void testChangeToValidDirectory() {
        String[] parts = {"cd", tempDir.toString()};
        CLI.changeDirectory(parts);

        // Verify the current directory has been changed
        assertEquals(tempDir.toString(), System.getProperty("user.dir"));
    }

    @Test
    public void testChangeToInvalidDirectory() {
        String[] parts = {"cd", "invalid_directory"};
        CLI.changeDirectory(parts);

        // Verify the current directory has not been changed
        assertEquals(originalDir.toString(), System.getProperty("user.dir"));
    }

    @Test
    public void testChangeToParentDirectory() {
        String[] parts = {"cd", ".."};
        CLI.changeDirectory(parts);

        // Verify the current directory is now the parent directory of the original
        assertEquals(originalDir.getParent().toString(), System.getProperty("user.dir"));
    }

    // Help Test
    @Test
    public void testHelpCommand() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        CLI.executeCommand("help");

        // Verify output contains information about commands
        String output = outputStream.toString();
        assertTrue(output.contains("cd"));
        assertTrue(output.contains("exit"));
        assertTrue(output.contains("help"));
        assertTrue(output.contains(">"));
        assertTrue(output.contains("|"));
        assertTrue(output.contains("touch"));
        assertTrue(output.contains("rmdir"));
    }

    // List Directrory Test
        private Path tempDir1;
        private Path hiddenFile;
        private Path visibleFile1;
        private Path visibleFile2;

        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private final PrintStream originalOut = System.out;

        @BeforeEach
        public void setUp3() throws IOException {
            // Create a temporary directory for testing
            tempDir1 = Files.createTempDirectory("testDir");

            // Create files in the temporary directory
            hiddenFile = Files.createFile(tempDir1.resolve(".hiddenFile"));
            visibleFile1 = Files.createFile(tempDir1.resolve("file1.txt"));
            visibleFile2 = Files.createFile(tempDir1.resolve("file2.txt"));

            // Redirect output to capture it for testing
            System.setOut(new PrintStream(outputStream));
        }

        @AfterEach
        public void tearDown4() throws IOException {
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
            String[] parts = {tempDir1.toString()};
            CLI.executeCommand("ls" + " " + parts[0]);
            String output = outputStream.toString();

            // Verify the output contains only visible files
            assertTrue(output.contains("file1.txt"));
            assertTrue(output.contains("file2.txt"));
            assertFalse(output.contains(".hiddenFile"));
        }

        @Test
        public void testListDirectoryWithShowAll() {
            String[] parts = {tempDir1.toString(), "-a"};
            CLI.executeCommand("ls" + " " + parts[0]+" " + parts[1]);
            String output = outputStream.toString();

            // Verify the output contains hidden and visible files
            assertTrue(output.contains(".hiddenFile"));
            assertTrue(output.contains("file1.txt"));
            assertTrue(output.contains("file2.txt"));
        }

        @Test
        public void testListDirectoryWithReverseOrder() {
            String[] parts = {tempDir1.toString(), "-r"};
            CLI.executeCommand("ls" + " " + parts[0]+" " + parts[1]);
            String output = outputStream.toString();

            // Split the output by lines and check the order
            String[] lines = output.split(System.lineSeparator());
            assertEquals("file2.txt", lines[1]); // Assuming output is sorted and reversed
            assertEquals("file1.txt", lines[2]);
        }

        @Test
        public void testListNonExistentDirectory() {
            String[] parts = {"non_existent_directory"};
            CLI.executeCommand("ls" + " " + parts[0]);
            String output = outputStream.toString();

            File dir = new File(parts[0]);
            // Verify the output indicates that the directory cannot be read
            assertTrue(output.contains("No files found or directory cannot be read."));

            // Clean up
            dir.delete();
        }

    @Test
    public void testListEmptyDirectory() throws IOException {
        // Set up a ByteArrayOutputStream to capture output directly
        ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(testOutput));

        // Create a temporary empty directory
        Path emptyDir = Files.createTempDirectory("emptyDir");
        String emptyDirPath = emptyDir.toString();

        // Execute the 'ls' command
        CLI.executeCommand("ls " + emptyDirPath);

        // Restore original System.out
        System.setOut(originalOut);

        // Capture the output
        String output = testOutput.toString().trim();
        System.out.println("Actual Output: " + output);
    }
}
