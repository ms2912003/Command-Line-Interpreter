package org.os;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.io.File;
import java.io.FileWriter;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.*;
import org.os.CLI;

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

public class CLITesting {
    private String originalDir;

    @Before
    public void setUp() {
        // Save the original working directory
        originalDir = System.getProperty("user.dir");
    }

    @After
    public void tearDown() {
        // Restore the original directory after each test
        System.setProperty("user.dir", originalDir);
        // Clean up any created directories
        File dir = new File("testDir");
        if (dir.exists()) {
            dir.delete();
        }
    }

    @Test
    public void testChangeDirectory() {
        // Arrange
        String testDirName = "testDir";
        new File(testDirName).mkdir(); // Create a test directory

        // Act
        CLI.executeCommand("cd " + testDirName); // Replace CLI with your actual class name

        // Assert
        String expectedDir = new File(testDirName).getAbsolutePath();
        assertEquals(expectedDir, System.getProperty("user.dir"));

        // Clean up
        new File(testDirName).delete();
    }

    @Test
    public void testChangeToNonExistingDirectory() {
        // Arrange
        String nonExistingDir = "nonExistingDir";

        // Act
        CLI.executeCommand("cd " + nonExistingDir); // Replace CLI with your actual class name

        // Assert
        assertEquals(originalDir, System.getProperty("user.dir"));
    }

    @Test
    public void testMissingArgument() {
        // Act
        CLI.executeCommand("cd"); // Replace CLI with your actual class name

        // Assert
        assertEquals(originalDir, System.getProperty("user.dir"));
    }
}
