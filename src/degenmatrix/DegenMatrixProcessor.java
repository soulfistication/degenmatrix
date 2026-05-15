package degenmatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Splits a 16-column matrix .DAT file or merges 16 per-electrode files into
 * converted per-electrode output files.
 */
public final class DegenMatrixProcessor {

    public static final String PROGRAM_VERSION = "1.0.0";
    public static final String ELEMENT_SEPARATOR = "\t";
    public static final int NUMBER_OF_COLUMNS = 16;

    public static final String[] ELECTRODE_LABELS = {
        "F1", "F3", "C3", "P3", "O1", "F7", "T3", "T5",
        "F2", "F4", "C4", "P4", "O2", "F8", "T4", "T6"
    };

    private static final String NEW_LINE = System.getProperty("line.separator");

    private DegenMatrixProcessor() {
    }

    public static String buildOutputPrefix(String patientName, String condition) {
        return patientName.trim() + condition.trim() + "EP";
    }

    public static void processFromMatrix(
            File matrixFile,
            File outputDirectory,
            String outputPrefix,
            Consumer<String> log) throws IOException {
        if (!matrixFile.isFile()) {
            throw new IOException("Matrix file not found: " + matrixFile);
        }
        if (!outputDirectory.isDirectory() && !outputDirectory.mkdirs()) {
            throw new IOException("Cannot create output directory: " + outputDirectory);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(matrixFile))) {
            BufferedWriter[] writers = openWriters(outputDirectory, outputPrefix);
            try {
                String line = reader.readLine();
                int row = 0;
                while (line != null) {
                    if (line.startsWith("[")) {
                        line = reader.readLine();
                        continue;
                    }
                    row++;
                    String[] columns = line.split(ELEMENT_SEPARATOR);
                    if (columns.length != NUMBER_OF_COLUMNS) {
                        log.accept("WARNING: Row " + row + " has " + columns.length
                                + " columns (expected " + NUMBER_OF_COLUMNS + ").");
                    }
                    for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                        String value = i < columns.length ? columns[i] : "0";
                        writers[i].write(convert(value) + NEW_LINE);
                    }
                    line = reader.readLine();
                }
            } finally {
                closeWriters(writers);
            }
        }
        log.accept("Created 16 electrode files in " + outputDirectory.getAbsolutePath());
    }

    public static void processFromElectrodeFiles(
            File[] inputFiles,
            File outputDirectory,
            String outputPrefix,
            Consumer<String> log) throws IOException {
        if (inputFiles.length != NUMBER_OF_COLUMNS) {
            throw new IllegalArgumentException("Expected " + NUMBER_OF_COLUMNS + " input files.");
        }
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            if (inputFiles[i] == null || !inputFiles[i].isFile()) {
                throw new IOException("Missing input file for " + ELECTRODE_LABELS[i]);
            }
        }
        if (!outputDirectory.isDirectory() && !outputDirectory.mkdirs()) {
            throw new IOException("Cannot create output directory: " + outputDirectory);
        }

        BufferedReader[] readers = new BufferedReader[NUMBER_OF_COLUMNS];
        BufferedWriter[] writers = openWriters(outputDirectory, outputPrefix);
        try {
            for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                readers[i] = new BufferedReader(new FileReader(inputFiles[i]));
            }
            int row = 0;
            while (true) {
                String[] values = new String[NUMBER_OF_COLUMNS];
                boolean endOfFiles = false;
                for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                    String line = readDataLine(readers[i]);
                    if (line == null) {
                        endOfFiles = true;
                        break;
                    }
                    values[i] = line.trim();
                }
                if (endOfFiles) {
                    break;
                }
                row++;
                for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                    writers[i].write(convert(values[i]) + NEW_LINE);
                }
            }
            log.accept("Processed " + row + " rows from 16 electrode files.");
        } finally {
            for (BufferedReader reader : readers) {
                if (reader != null) {
                    reader.close();
                }
            }
            closeWriters(writers);
        }
        log.accept("Created 16 electrode files in " + outputDirectory.getAbsolutePath());
    }

    private static BufferedWriter[] openWriters(File outputDirectory, String outputPrefix)
            throws IOException {
        BufferedWriter[] writers = new BufferedWriter[NUMBER_OF_COLUMNS];
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            File outFile = new File(outputDirectory, outputPrefix + ELECTRODE_LABELS[i] + ".DAT");
            writers[i] = new BufferedWriter(new FileWriter(outFile));
        }
        return writers;
    }

    private static void closeWriters(BufferedWriter[] writers) throws IOException {
        for (BufferedWriter writer : writers) {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static String readDataLine(BufferedReader reader) throws IOException {
        String line;
        do {
            line = reader.readLine();
        } while (line != null && line.startsWith("["));
        return line;
    }

    public static int convert(String pointString) {
        if (pointString == null || pointString.isEmpty()) {
            return 0;
        }
        try {
            double point = Double.parseDouble(pointString.trim());
            return (int) (point * 10);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
}
