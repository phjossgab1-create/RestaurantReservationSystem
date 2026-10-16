package repository;

import exception.FileOperationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * SINGLETON PATTERN.
 * The only class that reads and writes the .txt files in the "data" folder.
 * Having exactly one instance means there is one place that controls file access.
 *
 * How the Singleton works:
 *   1. The constructor is private, so no one else can call "new FileManager()".
 *   2. The single instance is kept in a private static field.
 *   3. getInstance() creates it the first time, then always returns the same object.
 */
public final class FileManager {

    public static final String CUSTOMER_FILE = "CUSTOMER.txt";
    public static final String RESERVE_FILE = "RESERVE.txt";
    public static final String STATUS_FILE = "STATUS.txt";
    public static final String CANCELLED_FILE = "CANCELLED.txt";
    public static final String EMPLOYEE_FILE = "EMPLOYEE.txt";

    private static FileManager instance;

    private final Path dataDirectory;

    private FileManager() {
        this.dataDirectory = Paths.get("data");
    }

    public static synchronized FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    /** Creates the data folder and all empty .txt files if they don't exist yet. */
    public void initializeDataFiles() throws FileOperationException {
        String[] files = {CUSTOMER_FILE, RESERVE_FILE, STATUS_FILE, CANCELLED_FILE, EMPLOYEE_FILE};
        try {
            Files.createDirectories(dataDirectory);
            for (String file : files) {
                Path path = dataDirectory.resolve(file);
                if (Files.notExists(path)) {
                    Files.createFile(path);
                }
            }
        } catch (IOException e) {
            throw new FileOperationException("Could not create the data files in "
                    + dataDirectory.toAbsolutePath(), e);
        }
    }

    public List<String> readLines(String fileName) throws FileOperationException {
        Path path = dataDirectory.resolve(fileName);
        if (Files.notExists(path)) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new FileOperationException("Could not read " + fileName, e);
        }
    }

    /** Replaces the whole file with the given lines. */
    public void writeLines(String fileName, List<String> lines) throws FileOperationException {
        try {
            Files.createDirectories(dataDirectory);
            Files.write(dataDirectory.resolve(fileName), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileOperationException("Could not write " + fileName, e);
        }
    }

    /** Adds one line to the end of the file. */
    public void appendLine(String fileName, String line) throws FileOperationException {
        try {
            Files.createDirectories(dataDirectory);
            Files.write(dataDirectory.resolve(fileName),
                    (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new FileOperationException("Could not write to " + fileName, e);
        }
    }
}
