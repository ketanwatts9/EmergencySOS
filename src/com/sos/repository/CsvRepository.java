package com.sos.repository;

import com.sos.exception.AppException;
import com.sos.model.Identifiable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic base class that keeps objects in a LinkedHashMap (memory) and saves them to a CSV file.
 * Subclasses only say HOW one object becomes one CSV line and back.
 * Stage 2: we will write Jdbc repositories that implement the same interfaces.
 */
public abstract class CsvRepository<T extends Identifiable> implements Repository<T> {
    protected final Map<Integer, T> items = new LinkedHashMap<>();   // id -> object, keeps insertion order
    private final File file;
    private int nextId = 1;

    protected CsvRepository(String path) throws AppException {
        this.file = new File(path);
        load();
    }

    protected abstract String toCsv(T item);
    protected abstract T fromCsv(String line) throws AppException;

    // ---------- file reading (always UTF-8, so Hindi / Punjabi names survive on every computer) ----------
    private void load() throws AppException {
        if (!file.exists()) return;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                T item = fromCsv(line);
                items.put(item.getId(), item);
                if (item.getId() >= nextId) nextId = item.getId() + 1;
            }
        } catch (IOException | RuntimeException e) {          // multi-catch
            throw new AppException(500, "Could not read " + file.getName());
        } finally {
            closeQuietly(reader);                              // finally always runs
        }
    }

    // ---------- file writing ----------
    private void writeAll() throws AppException {
        File folder = file.getParentFile();
        if (folder != null && !folder.exists()) folder.mkdirs();

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));
            for (T item : items.values()) {
                writer.write(toCsv(item));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new AppException(500, "Could not write " + file.getName());
        } finally {
            closeQuietly(writer);
        }
    }

    private void closeQuietly(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (IOException e) {
            // nothing useful to do
        }
    }

    // ---------- Repository methods (synchronized: HTTP thread and dispatcher thread both use us) ----------
    @Override
    public synchronized void save(T item) throws AppException {
        if (item.getId() == 0) item.setId(nextId++);
        items.put(item.getId(), item);
        writeAll();
    }

    @Override
    public synchronized T findById(int id) { return items.get(id); }

    @Override
    public synchronized List<T> findAll() { return new ArrayList<>(items.values()); }

    @Override
    public synchronized void delete(int id) throws AppException {
        items.remove(id);
        writeAll();
    }
}
