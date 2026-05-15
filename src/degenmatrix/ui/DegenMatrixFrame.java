package degenmatrix.ui;

import degenmatrix.DegenMatrixProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DegenMatrixFrame extends JFrame {

    private enum InputMode {
        MATRIX, ELECTRODES
    }

    private final JTextField patientNameField = new JTextField(4);
    private final JTextField conditionField = new JTextField(4);
    private final JTextField outputPrefixField = new JTextField(12);
    private final JTextField matrixFileField = new JTextField(28);
    private final JTextField outputDirField = new JTextField(28);
    private final JTextArea logArea = new JTextArea(6, 50);
    private final HeadMapPanel headMapPanel;
    private final JRadioButton matrixModeButton = new JRadioButton("Single matrix file (16 columns)", true);
    private final JRadioButton electrodesModeButton = new JRadioButton("16 separate electrode files", false);

    private File matrixInputFile;
    private File outputDirectory;
    private JButton browseMatrixButton;

    public DegenMatrixFrame() {
        super("DegenMatrix " + DegenMatrixProcessor.PROGRAM_VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 720));
        setLocationRelativeTo(null);

        headMapPanel = new HeadMapPanel(this::onBrowseElectrode);
        buildUi();
        updatePrefixFromPatientFields();
        refreshInputMode();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildPatientPanel(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);
        root.add(buildBottomPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildPatientPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Patient & output naming"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        patientNameField.setToolTipText("Two-letter patient code (used in output file prefix)");
        conditionField.setToolTipText("Two-letter condition code");
        patientNameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                updatePrefixFromPatientFields();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
        });
        conditionField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                updatePrefixFromPatientFields();
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                changed();
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Patient name:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.2;
        panel.add(patientNameField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Condition:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.2;
        panel.add(conditionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Output prefix:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        outputPrefixField.setToolTipText("Prefix for output files, e.g. ABCDEP → ABCDEF1.DAT");
        panel.add(outputPrefixField, gbc);
        gbc.gridwidth = 1;

        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(8, 8));

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        modePanel.setBorder(BorderFactory.createTitledBorder("Input mode"));
        ButtonGroup group = new ButtonGroup();
        group.add(matrixModeButton);
        group.add(electrodesModeButton);
        matrixModeButton.addActionListener(e -> refreshInputMode());
        electrodesModeButton.addActionListener(e -> refreshInputMode());
        modePanel.add(matrixModeButton);
        modePanel.add(electrodesModeButton);
        center.add(modePanel, BorderLayout.NORTH);

        JPanel matrixRow = new JPanel(new GridBagLayout());
        matrixRow.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        matrixRow.add(new JLabel("Matrix input (.DAT):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        matrixFileField.setEditable(false);
        matrixRow.add(matrixFileField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        browseMatrixButton = new JButton("Browse…");
        browseMatrixButton.addActionListener(e -> browseMatrixFile());
        matrixRow.add(browseMatrixButton, gbc);
        center.add(matrixRow, BorderLayout.CENTER);

        JPanel headWrapper = new JPanel(new BorderLayout());
        headWrapper.setBorder(BorderFactory.createTitledBorder("EEG cap — select one .DAT per electrode"));
        headWrapper.add(headMapPanel, BorderLayout.CENTER);
        center.add(headWrapper, BorderLayout.SOUTH);

        return center;
    }

    private JPanel buildBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(8, 8));

        JPanel outputPanel = new JPanel(new GridBagLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Output location"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        outputPanel.add(new JLabel("Output folder:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        outputDirField.setEditable(false);
        outputPanel.add(outputDirField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseOut = new JButton("Browse…");
        browseOut.addActionListener(e -> browseOutputDirectory());
        outputPanel.add(browseOut, gbc);

        JLabel hint = new JLabel(
                "<html>16 files will be written: <i>{prefix}F1.DAT</i> … <i>{prefix}T6.DAT</i></html>");
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        hint.setForeground(new Color(70, 70, 70));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        outputPanel.add(hint, gbc);

        bottom.add(outputPanel, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton runButton = new JButton("Process");
        runButton.addActionListener(this::onProcess);
        actions.add(runButton);
        bottom.add(actions, BorderLayout.CENTER);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Log"));
        bottom.add(logScroll, BorderLayout.SOUTH);

        return bottom;
    }

    private void updatePrefixFromPatientFields() {
        String name = patientNameField.getText().trim();
        String condition = conditionField.getText().trim();
        if (!name.isEmpty() || !condition.isEmpty()) {
            outputPrefixField.setText(DegenMatrixProcessor.buildOutputPrefix(name, condition));
        }
    }

    private void refreshInputMode() {
        boolean matrix = matrixModeButton.isSelected();
        matrixFileField.setEnabled(matrix);
        if (browseMatrixButton != null) {
            browseMatrixButton.setEnabled(matrix);
        }
        headMapPanel.setElectrodesEnabled(!matrix);
    }

    private void onBrowseElectrode(ActionEvent event) {
        if (matrixModeButton.isSelected()) {
            return;
        }
        int index = headMapPanel.electrodeIndexFromSource(event.getSource());
        if (index < 0) {
            return;
        }
        JFileChooser chooser = createDatChooser("Select input for "
                + DegenMatrixProcessor.ELECTRODE_LABELS[index]);
        File current = headMapPanel.getElectrodeFile(index);
        if (current != null) {
            chooser.setSelectedFile(current);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            headMapPanel.setElectrodeFile(index, chooser.getSelectedFile());
        }
    }

    private void browseMatrixFile() {
        JFileChooser chooser = createDatChooser("Select matrix .DAT file");
        if (matrixInputFile != null) {
            chooser.setSelectedFile(matrixInputFile);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            matrixInputFile = chooser.getSelectedFile();
            matrixFileField.setText(matrixInputFile.getAbsolutePath());
        }
    }

    private void browseOutputDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select output folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (outputDirectory != null) {
            chooser.setSelectedFile(outputDirectory);
        }
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputDirectory = chooser.getSelectedFile();
            outputDirField.setText(outputDirectory.getAbsolutePath());
        }
    }

    private static JFileChooser createDatChooser(String title) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileNameExtensionFilter("DAT files (*.DAT)", "DAT", "dat"));
        return chooser;
    }

    private void onProcess(ActionEvent event) {
        String prefix = outputPrefixField.getText().trim();
        if (prefix.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Enter patient name and condition, or set an output prefix.",
                    "Missing prefix",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (outputDirectory == null) {
            JOptionPane.showMessageDialog(this,
                    "Choose an output folder.",
                    "Missing output",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        final InputMode mode = matrixModeButton.isSelected() ? InputMode.MATRIX : InputMode.ELECTRODES;
        if (mode == InputMode.MATRIX && matrixInputFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Select the matrix input .DAT file.",
                    "Missing input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (mode == InputMode.ELECTRODES) {
            File[] files = headMapPanel.getAllElectrodeFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i] == null) {
                    JOptionPane.showMessageDialog(this,
                            "Select input .DAT for electrode "
                                    + DegenMatrixProcessor.ELECTRODE_LABELS[i] + ".",
                            "Missing input",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        logArea.setText("");
        appendLog("Processing…");

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (mode == InputMode.MATRIX) {
                    DegenMatrixProcessor.processFromMatrix(
                            matrixInputFile,
                            outputDirectory,
                            prefix,
                            this::publish);
                } else {
                    DegenMatrixProcessor.processFromElectrodeFiles(
                            headMapPanel.getAllElectrodeFiles(),
                            outputDirectory,
                            prefix,
                            this::publish);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String line : chunks) {
                    appendLog(line);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    appendLog("Done.");
                    JOptionPane.showMessageDialog(DegenMatrixFrame.this,
                            "Processing finished successfully.",
                            "DegenMatrix",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    appendLog("Error: " + ex.getMessage());
                    JOptionPane.showMessageDialog(DegenMatrixFrame.this,
                            ex.getMessage(),
                            "Processing failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void appendLog(String message) {
        logArea.append(message + System.lineSeparator());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            DegenMatrixFrame frame = new DegenMatrixFrame();
            frame.setVisible(true);
        });
    }
}
