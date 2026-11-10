package degenmatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Splits a 8, 16 or 19-column matrix .DAT file
 */
public final class DegenMatrixProcessor {

    public static final String PROGRAM_VERSION = "1.1.0";
    public static final String ELEMENT_SEPARATOR = "\t";

    public static final String[] ELECTRODE_LABELS = {
        "F1", "F3", "C3", "P3", "O1", "F7", "T3", "T5",
        "F2", "F4", "C4", "P4", "O2", "F8", "T4", "T6", "Z1", "Z2", "Z3"
    };

    private static final String NEW_LINE = System.getProperty("line.separator");

    private DegenMatrixProcessor() {
    }

    public static String buildOutputPrefix(String patientName, String condition) {
        return patientName.trim() + condition.trim() + "EP";
    }

    public static void processFromMatrix(
            File matrixFile,
            int numberOfChannels,
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
            BufferedWriter[] writers = openWriters(outputDirectory, numberOfChannels, outputPrefix);
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
                    for (int i = 0; i < numberOfChannels; i++) {
                        String value = columns[i];
                        writers[i].write(convert(value) + NEW_LINE);
                    }
                    line = reader.readLine();
                }
            } finally {
                closeWriters(writers);
            }
        }
        log.accept("Created "  + numberOfChannels + " electrode files in " + outputDirectory.getAbsolutePath());
    }

    private static BufferedWriter[] openWriters(File outputDirectory, int numberOfChannels, String outputPrefix)
            throws IOException {
        BufferedWriter[] writers = new BufferedWriter[numberOfChannels];
        for (int i = 0; i < numberOfChannels; i++) {
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
