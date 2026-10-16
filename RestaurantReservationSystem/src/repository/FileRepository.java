package repository;

import exception.FileOperationException;
import exception.InvalidInputException;
import exception.ReservationSystemException;
import model.Storable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared CRUD logic for any repository that stores one object per line in a .txt file.
 * Subclasses only say how to parse a line, how to get an ID, and what "not found" means.
 * (Abstraction + Inheritance: the CRUD code is written once and reused.)
 */
public abstract class FileRepository<T extends Storable> implements CrudRepository<T> {

    protected final FileManager fileManager = FileManager.getInstance();
    private final String fileName;

    protected FileRepository(String fileName) {
        this.fileName = fileName;
    }

    /** Turns one saved line back into an object. */
    protected abstract T parse(String line);

    /** Returns the object's unique ID. */
    protected abstract String getId(T item);

    /** The exception to throw when an ID doesn't exist. */
    protected abstract ReservationSystemException notFound(String id);

    protected String getFileName() {
        return fileName;
    }

    @Override
    public void create(T item) throws ReservationSystemException {
        if (existsById(getId(item))) {
            throw new InvalidInputException("A record with ID " + getId(item) + " already exists.");
        }
        fileManager.appendLine(fileName, item.toFileString());
    }

    @Override
    public T findById(String id) throws ReservationSystemException {
        for (T item : findAll()) {
            if (getId(item).equalsIgnoreCase(id.trim())) {
                return item;
            }
        }
        throw notFound(id);
    }

    @Override
    public List<T> findAll() throws FileOperationException {
        return parseLines(fileName);
    }

    @Override
    public void update(T updated) throws ReservationSystemException {
        List<T> items = findAll();
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            if (getId(items.get(i)).equalsIgnoreCase(getId(updated))) {
                items.set(i, updated);
                found = true;
                break;
            }
        }
        if (!found) {
            throw notFound(getId(updated));
        }
        saveAll(items);
    }

    @Override
    public void delete(String id) throws ReservationSystemException {
        List<T> items = findAll();
        boolean removed = items.removeIf(item -> getId(item).equalsIgnoreCase(id.trim()));
        if (!removed) {
            throw notFound(id);
        }
        saveAll(items);
    }

    public boolean existsById(String id) throws FileOperationException {
        for (T item : findAll()) {
            if (getId(item).equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    /** Reads every line of a file into objects, skipping (and reporting) corrupted lines. */
    protected List<T> parseLines(String file) throws FileOperationException {
        List<T> items = new ArrayList<>();
        int lineNumber = 0;
        for (String line : fileManager.readLines(file)) {
            lineNumber++;
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                items.add(parse(line));
            } catch (RuntimeException e) {
                System.out.println("[Warning] Skipped corrupted line " + lineNumber
                        + " in " + file + ": " + e.getMessage());
            }
        }
        return items;
    }

    protected void saveAll(List<T> items) throws FileOperationException {
        List<String> lines = new ArrayList<>();
        for (T item : items) {
            lines.add(item.toFileString());
        }
        fileManager.writeLines(fileName, lines);
    }
}
