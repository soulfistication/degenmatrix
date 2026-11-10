package degenmatrix;

import degenmatrix.ui.DegenMatrixFrame;
import java.util.Scanner;

/**
 * EEG matrix splitter: converts a 16-column .DAT matrix (or 16 per-electrode
 * files) into per-electrode output files.
 */
public class DegenMatrix {

    /**
     * @param args optional {@code --cli} to use the original console workflow
     */
    public static void main(String[] args) {
        boolean useCli = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg)) {
                useCli = true;
                break;
            }
        }
        if (useCli) {
            runCli();
        } else {
            DegenMatrixFrame.launch();
        }
    }

    private static void runCli() {
        System.out.println("DegenMatrix ver: " + DegenMatrixProcessor.PROGRAM_VERSION);

        Scanner keyboard = new Scanner(System.in);
        System.out.println("Input two letters for name: ");
        String name = keyboard.nextLine();
        System.out.println("Input two letters for condition: ");
        String condition = keyboard.nextLine();
        System.out.println("Input number of channels (8/16/19");
        String channels = keyboard.nextLine();
        int numberOfChannels = Integer.parseInt(channels);

        String prefix = DegenMatrixProcessor.buildOutputPrefix(name, condition);
        java.io.File input = new java.io.File(System.getProperty("user.dir"), "EG.DAT");
        java.io.File outputDir = new java.io.File(System.getProperty("user.dir"));

        try {
            DegenMatrixProcessor.processFromMatrix(input, numberOfChannels, outputDir, prefix, System.out::println);
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        } finally {
            System.out.println("Process finished!");
        }
    }
}
