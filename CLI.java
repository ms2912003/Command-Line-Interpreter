package org.os;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class CLI {

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String command;

        System.out.println("CLI - Type 'help' for available commands.");

        while (true) {
            System.out.print("CLI> ");
            try {
                command = reader.readLine();
                executeCommand(command);
            } catch (IOException e) {
                System.out.println("Error reading input: " + e.getMessage());
            }
        }
    }

    public static void executeCommand(String command) {
        if (command.isEmpty()) {
            return;
        }

        // Handle redirection and pipe commands first
        if (command.contains("|")) {
            pipeCommand(command);
            return;
        } else if (command.contains(">")) {
            redirectCommand(command);
            return;
        }

        String[] parts = command.split(" ");
        String cmd = parts[0];

        switch (cmd) {
            case "exit":
                System.out.println("Exiting...");
                System.exit(0);
                break;
            case "help":
                helpCommand();
                break;
            case "mkdir":
                createDirectory(parts);
                break;
            case "rmdir":
                removeDirectory(parts);
                break; 
            case "cd":
                changDirectory(parts);
            case "touch":
                touchFile(parts);
                break;
            case "rm":
                removeFile(parts);
                break;
            case "cat":
                displayFile(parts);
                break;
            case "mv":
                moveFile(parts);
                break;
            case "ls":
                listDirectory(parts);
                break;
            default:
                System.err.println("Unknown command: " + cmd);
        }
    }

    private static void helpCommand() {
        String helpText = """
                Available commands:
                - pwd: Print current working directory
                - cd [directory]: Change directory
                - ls: List directory contents
                - mkdir [directory]: Create a new directory
                - rmdir [directory]: Remove an empty directory
                - touch [file]: Create a new file or update timestamp
                - mv [source] [destination]: Move or rename files
                - rm [file]: Remove files
                - cat [file]: Display file contents
                - exit: Exit the CLI
                - help: Show this help message
                - > [file]: Redirect output to file (overwrite)
                - >> [file]: Redirect output to file (append)
                - | [command]: Pipe the output of one command to another
                """;
        System.out.println(helpText);
    }

    private static void createDirectory(String[] parts) {
        if (parts.length > 1) {
            File dir = new File(parts[1]);
            if (!dir.exists()) {
                dir.mkdir();
                System.out.println("Directory created: " + parts[1]);
            } else {
                System.err.println("mkdir: directory already exists: " + parts[1]);
            }
        } else {
            System.err.println("mkdir: missing argument");
        }
    }

    private static void removeDirectory(String[] parts) {
        if (parts.length > 1) {
            File dir = new File(parts[1]);
            if (dir.exists() && dir.isDirectory()) {
                dir.delete();
                System.out.println("Directory removed: " + parts[1]);
            } else {
                System.err.println("rmdir: no such directory: " + parts[1]);
            }
        } else {
            System.err.println("rmdir: missing argument");
        }
    }
    private static void changeDirectory(String[] parts) {
        if (parts.length > 1) {
            File newDir = new File(parts[1]);
            if (newDir.exists() && newDir.isDirectory()) {
                System.setProperty("user.dir", newDir.getAbsolutePath());
                System.out.println("Changed directory to: " + newDir.getAbsolutePath());
            } else {
                System.err.println("cd: no such directory: " + parts[1]);
            }
        } else {
            System.err.println("cd: missing argument");
        }
    }

    private static void touchFile(String[] parts) {
        if (parts.length > 1) {
            try {
                new File(parts[1]).createNewFile();
                System.out.println("File created/updated: " + parts[1]);
            } catch (IOException e) {
                System.err.println("touch: " + e.getMessage());
            }
        } else {
            System.err.println("touch: missing argument");
        }
    }

    private static void removeFile(String[] parts) {
        if (parts.length > 1) {
            File file = new File(parts[1]);
            if (file.exists()) {
                file.delete();
                System.out.println("File removed: " + parts[1]);
            } else {
                System.err.println("rm: no such file: " + parts[1]);
            }
        } else {
            System.err.println("rm: missing argument");
        }
    }

    private static void displayFile(String[] parts) {
        if (parts.length > 1) {
            try (BufferedReader br = new BufferedReader(new FileReader(parts[1]))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.err.println("cat: " + e.getMessage());
            }
        } else {
            System.err.println("cat: missing argument");
        }
    }

    private static void moveFile(String[] parts) {
        if (parts.length < 3) {
            System.err.println("mv: missing arguments");
            return;
        }

        File source = new File(parts[1]);
        File destination = new File(parts[parts.length - 1]);

        // Case 1: Renaming or moving a single file to a different location
        if (parts.length == 3) {
            if (!source.exists()) {
                System.err.println("mv: no such file: " + parts[1]);
                return;
            }

            if (destination.isDirectory()) {
                destination = new File(destination, source.getName());
            }

            // If the destination already exists, prompt the user for confirmation
            if (destination.exists()) {
                System.out.print("Overwrite " + destination.getName() + "? (y/n): ");
                Scanner scanner = new Scanner(System.in);
                String response = scanner.nextLine();
                if (!response.toLowerCase().startsWith("y")) {
                    System.out.println("Skipped: " + source.getName());
                    return;
                }
            }

            // Attempt to move or rename
            if (source.renameTo(destination)) {
                System.out.println("Moved/Renamed: " + source.getName() + " to " + destination.getAbsolutePath());
            } else {
                try {
                    copyFile(source, destination);
                    if (source.delete()) {
                        System.out.println("Moved: " + source.getName() + " to " + destination.getAbsolutePath());
                    } else {
                        System.err.println("mv: failed to delete source file after copying.");
                    }
                } catch (IOException e) {
                    System.err.println("mv: failed to move " + source.getName() + ": " + e.getMessage());
                }
            }
        } else {
            // Case 2: Moving multiple files into a directory
            if (destination.exists() && destination.isDirectory()) {
                for (int i = 1; i < parts.length - 1; i++) {
                    File fileToMove = new File(parts[i]);
                    if (fileToMove.exists()) {
                        File newLocation = new File(destination, fileToMove.getName());

                        if (newLocation.exists()) {
                            System.out.print("Overwrite " + newLocation.getName() + "? (y/n): ");
                            Scanner scanner = new Scanner(System.in);
                            String response = scanner.nextLine();
                            if (!response.toLowerCase().startsWith("y")) {
                                System.out.println("Skipped: " + fileToMove.getName());
                                continue;
                            }
                        }

                        if (!fileToMove.renameTo(newLocation)) {
                            try {
                                copyFile(fileToMove, newLocation);
                                if (fileToMove.delete()) {
                                    System.out.println("Moved: " + fileToMove.getName() + " to " + destination.getAbsolutePath());
                                } else {
                                    System.err.println("mv: failed to delete " + fileToMove.getName() + " after copying.");
                                }
                            } catch (IOException e) {
                                System.err.println("mv: failed to move " + fileToMove.getName() + ": " + e.getMessage());
                            }
                        }
                    } else {
                        System.err.println("mv: no such file: " + parts[i]);
                    }
                }
            } else {
                System.err.println("mv: target is not a directory: " + parts[parts.length - 1]);
            }
        }
    }

    private static void copyFile(File source, File destination) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(destination)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private static void listDirectory(String[] parts) {
        File dir = new File("."); // Default to the current directory
        boolean showAll = false; // Option to show hidden files
        boolean reverseOrder = false; // Option to reverse order

        // Check for options
        for (int i = 1; i < parts.length; i++) {
            if ("-a".equals(parts[i])) {
                showAll = true; // Show hidden files
            } else if ("-r".equals(parts[i])) {
                reverseOrder = true; // Reverse the listing order
            } else {
                dir = new File(parts[i]); // Use the specified directory
            }
        }

        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("ls: no such directory: " + dir.getAbsolutePath());
            return;
        }

        String[] files = dir.list();
        if (files != null) {
            if (!showAll) {
                files = Arrays.stream(files).filter(file -> !file.startsWith(".")).toArray(String[]::new);
            }

            Arrays.sort(files);

            if (reverseOrder) {
                Collections.reverse(Arrays.asList(files));
            }

            System.out.println("Listing directory: " + dir.getAbsolutePath());
            for (String file : files) {
                System.out.println(file);
            }
        } else {
            System.out.println("Directory is empty or cannot be read.");
        }
    }

    // New command handling methods for redirection and piping

    // Handle output redirection ('>' and '>>')
    private static void redirectCommand(String command) {
        // Split command around '>' and trim the parts
        String[] parts = command.contains(">>") ? command.split(">>") : command.split(">");
        String leftCommand = parts[0].trim(); // The command to execute (e.g., "ls")
        String rightCommand = parts[1].trim(); // The file to write to (e.g., "menna.txt")

        boolean append = command.contains(">>"); // Append mode if ">>" is used

        // Create a new File object for the target file
        File outputFile = new File(rightCommand);

        // Print debug information for troubleshooting
        System.out.println("Redirecting output to: " + outputFile.getAbsolutePath());

        // Open a PrintStream to the file
        try (PrintStream ps = new PrintStream(new FileOutputStream(outputFile, append))) {
            // Temporarily redirect System.out to the file
            PrintStream originalOut = System.out;
            System.setOut(ps); // Redirect to the file

            // Execute the command (left part)
            executeCommand(leftCommand); // This will output to the file

            // Restore the original System.out
            System.setOut(originalOut);
            System.out.println("Redirection completed successfully.");
        } catch (FileNotFoundException e) {
            // Detailed error handling for invalid file path or permissions
            System.err.println("Error with file redirection: Unable to write to " + rightCommand + ". File not found or access denied.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error with file redirection: " + e.getMessage());
        }
    }


    // Handle piping ('|')
    private static void pipeCommand(String command) {
        String[] commands = command.split("\\|");
        if (commands.length != 2) {
            System.err.println("Invalid pipe command.");
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream originalOut = System.out;

        // Capture output of the first command
        System.setOut(ps);
        executeCommand(commands[0].trim());
        System.setOut(originalOut);

        // Feed the output of the first command as input to the second
        InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while (true) {
            try {
                line = reader.readLine();
                if (line == null) break;
                System.out.println(line); // Feed each line to the second command
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
