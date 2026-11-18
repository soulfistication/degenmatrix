package degenmatrix.ui;

import degenmatrix.DegenMatrixProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DegenMatrixFrame extends JFrame {

    private enum InputMode {
        CHANNELS8, CHANNELS16, CHANNELS19
    }
    
    private InputMode mode = InputMode.CHANNELS16;

    private final JTextField patientNameField = new JTextField(4);
    private final JTextField conditionField = new JTextField(4);
    private final JTextField otherConditionField = new JTextField(4);
    private final JTextField outputPrefixField = new JTextField(4);
    private final JTextField matrixFileField = new JTextField(28);
    private final JTextField outputDirField = new JTextField(28);
    private final JTextArea logArea = new JTextArea(6, 50);
    private final JRadioButton channels8RadioButton = new JRadioButton("8 channels", false);
    private final JRadioButton channels16RadioButton = new JRadioButton("16 channels", true);
    private final JRadioButton channels19RadioButton = new JRadioButton("19 channels", false);
    
    // Delimiter switch: false = Comma separated values, true = Tab separated values
    private final UISwitch delimiterSwitch = new UISwitch();
    private final JLabel commaLabel = new JLabel("Comma separated values");
    private final JLabel tabLabel = new JLabel("Tab separated values");

    private File matrixInputFile;
    private File outputDirectory;
    private JButton browseMatrixButton;

    public DegenMatrixFrame() {
        super("DegenMatrix " + DegenMatrixProcessor.PROGRAM_VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 720));
        setLocationRelativeTo(null);

        buildUi();
        updatePrefixFromPatientFields();
        refreshInputMode();
        updateDelimiterLabelStyles();
        
        // Setup MatrixFields
        matrixFileField.setEnabled(true);
        browseMatrixButton.setEnabled(true);
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
        outputPrefixField.setToolTipText("Prefix for output files, e.g. ABCDEP → ABCDEF1.DAT");
        otherConditionField.setToolTipText("Two-letter code for other prefix");
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
        otherConditionField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                updatePrefixFromPatientFields();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
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
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        panel.add(outputPrefixField, gbc);
        
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Other condition:"), gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.2;
        panel.add(otherConditionField, gbc);
        
        gbc.gridwidth = 1;

        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(8, 8));

        JPanel modePanel = new JPanel(new BorderLayout(8, 4));
        modePanel.setBorder(BorderFactory.createTitledBorder("Number of Channels"));

        // Left side: Channel radio buttons
        JPanel channelsGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        ButtonGroup group = new ButtonGroup();
        group.add(channels8RadioButton);
        group.add(channels16RadioButton);
        group.add(channels19RadioButton);
        channels8RadioButton.addActionListener(e -> refreshInputMode());
        channels16RadioButton.addActionListener(e -> refreshInputMode());
        channels19RadioButton.addActionListener(e -> refreshInputMode());
        channelsGroupPanel.add(channels8RadioButton);
        channelsGroupPanel.add(channels16RadioButton);
        channelsGroupPanel.add(channels19RadioButton);

        // Right side: Delimiter UISwitch with flanking labels
        JPanel delimiterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        commaLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tabLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        commaLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (delimiterSwitch.isSelected()) {
                    delimiterSwitch.doClick();
                }
            }
        });
        tabLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!delimiterSwitch.isSelected()) {
                    delimiterSwitch.doClick();
                }
            }
        });
        delimiterSwitch.addItemListener(e -> updateDelimiterLabelStyles());

        delimiterPanel.add(commaLabel);
        delimiterPanel.add(delimiterSwitch);
        delimiterPanel.add(tabLabel);

        modePanel.add(channelsGroupPanel, BorderLayout.WEST);
        modePanel.add(delimiterPanel, BorderLayout.EAST);
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
        return center;
    }

    private void updateDelimiterLabelStyles() {
        boolean isTabSelected = delimiterSwitch.isSelected();
        Font baseFont = commaLabel.getFont();
        
        commaLabel.setFont(baseFont.deriveFont(isTabSelected ? Font.PLAIN : Font.BOLD));
        commaLabel.setForeground(isTabSelected ? Color.GRAY : Color.BLACK);
        
        tabLabel.setFont(baseFont.deriveFont(isTabSelected ? Font.BOLD : Font.PLAIN));
        tabLabel.setForeground(isTabSelected ? Color.BLACK : Color.GRAY);
    }

    /**
     * Helper method to query the selected delimiter in your processor call.
     * @return true if Tab separated values is selected, false if Comma separated values is selected.
     */
    public boolean isTabSeparated() {
        return delimiterSwitch.isSelected();
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
        String otherCondition = otherConditionField.getText().trim();
        if (!name.isEmpty() || !condition.isEmpty()) {
            outputPrefixField.setText(DegenMatrixProcessor.buildOutputPrefix(name, condition, otherCondition));
        }
    }

    private void refreshInputMode() {
        boolean is8ChannelsSelected = channels8RadioButton.isSelected();
        boolean is16ChannelsSelected = channels16RadioButton.isSelected();
        boolean is19ChannelsSelected = channels19RadioButton.isSelected();
        
        if (is8ChannelsSelected) {
            mode = InputMode.CHANNELS8;
            return;
        }
        
        if (is16ChannelsSelected) {
            mode = InputMode.CHANNELS16;
            return;
        }
        
        if (is19ChannelsSelected) {
            mode = InputMode.CHANNELS19;
        }
    }

    private void onBrowseElectrode(ActionEvent event) {
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

        if (matrixInputFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Select the matrix input .DAT file.",
                    "Missing input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        logArea.setText("");
        appendLog("Processing…");

        // Example usage of the switch state:
        final boolean tabSeparated = isTabSeparated();

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                int channelCount = (mode == InputMode.CHANNELS8) ? 8 : (mode == InputMode.CHANNELS16 ? 16 : 19);
                
                // Pass `tabSeparated` (or `tabSeparated ? "\t" : ","`) to DegenMatrixProcessor if needed
                DegenMatrixProcessor.processFromMatrix(
                        matrixInputFile, 
                        channelCount,
                        outputDirectory,
                        prefix,
                        tabSeparated,
                        this::publish);
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