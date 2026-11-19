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

    public static final String PROGRAM_VERSION = "1.2.0";
    public static final String TAB_SEPARATOR = "\t";
    public static final String COMMA_SEPARATOR = ",";

    public static final String[] ELECTRODE8_LABELS = {
        "Fz", "C3", "Cz", "C4", "Pz", "PO7", "Oz", "PO8"
    };
    public static final String[] ELECTRODE16_LABELS = {
        "F1", "F3", "C3", "P3", "O1", "F7", "T3", "T5",
        "F2", "F4", "C4", "P4", "O2", "F8", "T4", "T6"
    };
    public static final String[] ELECTRODE19_LABELS = {
        "F1",  "F3", "C3", "P3", "O1", "F7", "T3", "T5",
        "F2", "F4", "C4", "P4", "O2", "F8", "T4", "T6", "Fz", "Cz", "Pz"
    };

    private static final String NEW_LINE = System.getProperty("line.separator");

    private DegenMatrixProcessor() {
    }

    public static String buildOutputPrefix(String patientName, String condition, String otherCondition) {
        if (otherCondition.isEmpty()) {
            return patientName.trim() + condition.trim() + "EP";
        } else {
            return patientName.trim() + condition.trim() + otherCondition.trim();
        }
    }

    public static void processFromMatrix(
            File matrixFile,
            int numberOfChannels,
            File outputDirectory,
            String outputPrefix,
            boolean isTabSeparated,
            Consumer<String> log) throws IOException {
        
        String elementSeparator = isTabSeparated? TAB_SEPARATOR : COMMA_SEPARATOR;
        
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
                    String[] columns = line.split(elementSeparator);
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
        String[] electrodeLabels;

        switch (numberOfChannels) {
            case 8:
                electrodeLabels = ELECTRODE8_LABELS;
                break;
            case 16:
                electrodeLabels = ELECTRODE16_LABELS;
                break;
            case 19:
                electrodeLabels = ELECTRODE19_LABELS;
                break;
            default:
                electrodeLabels = ELECTRODE16_LABELS;
                break;
        }
        
        for (int i = 0; i < numberOfChannels; i++) {
            File outFile = new File(outputDirectory, outputPrefix + electrodeLabels[i] + ".DAT");
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
