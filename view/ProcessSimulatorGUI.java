package view;

import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProcessSimulatorGUI extends JFrame implements ActionListener {
    private ProcessManager processManager;
    
    private JTextField txtProcessName;
    private JTextField txtProcessTime;
    private JTextField txtPriority;
    private JTextField txtPriorityChange;
    private JComboBox<String> cmbStatus;
    private JComboBox<String> cmbSuspended;
    private JComboBox<String> cmbResumed;
    private JComboBox<String> cmbDestroyed;
    
    private JPanel referencedProcessesPanel;
    private JScrollPane referencedProcessesScrollPane;
    private List<JCheckBox> referencedProcessesCheckboxes;
    
    private DefaultTableModel processTableModel;
    private JTable processTable;
    
    private JPanel resultsPanel;
    private CardLayout cardLayout;
    
    private DefaultTableModel[] resultTableModels;
    private String[] tableNames = {
        "Inicial", "Listos", "Despachados", "En Ejecución", 
        "Tiempo Expirado", "Bloqueados", "Despertar", "Finalizados",
        "Prioridad Cambiada", "Suspendidos", "Reanudados", "Destruidos", "Relacion-Comunicacion"
    };
    private Filter[] filters = {
        Filter.INICIAL, Filter.LISTO, Filter.DESPACHADO, Filter.EN_EJECUCION,
        Filter.TIEMPO_EXPIRADO, Filter.BLOQUEADO, Filter.DESPERTAR, Filter.FINALIZADO,
        Filter.PRIORIDAD_CAMBIADA, Filter.SUSPENDIDO, Filter.REANUDADO, Filter.DESTRUIDO,
        Filter.TODO 
    };

    private String currentAction;
    private NumberFormat numberFormatter;

    public ProcessSimulatorGUI() {
        processManager = new ProcessManager();
        numberFormatter = NumberFormat.getNumberInstance(new Locale("es", "ES"));
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        setUndecorated(true); 
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void initializeComponents() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        txtProcessName = new JTextField(15);
        txtProcessTime = new JTextField(15);
        txtPriority = new JTextField(15);
        txtPriorityChange = new JTextField(15);
        cmbStatus = new JComboBox<>(new String[]{"No Bloqueado", "Bloqueado"});
        cmbSuspended = new JComboBox<>(new String[]{"No", "Si"});
        cmbResumed = new JComboBox<>(new String[]{"No", "Si"});
        cmbDestroyed = new JComboBox<>(new String[]{"No", "Si"});
        
        referencedProcessesPanel = new JPanel();
        referencedProcessesPanel.setLayout(new BoxLayout(referencedProcessesPanel, BoxLayout.Y_AXIS));
        referencedProcessesCheckboxes = new ArrayList<>();
        referencedProcessesScrollPane = new JScrollPane(referencedProcessesPanel);
        referencedProcessesScrollPane.setPreferredSize(new Dimension(200, 80));
        referencedProcessesScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        referencedProcessesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        setupTimeField();
        setupPriorityFields();
        updateReferencedProcessCheckboxes();

        processTableModel = new DefaultTableModel(
            new String[]{"Nombre", "Tiempo", "Prioridad", "Estado", "Suspendido", "Reanudado", "Destruido", "Comunicacion"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        processTable = new JTable(processTableModel);
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        cardLayout = new CardLayout();
        resultsPanel = new JPanel(cardLayout);
        
        resultTableModels = new DefaultTableModel[tableNames.length];
        for (int i = 0; i < tableNames.length; i++) {
            if (i < 8) {
                resultTableModels[i] = new DefaultTableModel(
                    new String[]{"Proceso", "Tiempo Restante", "Prioridad", "Estado", "Suspendido", "Reanudado", "Destruido", "Comunicacion", "Ciclos"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
            } else if (i == 8 || i == 12) {
                resultTableModels[i] = new DefaultTableModel(
                    new String[]{"Proceso", "Información"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
            } else {
                resultTableModels[i] = new DefaultTableModel(
                    new String[]{"Proceso", "Tiempo Original", "Prioridad", "Estado", "Suspendido", "Reanudado", "Destruido", "Comunicacion", "Información"}, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
            }
            
            JTable table = new JTable(resultTableModels[i]);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            JScrollPane scrollPane = new JScrollPane(table);
            resultsPanel.add(scrollPane, tableNames[i]);
        }
    }

  
    private void setupTimeField() {
        txtProcessTime.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                
                
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                    return;
                }
                
                
                if (Character.isDigit(c)) {
                    SwingUtilities.invokeLater(() -> {
                        formatTimeFieldInRealTime();
                    });
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {
               
                formatTimeFieldInRealTime();
            }
        });
    }

    private void formatTimeFieldInRealTime() {
        String text = txtProcessTime.getText().replaceAll("[^0-9]", "");
        if (!text.isEmpty()) {
            try {
      
                
                String displayText = text;
                
                
                if (text.length() > 18) {
                   
                    StringBuilder formatted = new StringBuilder();
                    int count = 0;
                    for (int i = displayText.length() - 1; i >= 0; i--) {
                        if (count > 0 && count % 3 == 0) {
                            formatted.insert(0, ".");
                        }
                        formatted.insert(0, displayText.charAt(i));
                        count++;
                    }
                    displayText = formatted.toString();
                } else {
                    
                    long number = Long.parseLong(text);
                    displayText = numberFormatter.format(number);
                }
                
                if (!txtProcessTime.getText().equals(displayText)) {
                    int caretPos = txtProcessTime.getCaretPosition();
                    txtProcessTime.setText(displayText);
                    try {
                        
                        int newCaretPos = Math.min(caretPos + (displayText.length() - text.length()), displayText.length());
                        txtProcessTime.setCaretPosition(newCaretPos);
                    } catch (IllegalArgumentException ex) {
                        txtProcessTime.setCaretPosition(displayText.length());
                    }
                }
            } catch (NumberFormatException ex) {
           
                if (text.length() > 0) {
                    StringBuilder formatted = new StringBuilder();
                    int count = 0;
                    for (int i = text.length() - 1; i >= 0; i--) {
                        if (count > 0 && count % 3 == 0) {
                            formatted.insert(0, ".");
                        }
                        formatted.insert(0, text.charAt(i));
                        count++;
                    }
                    
                    if (!txtProcessTime.getText().equals(formatted.toString())) {
                        int caretPos = txtProcessTime.getCaretPosition();
                        txtProcessTime.setText(formatted.toString());
                        try {
                            txtProcessTime.setCaretPosition(Math.min(caretPos, formatted.length()));
                        } catch (IllegalArgumentException ex2) {
                            txtProcessTime.setCaretPosition(formatted.length());
                        }
                    }
                }
            }
        }
    }

    private void setupPriorityFields() {
        KeyListener priorityListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
        };
        
        txtPriority.addKeyListener(priorityListener);
        txtPriorityChange.addKeyListener(priorityListener);
    }

    private void formatTimeField() {
        String text = txtProcessTime.getText().replaceAll("[^0-9]", "");
        if (!text.isEmpty()) {
            try {
                if (text.length() > 18) {
                    return; 
                }
                
                long number = Long.parseLong(text);
                String formatted = numberFormatter.format(number);
                
                if (!txtProcessTime.getText().equals(formatted)) {
                    int caretPos = txtProcessTime.getCaretPosition();
                    txtProcessTime.setText(formatted);
                    try {
                        txtProcessTime.setCaretPosition(Math.min(caretPos, formatted.length()));
                    } catch (IllegalArgumentException ex) {
                        txtProcessTime.setCaretPosition(formatted.length());
                    }
                }
            } catch (NumberFormatException ex) {
               
            }
        }
    }

    private long parseTimeWithTrick(String timeText) throws NumberFormatException {
        String numbersOnly = timeText.replaceAll("[^0-9]", "");
        if (numbersOnly.isEmpty()) {
            throw new NumberFormatException("Campo vacío");
        }
        
        if (numbersOnly.length() > 18) {
            numbersOnly = numbersOnly.substring(0, 18);
            
        }
        
        while (numbersOnly.length() > 1) {
            try {
                long result = Long.parseLong(numbersOnly);
            
                return result;
            } catch (NumberFormatException ex) {
                
                numbersOnly = numbersOnly.substring(1); 
               
            }
        }
        
        return Long.parseLong(numbersOnly);
    }

    private long parseTimeField() throws NumberFormatException {
        return parseTimeWithTrick(txtProcessTime.getText());
    }

    private long parseTimeFieldForDialog(JTextField timeField) throws NumberFormatException {
        return parseTimeWithTrick(timeField.getText());
    }

    private void updateReferencedProcessCheckboxes() {
        referencedProcessesPanel.removeAll();
        referencedProcessesCheckboxes.clear();
        
        List<model.Process> processes = processManager.getInitialProcesses();
        
        if (processes.isEmpty()) {
            JLabel noProcessesLabel = new JLabel("No hay procesos disponibles");
            noProcessesLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            noProcessesLabel.setForeground(Color.GRAY);
            referencedProcessesPanel.add(noProcessesLabel);
        } else {
            for (model.Process p : processes) {
                JCheckBox checkBox = new JCheckBox(p.getName());
                checkBox.setFont(new Font("Arial", Font.PLAIN, 12));
                referencedProcessesCheckboxes.add(checkBox);
                referencedProcessesPanel.add(checkBox);
            }
        }
        
        referencedProcessesPanel.revalidate();
        referencedProcessesPanel.repaint();
    }

    private List<String> getSelectedReferencedProcesses() {
        List<String> selected = new ArrayList<>();
        for (JCheckBox checkBox : referencedProcessesCheckboxes) {
            if (checkBox.isSelected()) {
                selected.add(checkBox.getText());
            }
        }
        return selected;
    }

    private void clearReferencedProcessesSelection() {
        for (JCheckBox checkBox : referencedProcessesCheckboxes) {
            checkBox.setSelected(false);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(44, 62, 80));
        JLabel titleLabel = new JLabel("SIMULADOR DE PROCESOS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.setPreferredSize(new Dimension(500, 0));

        JPanel formPanel = createFormPanel();
        leftPanel.add(formPanel, BorderLayout.NORTH);

        JScrollPane tableScrollPane = new JScrollPane(processTable);
        tableScrollPane.setPreferredSize(new Dimension(480, 250));
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);

        JPanel actionPanel = createActionPanel();
        leftPanel.add(actionPanel, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Resultados de la Simulación"));

        JPanel buttonPanel = createResultButtonPanel();
        rightPanel.add(buttonPanel, BorderLayout.NORTH);
        
        rightPanel.add(resultsPanel, BorderLayout.CENTER);

        add(titlePanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Crear Nuevo Proceso"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        panel.add(txtProcessName, gbc);
        row++;

        // Time
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Tiempo:"), gbc);
        gbc.gridx = 1;
        panel.add(txtProcessTime, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Prioridad:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPriority, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Cambio Prioridad:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPriorityChange, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbStatus, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Suspendido:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbSuspended, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Reanudado:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbResumed, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Destruido:"), gbc);
        gbc.gridx = 1;
        panel.add(cmbDestroyed, gbc);
        row++;

        
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Comunicar procesos:"), gbc);
        gbc.gridx = 1;
        panel.add(referencedProcessesScrollPane, gbc);

        return panel;
    }



    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; 

        JButton btnAdd = new JButton("Agregar");
        JButton btnEdit = new JButton("Modificar");
        JButton btnDelete = new JButton("Eliminar");
        JButton btnSimulate = new JButton("Ejecutar Simulación");
        JButton btnReset = new JButton("Limpiar Todo");
        JButton btnExit = new JButton("Salir");
        JButton btnManual = new JButton("Manual de usuario");


        Dimension buttonSize = new Dimension(140, 35);
        btnAdd.setPreferredSize(buttonSize);
        btnEdit.setPreferredSize(buttonSize);
        btnDelete.setPreferredSize(buttonSize);
        btnSimulate.setPreferredSize(buttonSize);
        btnReset.setPreferredSize(buttonSize);
        btnExit.setPreferredSize(buttonSize);
        btnManual.setPreferredSize(buttonSize);


        btnSimulate.setBackground(new Color(46, 125, 50)); // Verde oscuro
        btnSimulate.setForeground(Color.WHITE);
        btnSimulate.setOpaque(true);
        btnSimulate.setBorderPainted(false);
        btnSimulate.setFocusPainted(false);
        
       
        btnExit.setBackground(new Color(198, 40, 40)); // Rojo oscuro
        btnExit.setForeground(Color.WHITE);
        btnExit.setOpaque(true);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);

     
        btnAdd.addActionListener(e -> addProcess());
        btnEdit.addActionListener(e -> editProcess());
        btnDelete.addActionListener(e -> deleteProcess());
        btnSimulate.addActionListener(e -> runSimulation());
        btnExit.addActionListener(e -> System.exit(0));
        btnManual.addActionListener(e -> openUserManual());
        btnReset.addActionListener(e -> clearAll());


        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(btnAdd, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(btnEdit, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(btnDelete, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(btnSimulate, gbc);


        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(btnManual, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(btnReset, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(btnExit, gbc);

    
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(Box.createRigidArea(new Dimension(0, 0)), gbc);

        return panel;
    }

    private JPanel createResultButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 5, 5, 5)); 
        
        for (int i = 0; i < tableNames.length; i++) {
            JButton btn = new JButton(tableNames[i]);
            btn.setPreferredSize(new Dimension(120, 30));
            final int index = i;
            btn.addActionListener(e -> {
                cardLayout.show(resultsPanel, tableNames[index]);
                updateResultTable(index);
            });
            panel.add(btn);
        }

        return panel;
    }

    private void setupEventHandlers() {
       
    }

    private void addProcess() {
        String name = txtProcessName.getText().trim();
        String timeText = txtProcessTime.getText().trim();
        String priorityText = txtPriority.getText().trim();

   
        if (name.isEmpty()) {
            showError("El nombre del proceso no puede estar vacío");
            return;
        }

        if (processManager.processExists(name)) {
            showError("Ya existe un proceso con ese nombre");
            return;
        }

        if (priorityText.isEmpty()) {
            showError("Debe ingresar una prioridad");
            return;
        }

        try {
            long time = parseTimeField(); 
            if (time <= 0) {
                showError("El tiempo debe ser mayor a 0");
                return;
            }

            int priority = Integer.parseInt(priorityText);
            if (priority <= 0) {
                showError("La prioridad debe ser mayor a 0");
                return;
            }

            Status status = cmbStatus.getSelectedIndex() == 0 ? 
                Status.NO_BLOQUEADO : Status.BLOQUEADO;
            
            Status suspended = cmbSuspended.getSelectedIndex() == 0 ? 
                Status.NO_SUSPENDIDO : Status.SUSPENDIDO;
            
            Status resumed = cmbResumed.getSelectedIndex() == 0 ? 
                Status.NO_REANUDADO : Status.REANUDADO;
            
            Status destroyed = cmbDestroyed.getSelectedIndex() == 0 ? 
                Status.NO_DESTRUIDO : Status.DESTRUIDO;

           
            if (resumed == Status.REANUDADO && suspended == Status.NO_SUSPENDIDO) {
                showError("Un proceso no puede ser reanudado sin estar suspendido");
                return;
            }

            
            List<String> selectedProcesses = getSelectedReferencedProcesses();
            String referencedProcesses = selectedProcesses.isEmpty() ? null : String.join(",", selectedProcesses);

            int finalPriority = priority;
            
        
            String priorityChangeText = txtPriorityChange.getText().trim();
            if (!priorityChangeText.isEmpty()) {
                try {
                    int newPriority = Integer.parseInt(priorityChangeText);
                    if (newPriority != priority && newPriority > 0) {
                        finalPriority = newPriority;
                    }
                } catch (NumberFormatException ex) {
                    showError("Ingrese un valor válido para el cambio de prioridad");
                    return;
                }
            }

          
            processManager.addProcess(name, time, status, priority, finalPriority, 
                                    suspended, resumed, destroyed, referencedProcesses);
            
            updateProcessTable();
            updateReferencedProcessCheckboxes();
            clearForm();
            showInfo("Proceso agregado exitosamente");

        } catch (NumberFormatException ex) {
            showError("Ingrese valores numéricos válidos");
        }
    }

    private void editProcess() {
        int selectedRow = processTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Seleccione un proceso para modificar");
            return;
        }

        String oldName = (String) processTableModel.getValueAt(selectedRow, 0);
        model.Process selectedProcess = null;
        
        for (model.Process p : processManager.getInitialProcesses()) {
            if (p.getName().equals(oldName)) {
                selectedProcess = p;
                break;
            }
        }

        if (selectedProcess == null) return;

       
        JDialog editDialog = createEditDialog(selectedProcess, selectedRow);
        editDialog.setVisible(true);
    }

   
    private JDialog createEditDialog(model.Process process, int selectedRow) {
        JDialog dialog = new JDialog(this, "Modificar Proceso", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 600);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); 

      
        JTextField txtEditName = new JTextField(process.getName(), 20); 
        txtEditName.setEditable(false);
        txtEditName.setBackground(Color.LIGHT_GRAY);

        
        JTextField txtEditTime = new JTextField(String.valueOf(process.getOriginalTime()), 20);
        
        
        JTextField txtEditPriority = new JTextField(String.valueOf(process.getFinalPriority()), 20);


        JComboBox<String> cmbEditStatus = new JComboBox<>(new String[]{"No Bloqueado", "Bloqueado"});
        cmbEditStatus.setSelectedIndex(process.isBlocked() ? 1 : 0);
        cmbEditStatus.setPreferredSize(new Dimension(200, 25)); 

        JComboBox<String> cmbEditSuspended = new JComboBox<>(new String[]{"No", "Si"});
        cmbEditSuspended.setSelectedIndex(process.isSuspended() ? 1 : 0);
        cmbEditSuspended.setPreferredSize(new Dimension(200, 25));

        JComboBox<String> cmbEditResumed = new JComboBox<>(new String[]{"No", "Si"});
        cmbEditResumed.setSelectedIndex(process.isResumed() ? 1 : 0);
        cmbEditResumed.setPreferredSize(new Dimension(200, 25));

        JComboBox<String> cmbEditDestroyed = new JComboBox<>(new String[]{"No", "Si"});
        cmbEditDestroyed.setSelectedIndex(process.isDestroyed() ? 1 : 0);
        cmbEditDestroyed.setPreferredSize(new Dimension(200, 25));

       
        JPanel editReferencedPanel = new JPanel();
        editReferencedPanel.setLayout(new BoxLayout(editReferencedPanel, BoxLayout.Y_AXIS));
        List<JCheckBox> editReferencedCheckboxes = new ArrayList<>();

      
        for (model.Process p : processManager.getInitialProcesses()) {
            if (!p.getName().equals(process.getName())) {
                JCheckBox checkBox = new JCheckBox(p.getName());
                checkBox.setFont(new Font("Arial", Font.PLAIN, 12));
                
                // Seleccionar si estA en las referencias actuales
                if (process.hasReference()) {
                    String[] currentRefs = process.getReferencedProcess().split(",");
                    for (String ref : currentRefs) {
                        if (ref.trim().equals(p.getName())) {
                            checkBox.setSelected(true);
                            break;
                        }
                    }
                }
                
                editReferencedCheckboxes.add(checkBox);
                editReferencedPanel.add(checkBox);
            }
        }

        JScrollPane editScrollPane = new JScrollPane(editReferencedPanel);
        editScrollPane.setPreferredSize(new Dimension(200, 80));
        editScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

   
        int row = 0;
        addDialogComponent(dialog, gbc, "Nombre:", txtEditName, row++);
        addDialogComponent(dialog, gbc, "Tiempo:", txtEditTime, row++);
        addDialogComponent(dialog, gbc, "Prioridad:", txtEditPriority, row++);
        addDialogComponent(dialog, gbc, "Estado:", cmbEditStatus, row++);
        addDialogComponent(dialog, gbc, "Suspendido:", cmbEditSuspended, row++);
        addDialogComponent(dialog, gbc, "Reanudado:", cmbEditResumed, row++);
        addDialogComponent(dialog, gbc, "Destruido:", cmbEditDestroyed, row++);
        
       
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST; 
        gbc.fill = GridBagConstraints.NONE;
        
        JLabel lblReference = new JLabel("Comunicacion:");
        lblReference.setPreferredSize(new Dimension(120, 25)); 
        dialog.add(lblReference, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dialog.add(editScrollPane, gbc);
        row++;

   
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnSave = new JButton("Guardar");
        JButton btnCancel = new JButton("Cancelar");
        
        Dimension buttonSize = new Dimension(100, 30);
        btnSave.setPreferredSize(buttonSize);
        btnCancel.setPreferredSize(buttonSize);

        btnSave.addActionListener(e -> {
            if (saveEditedProcess(dialog, process, selectedRow, txtEditTime, txtEditPriority, 
                                cmbEditStatus, cmbEditSuspended, 
                                cmbEditResumed, cmbEditDestroyed, editReferencedCheckboxes)) {
                dialog.dispose();
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10); 
        dialog.add(buttonPanel, gbc);

  
        dialog.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnSave.doClick(); 
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dialog.dispose(); 
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        dialog.setFocusable(true);
        dialog.requestFocus();

        return dialog;
    }

    private void addDialogComponent(JDialog dialog, GridBagConstraints gbc, String label, JComponent component, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST; 
        gbc.fill = GridBagConstraints.NONE;
        
        JLabel lblComponent = new JLabel(label);
        lblComponent.setPreferredSize(new Dimension(120, 25)); 
        dialog.add(lblComponent, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST; 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        dialog.add(component, gbc);
    }

  
    private boolean saveEditedProcess(JDialog dialog, model.Process originalProcess, int selectedRow,
                                JTextField txtTime, JTextField txtPriority,
                                JComboBox<String> cmbStatus, JComboBox<String> cmbSuspended,
                                JComboBox<String> cmbResumed, JComboBox<String> cmbDestroyed,
                                List<JCheckBox> editReferencedCheckboxes) {
        try {
            
            long newTime = parseTimeFieldForDialog(txtTime);
            if (newTime <= 0) {
                showError("El tiempo debe ser mayor a 0");
                return false;
            }

      
            int newPriority = Integer.parseInt(txtPriority.getText().trim());
            if (newPriority <= 0) {
                showError("La prioridad debe ser mayor a 0");
                return false;
            }

     
            Status newStatus = cmbStatus.getSelectedIndex() == 0 ? Status.NO_BLOQUEADO : Status.BLOQUEADO;
            Status newSuspended = cmbSuspended.getSelectedIndex() == 0 ? Status.NO_SUSPENDIDO : Status.SUSPENDIDO;
            Status newResumed = cmbResumed.getSelectedIndex() == 0 ? Status.NO_REANUDADO : Status.REANUDADO;
            Status newDestroyed = cmbDestroyed.getSelectedIndex() == 0 ? Status.NO_DESTRUIDO : Status.DESTRUIDO;

        
            if (newResumed == Status.REANUDADO && newSuspended == Status.NO_SUSPENDIDO) {
                showError("Un proceso no puede ser reanudado sin estar suspendido");
                return false;
            }

            
            List<String> selectedRefs = new ArrayList<>();
            for (JCheckBox checkBox : editReferencedCheckboxes) {
                if (checkBox.isSelected()) {
                    selectedRefs.add(checkBox.getText());
                }
            }
            String newReferences = selectedRefs.isEmpty() ? null : String.join(",", selectedRefs);

      
            processManager.editProcess(selectedRow, originalProcess.getName(), newTime, newStatus,
                                     newPriority, newSuspended, newResumed, newDestroyed, newReferences);
            
            updateProcessTable();
            updateReferencedProcessCheckboxes();
            showInfo("Proceso editado exitosamente");
            return true;

        } catch (NumberFormatException ex) {
            showError("Ingrese valores numéricos válidos");
            return false;
        }
    }

    private void deleteProcess() {
        int selectedRow = processTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Seleccione un proceso para eliminar");
            return;
        }

        String processName = (String) processTableModel.getValueAt(selectedRow, 0);
        
      
        if (isProcessBeingReferenced(processName)) {
            List<String> referencingProcesses = getProcessesReferencingThis(processName);
            String referencingList = String.join(", ", referencingProcesses);
            showError("No se puede eliminar el proceso '" + processName + "' porque está siendo comunicado por: " + referencingList + ".<br><br>Elimine primero las referencias o modifique los procesos que lo comunican.");
            return;
        }
        
        currentAction = "DELETE_PROCESS:" + processName;
        new CustomDialog(this, "¿Está seguro de que desea eliminar el proceso '" + processName + "'?", CustomDialog.CONFIRM_TYPE);
    }


    private boolean isProcessBeingReferenced(String processName) {
        for (model.Process p : processManager.getInitialProcesses()) {
            if (p.hasReference()) {
                String[] references = p.getReferencedProcess().split(",");
                for (String ref : references) {
                    if (ref.trim().equalsIgnoreCase(processName.trim())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

   
    private List<String> getProcessesReferencingThis(String processName) {
        List<String> referencingProcesses = new ArrayList<>();
        for (model.Process p : processManager.getInitialProcesses()) {
            if (p.hasReference()) {
                String[] references = p.getReferencedProcess().split(",");
                for (String ref : references) {
                    if (ref.trim().equalsIgnoreCase(processName.trim())) {
                        referencingProcesses.add(p.getName());
                        break; // Solo agregar el proceso una vez
                    }
                }
            }
        }
        return referencingProcesses;
    }

    private void runSimulation() {
        if (processManager.isEmpty()) {
            showError("No hay procesos para simular");
            return;
        }

        List<model.Process> priorityChangesBeforeSim = processManager.getProcessesWithPriorityChanges();
        
        for (model.Process p : priorityChangesBeforeSim) {
            System.out.println("- " + p.getName() + ": " + p.getInitialPriority() + " -> " + p.getFinalPriority());
        }

        processManager.runSimulation();
        
      
        List<Log> priorityLogs = processManager.getLogsByFilter(Filter.PRIORIDAD_CAMBIADA);
        
        
      
        for (int i = 0; i < tableNames.length; i++) {
            updateResultTable(i);
        }
        
        cardLayout.show(resultsPanel, tableNames[0]); 
        showInfo("Simulación ejecutada exitosamente." );
    }

    private void updateProcessTable() {
        processTableModel.setRowCount(0);
        
       
        for (model.Process p : processManager.getInitialProcesses()) {
            String formattedTime = numberFormatter.format(p.getOriginalTime());
            String reference = p.hasReference() ? p.getReferencedProcess() : "Ninguno";
            
          
            String priorityDisplay = String.valueOf(p.getInitialPriority());
            
            processTableModel.addRow(new Object[]{
                p.getName(),
                formattedTime,
                priorityDisplay,  
                p.getStatusString(),
                p.getSuspendedString(),
                p.getResumedString(),
                p.getDestroyedString(),
                reference
            });
        }
    }

    private void updateResultTable(int tableIndex) {
        if (tableIndex == 0) {
           
            resultTableModels[0].setRowCount(0);
            
       
            for (model.Process p : processManager.getInitialProcesses()) {
                String formattedTime = numberFormatter.format(p.getOriginalTime());
                String reference = p.hasReference() ? p.getReferencedProcess() : "Ninguno";
                
                resultTableModels[0].addRow(new Object[]{
                    p.getName(),                    
                    formattedTime,                  
                    p.getFinalPriority(),          
                    p.getStatusString(),           
                    p.getSuspendedString(),       
                    p.getResumedString(),          
                    p.getDestroyedString(),        
                    reference,                      
                    0                             
                });
            }
        } else if (tableIndex < 8) {
          
            List<Log> logs = processManager.getLogsByFilter(filters[tableIndex]);
            resultTableModels[tableIndex].setRowCount(0);
            for (Log log : logs) {
                String formattedTime = numberFormatter.format(log.getRemainingTime());
                
       
                model.Process originalProcess = null;
                for (model.Process p : processManager.getInitialProcesses()) {
                    if (p.getName().equals(log.getProcessName())) {
                        originalProcess = p;
                        break;
                    }
                }
                
                String reference = "Ninguno";
                String suspended = "No";
                String resumed = "No";
                String destroyed = "No";
                
                if (originalProcess != null) {
                    reference = originalProcess.hasReference() ? originalProcess.getReferencedProcess() : "Ninguno";
                    suspended = originalProcess.getSuspendedString();
                    resumed = originalProcess.getResumedString();
                    destroyed = originalProcess.getDestroyedString();
                }
                
                resultTableModels[tableIndex].addRow(new Object[]{
                    log.getProcessName(),         
                    formattedTime,                
                    log.getPriority(),            
                    log.getStatusString(),        
                    suspended,                    
                    resumed,                       
                    destroyed,                    
                    reference,                    
                    log.getCycleCount()          
                });
            }
        } else {
           
            updateSpecialReportTable(tableIndex);
        }
    }

    private void updateSpecialReportTable(int tableIndex) {
        resultTableModels[tableIndex].setRowCount(0);
        
        switch (tableIndex) {
            case 8: 
                List<model.Process> priorityChanges = processManager.getProcessesWithPriorityChanges();
                for (model.Process p : priorityChanges) {
                    resultTableModels[tableIndex].addRow(new Object[]{
                        p.getName(),
                        "Prioridad: " + p.getInitialPriority() + " → " + p.getFinalPriority()
                    });
                }
                break;
                
            case 9: 
                List<model.Process> suspended = processManager.getSuspendedProcesses();
                if (suspended.isEmpty()) {
                    resultTableModels[tableIndex].addRow(new Object[]{
                        "", ""
                    });
                } else {
                    
                    if (resultTableModels[tableIndex].getColumnCount() == 2) {
                        String[] fullColumns = {"Proceso", "Tiempo Restante", "Prioridad", "Estado", "Suspendido", "Reanudado", "Destruido", "Comunicacion", "Información"};
                        resultTableModels[tableIndex].setColumnIdentifiers(fullColumns);
                    }
                    
                    for (model.Process p : suspended) {
                        
                        String formattedTime = numberFormatter.format(p.getRemainingTime());
                        String reference = p.hasReference() ? p.getReferencedProcess() : "Ninguno";
                        
                        resultTableModels[tableIndex].addRow(new Object[]{
                            p.getName(),                    
                            formattedTime,                  
                            p.getInitialPriority(),        
                            p.getStatusString(),           
                            p.getSuspendedString(),        
                            p.getResumedString(),          
                            p.getDestroyedString(),        
                            reference,                    
                            "Ciclo: " + p.getCycleCount()          
                        });
                    }
                }
                break;
                
            case 10: 
                List<model.Process> resumed = processManager.getResumedProcesses();
                if (resumed.isEmpty()) {
                    resultTableModels[tableIndex].addRow(new Object[]{
                        "", ""
                    });
                } else {
                   
                    if (resultTableModels[tableIndex].getColumnCount() == 2) {
                        String[] fullColumns = {"Proceso", "Tiempo Restante", "Prioridad", "Estado", "Suspendido", "Reanudado", "Destruido", "Comunicacion", "Información"};
                        resultTableModels[tableIndex].setColumnIdentifiers(fullColumns);
                    }
                    
                    for (model.Process p : resumed) {
                       
                        String formattedTime = numberFormatter.format(p.getRemainingTime());
                        String reference = p.hasReference() ? p.getReferencedProcess() : "Ninguno";
                        
                        resultTableModels[tableIndex].addRow(new Object[]{
                            p.getName(),                  
                            formattedTime,                
                            p.getInitialPriority(),      
                            p.getStatusString(),           
                            p.getSuspendedString(),       
                            p.getResumedString(),          
                            p.getDestroyedString(),        
                            reference,                    
                            "Ciclo: " + p.getCycleCount()           
                        });
                    }
                }
                break;

                
            case 11:
                List<model.Process> destroyed = processManager.getDestroyedProcesses();
                if (destroyed.isEmpty()) {
                    resultTableModels[tableIndex].addRow(new Object[]{
                        "", ""
                    });
                } else {
                  
                    if (resultTableModels[tableIndex].getColumnCount() == 2) {
                        String[] fullColumns = {"Proceso", "Tiempo Original", "Prioridad", "Estado", "Suspendido", "Reanudado", "Destruido", "Comunicacion", "Información"};
                        resultTableModels[tableIndex].setColumnIdentifiers(fullColumns);
                    }
                    
                    for (model.Process p : destroyed) {
                        String formattedTime = numberFormatter.format(p.getOriginalTime());
                        String reference = p.hasReference() ? p.getReferencedProcess() : "Ninguno";
                        
                        resultTableModels[tableIndex].addRow(new Object[]{
                            p.getName(),                   
                            formattedTime,                  
                            p.getFinalPriority(),          
                            p.getStatusString(),           
                            p.getSuspendedString(),        
                            p.getResumedString(),         
                            p.getDestroyedString(),        
                            reference,                      
                            "Proceso destruido"          
                        });
                    }
                }
                break;
                
            case 12:
                List<String> relations = processManager.getProcessRelationsReport();
                if (relations.isEmpty()) {
                    resultTableModels[tableIndex].addRow(new Object[]{
                        "Sin Comunicacion", "No hay procesos con referencias"
                    });
                } else {
                    
                    for (String relation : relations) {
                        String[] parts = relation.split(" -> ");
                        if (parts.length == 2) {
                            String fromProcess = parts[0].trim();
                            String toProcess = parts[1].trim();
                            
                           
                            resultTableModels[tableIndex].addRow(new Object[]{
                                fromProcess, 
                                "Comunica con: " + toProcess
                            });
                            
                            
                        }
                    }
                }
                break;
        }
    }

    private void clearAll() {
        currentAction = "CLEAR_ALL";
        new CustomDialog(this, "¿Está seguro de que desea eliminar todos los procesos?", CustomDialog.CONFIRM_TYPE);
    }

    
    private void clearForm() {
        txtProcessName.setText("");
        txtProcessTime.setText("");
        txtPriority.setText("");
        txtPriorityChange.setText("");
        cmbStatus.setSelectedIndex(0);
        cmbSuspended.setSelectedIndex(0);
        cmbResumed.setSelectedIndex(0);
        cmbDestroyed.setSelectedIndex(0);
        clearReferencedProcessesSelection();  // CAMBIO: Limpiar selección de checkboxes
    }

    private void openUserManual() {
        try {
            File manualFile = new File("Manual_Usuario.pdf");
            
            if (!manualFile.exists()) {
                showError("No se encontró el archivo del manual de usuario.<br>" +
                         "Asegúrese de que el archivo 'Manual_Usuario.pdf'<br>" +
                         "esté en la misma carpeta que el programa.");
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(manualFile);
                } else {
                    showError("Su sistema no permite abrir archivos PDF automáticamente.<br>" +
                             "Por favor, abra manualmente el archivo:<br>" +
                             "Manual_Usuario_Simulador_Procesos.pdf");
                }
            } else {
                showError("Su sistema no permite abrir archivos automáticamente.<br>" +
                         "Por favor, abra manualmente el archivo:<br>" +
                         manualFile.getAbsolutePath());
            }
            
        } catch (IOException ex) {
            showError("Error al abrir el manual de usuario:<br>" + ex.getMessage());
        } catch (Exception ex) {
            showError("Error inesperado al abrir el manual:<br>" + ex.getMessage());
        }
    }

    private void showError(String message) {
        new CustomDialog(this, message, CustomDialog.WARNING_TYPE);
    }

    private void showInfo(String message) {
        new CustomDialog(this, message, CustomDialog.INFO_TYPE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        switch(command) {
            case Constants.CLOSE_WARNING:
            case Constants.CLOSE_INFO:
                ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
                break;
                
            case Constants.CONFIRM_YES:
                handleConfirmYes();
                ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
                break;
                
            case Constants.CONFIRM_NO:
                ((JDialog)((JButton)e.getSource()).getTopLevelAncestor()).dispose();
                break;
        }
    }
    
    private void handleConfirmYes() {
        if (currentAction != null) {
            if (currentAction.startsWith("DELETE_PROCESS:")) {
                String processName = currentAction.substring("DELETE_PROCESS:".length());
                processManager.removeProcess(processName);
                updateProcessTable();
                updateReferencedProcessCheckboxes();  // CAMBIO: Usar nuevo método
                showInfo("Proceso eliminado");
            } else if (currentAction.equals("CLEAR_ALL")) {
                processManager.clearAll();
                updateProcessTable();
                updateReferencedProcessCheckboxes();  // CAMBIO: Usar nuevo método
                
                for (DefaultTableModel model : resultTableModels) {
                    model.setRowCount(0);
                }
                
                clearForm();
                showInfo("Todos los datos han sido eliminados");
            }
            currentAction = null;
        }
    }
}