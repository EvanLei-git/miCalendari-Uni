package gr.hua.dit.oop2;

import gr.hua.dit.oop2.calendar.TimeService;
import gr.hua.dit.oop2.calendar.TimeTeller;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.validate.ValidationException;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static gr.hua.dit.oop2.CalendarApp.loadCalendarFromFile;
public class UIApp {
    private static CalendarManager calendarManager;

    private static JComboBox<String> typeComboBox;
    private static JTextField titleField;
    private static JTextField descriptionField;

    private static JDatePickerImpl startDatePicker;
    private static JDatePickerImpl endDatePicker;
    private static JSpinner startTimeSpinner;
    private static JSpinner endTimeSpinner;
    private static JTextField startDateField;
    private static JTextField endDateField;
    private static JTable table;

    private static JLabel timeLabel;
    private static JLabel upcomingEventLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> GUI());
    }

    private static void GUI() {
        JFrame frame = new JFrame("MiCalendari");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // First panel
        JPanel buttonPanel = new JPanel();
        JButton loadButton = new JButton("LOAD");
        JButton saveButton = new JButton("SAVE");
        JLabel startTimeDisplay = new JLabel();

        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (selectedFile != null) {                       
                        calendarManager = loadCalendarFromFile(selectedFile.getAbsolutePath());
                        populateTableWithEvents(calendarManager.getAllElements());
                    }
                }
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = JOptionPane.showInputDialog(frame, "Enter file name:", "Save Calendar", JOptionPane.PLAIN_MESSAGE);
                if (fileName != null && !fileName.trim().isEmpty()) {
                    fileName = fileName.trim();
                    if (!fileName.endsWith(".ics")) {
                        fileName += ".ics";
                    }
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save as");
                    fileChooser.setFileFilter(new FileNameExtensionFilter("iCalendar files (*.ics)", "ics"));
                    fileChooser.setSelectedFile(new File(fileName));

                    int userSelection = fileChooser.showSaveDialog(frame);

                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToSave = fileChooser.getSelectedFile();
                        saveCalendarToFile(fileToSave.getAbsolutePath());
                    }
                }
            }
        });

        String[] options = {"All","Day", "Week", "Month", "Past Day", "Past Week", "Past Month", "Due", "To Do"};
        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFilter = (String) comboBox.getSelectedItem();
                updateTableBasedOnFilter(selectedFilter);
            }
        });
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(comboBox);

        new Timer(1000, e -> {
            TimeTeller timeTeller = TimeService.getTeller();
            LocalDateTime now = timeTeller.now();
            startTimeDisplay.setText("TimeTeller: " + now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        }).start();

        buttonPanel.add(startTimeDisplay);

        // Second panel
        JPanel tablePanel = new JPanel(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 7 ? Boolean.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                // Preventing editing of the "No#" and "Type" columns...
                return column != 0 && column != 1;
            }
        };
                // Returning the appropriate class for each column...
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                // Validating rows and columns...
                if (row >= 0 && row < table.getRowCount() && column >= 0 && column < table.getColumnCount()) {
                    updateCalendarComponent(row, column);
                }
            }
        });

        model.addColumn("No#");
        model.addColumn("Type");
        model.addColumn("Title");
        model.addColumn("Description");
        model.addColumn("Start Date");
        model.addColumn("End Date/Deadline");
        model.addColumn("Duration");
        model.addColumn("Completed");

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Setting a cell renderer for the "End Date/Deadline" column...
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                String type = (String) table.getModel().getValueAt(row, 1);
                component.setBackground(table.getBackground());

                return component;
            }
        });
        // Setting cell renderer for the "Completed" column...
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Determining the type for the current row...
                String type = (String) table.getModel().getValueAt(row, 1);

                // If the type is "Task", a checkbox is used!
                if ("Task".equals(type)) {
                    JCheckBox checkBox = new JCheckBox();
                    checkBox.setHorizontalAlignment(JLabel.CENTER);
                    checkBox.setSelected((Boolean)value);
                    return checkBox;
                } else {
                    // For non-task types, returning a label with a "-"...
                    JLabel label = new JLabel("-");
                    label.setHorizontalAlignment(JLabel.CENTER);
                    return label;
                }
            }
        });


        JPanel statusPanel = new JPanel();
        upcomingEventLabel = new JLabel("No upcoming events or tasks."); 
        statusPanel.add(upcomingEventLabel);

        // Using a Timer to update the upcomingEventLabel every second!
        new Timer(1000, e -> updateUpcomingEventLabel()).start();

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Third panel (it is empty!)
        JPanel emptyPanel = new JPanel();

        // Fourth panel
        JPanel inputPanel = new JPanel();
        JLabel typeLabel = new JLabel("Type:");
        typeComboBox = new JComboBox<>(new String[]{"Event", "Task", "Appointment"});

        JLabel titleLabel = new JLabel("Title:");
        titleField = new JTextField(10);



        JLabel descriptionLabel = new JLabel("Description:");
        descriptionField = new JTextField(10);

        JLabel startDateLabel = new JLabel("Start Date:");
        startDateField = new JTextField(10);

        JLabel endDateLabel = new JLabel("End Date/Deadline:");
        endDateField = new JTextField(10);



        // Adding ItemListener to typeComboBox...
        typeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) { // Checking if the event is due to an item being selected.
                    String selectedType = (String) e.getItem();
                    if ("Event".equals(selectedType)) {
                        endDatePicker.getComponent(1).setEnabled(false); 

                        endDatePicker.setEnabled(false); 
                        endTimeSpinner.setEnabled(false);
                    } else {
                        endDatePicker.getComponent(1).setEnabled(true); 

                        endDatePicker.setEnabled(true); 
                        endTimeSpinner.setEnabled(true);
                    }

                    if ("Task".equals(selectedType)) {
                        startDatePicker.getComponent(1).setEnabled(false); 
                        startDatePicker.setEnabled(false); 
                        startTimeSpinner.setEnabled(false);
                    } else {
                        startDatePicker.getComponent(1).setEnabled(true); 

                        startDatePicker.setEnabled(true); 
                        startTimeSpinner.setEnabled(true);
                    }
                }
            }
        });


        JButton insertButton = new JButton("Insert");
        insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertData();
            }
        });

        // Initializing JDatePicker for Start Date..
        UtilDateModel model1 = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel1 = new JDatePanelImpl(model1, p);
        startDatePicker = new JDatePickerImpl(datePanel1, new DateLabelFormatter());

        // Initializing JSpinner for Start Time...
        SpinnerDateModel timeModel1 = new SpinnerDateModel();
        startTimeSpinner = new JSpinner(timeModel1);
        JSpinner.DateEditor timeEditor1 = new JSpinner.DateEditor(startTimeSpinner, "HH:mm:ss");
        startTimeSpinner.setEditor(timeEditor1);

        // Initializing JDatePicker for Start Date...
        UtilDateModel model2 = new UtilDateModel();
        Properties p2 = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel2 = new JDatePanelImpl(model2, p2);
        endDatePicker = new JDatePickerImpl(datePanel2, new DateLabelFormatter());

        // Initializing JSpinner for End Time...
        SpinnerDateModel timeModel2 = new SpinnerDateModel();
        endTimeSpinner = new JSpinner(timeModel2);
        JSpinner.DateEditor timeEditor2 = new JSpinner.DateEditor(endTimeSpinner, "HH:mm:ss");
        endTimeSpinner.setEditor(timeEditor2);

        inputPanel.add(typeLabel);
        inputPanel.add(typeComboBox);
        inputPanel.add(titleLabel);
        inputPanel.add(titleField);
        inputPanel.add(descriptionLabel);
        inputPanel.add(descriptionField);
        inputPanel.add(startDateLabel);
        inputPanel.add(startDatePicker);

        inputPanel.add(startTimeSpinner);
        

        inputPanel.add(endDateLabel);
        inputPanel.add(endDatePicker);
        inputPanel.add(endTimeSpinner);
        

        inputPanel.add(insertButton);

        
        endDatePicker.getComponent(1).setEnabled(false); 

        endDatePicker.setEnabled(false); 
        endTimeSpinner.setEnabled(false);

        // Main panel
        JPanel mainPanel = new JPanel(new GridLayout(5, 1));
        mainPanel.add(buttonPanel);
        mainPanel.add(tablePanel);
        mainPanel.add(emptyPanel);
        mainPanel.add(inputPanel);
        mainPanel.add(statusPanel); 

        frame.add(mainPanel);

        frame.setVisible(true);
    }


    private static void updateUpcomingEventLabel() {
        if (calendarManager == null) {
            upcomingEventLabel.setText("No calendar data loaded.");
            return;
        }

        CalendarComponent mostRecent = calendarManager.getMostRecentEvent();
        if (mostRecent != null) {
            // Formatting the display of the most recent event..
            String displayText = formatForDisplay(mostRecent);
            upcomingEventLabel.setText("Upcoming: "+displayText);
        } else {
            upcomingEventLabel.setText("No upcoming events");
        }
    }
    private static String formatForDisplay(CalendarComponent component) {
        if (component == null) {
            return "No upcoming events or tasks.";
        }

        StringBuilder display = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        if (component instanceof VEvent) {
            VEvent event = (VEvent) component;
            display.append("Event: ")
                    .append(event.getSummary() != null ? event.getSummary().getValue() : "N/A")
                    .append(", Start: ")
                    .append(event.getStartDate() != null ? dateFormat.format(event.getStartDate().getDate()) : "N/A");

            if (event.getEndDate() != null) {
                display.append(", End: ").append(dateFormat.format(event.getEndDate().getDate()));
            }
        } else if (component instanceof VToDo) {
            VToDo task = (VToDo) component;
            display.append("Task: ")
                    .append(task.getSummary() != null ? task.getSummary().getValue() : "N/A")
                    .append(", Due: ")
                    .append(task.getDue() != null ? dateFormat.format(task.getDue().getDate()) : "N/A");
        }

        return display.toString();
    }
    private static void updateCalendarComponent(int row, int column) {
        if (calendarManager == null) return;
        CalendarComponent component = calendarManager.getAllElements().get(row);

        
        Object newValue = table.getValueAt(row, column);

        if (component instanceof VEvent) {
            VEvent event = (VEvent) component;
            updateEvent(event, column, newValue.toString());
            calendarManager.editEvent(row, event); 
        } else if (component instanceof VToDo) {
            VToDo task = (VToDo) component;
            updateTask(task, column, newValue.toString());
            calendarManager.editTask(row, task); 
        }
    }
    private static void saveCalendarToFile(String filePath) {
        if (calendarManager == null) return;

        net.fortuna.ical4j.model.Calendar icalCalendar = new net.fortuna.ical4j.model.Calendar();
        icalCalendar.getProperties().add(new ProdId("-//MiCalendari//iCal4j 1.0//EN"));
        icalCalendar.getProperties().add(Version.VERSION_2_0);
        icalCalendar.getProperties().add(CalScale.GREGORIAN);

        // Adding events to the calendar...
        for (VEvent event : calendarManager.getEvents()) {
            icalCalendar.getComponents().add(event);
        }

        // Adding tasks to the calendar.
        for (VToDo task : calendarManager.getTasks()) {
            icalCalendar.getComponents().add(task);
        }

        // Writing calendar to file...
        try (FileOutputStream fout = new FileOutputStream(filePath)) {
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(icalCalendar, fout);
            JOptionPane.showMessageDialog(null, "Calendar saved successfully to " + filePath);
        } catch (IOException | ValidationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving calendar: " + e.getMessage());
        }
    }

    private static void updateEvent(VEvent event, int column, String newValue) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        switch (column) {
            case 2: // Title
                event.getProperties().remove(event.getSummary());
                event.getProperties().add(new Summary(newValue));
                break;
            case 3: // Description
                event.getProperties().remove(event.getDescription());
                event.getProperties().add(new Description(newValue));
                break;
            case 4: // Start Date
                Date startDate = null;
                try {
                    startDate = sdf.parse(newValue);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                DtStart dtStart = new DtStart(new DateTime(startDate));
                event.getProperties().remove(event.getStartDate());
                event.getProperties().add(dtStart);
                break;
            case 5: // End Date
                Date endDate = null;
                try {
                    endDate = sdf.parse(newValue);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                DtEnd dtEnd = new DtEnd(new DateTime(endDate));
                event.getProperties().remove(event.getEndDate());
                event.getProperties().add(dtEnd);
                break;
        }
    }


    private static void updateTask(VToDo task, int column, String newValue) {
        switch (column) {
            case 2: // Title
                task.getProperties().remove(task.getSummary());
                task.getProperties().add(new Summary(newValue));
                break;
            case 3: // Description
                task.getProperties().remove(task.getDescription());
                task.getProperties().add(new Description(newValue));
                break;
            case 5: // Due Date
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                Date dueDate = null;
                try {
                    dueDate = sdf.parse(newValue);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                Due due = new Due(new DateTime(dueDate));
                task.getProperties().remove(task.getDue());
                task.getProperties().add(due);
                break;
            case 7: // Completed
                Status currentStatus = task.getStatus();
                boolean isCurrentlyCompleted = currentStatus != null && currentStatus.equals(Status.VTODO_COMPLETED);

                if ("true".equalsIgnoreCase(newValue) && !isCurrentlyCompleted) {
                    // Updating status to completed if it's currently not completed...
                    task.getProperties().remove(currentStatus);
                    task.getProperties().add(Status.VTODO_COMPLETED);
                } else if ("false".equalsIgnoreCase(newValue) && isCurrentlyCompleted) {
                    // Updating status to needs action if it's currently completed...
                    task.getProperties().remove(currentStatus);
                    task.getProperties().add(Status.VTODO_NEEDS_ACTION);
                }
                break;            
        }
    }




    public static class DateLabelFormatter extends AbstractFormatter {
        private String datePattern = "yyyy-MM-dd";
        private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parseObject(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }

    private static void populateTableWithEvents(List<CalendarComponent> events) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); 

        int rowNumber = 1; // Initializing a counter for row numbering...

        for (CalendarComponent component : events) {
            String type;
            String title = "";
            String description = "";
            String startDate;
            String endDate = "";
            String duration = "";
            boolean completed = false;

            if (component instanceof VEvent) {
                VEvent event = (VEvent) component;

                if (event.getSummary() != null) {
                    title = event.getSummary().getValue();
                }
                if (event.getDescription() != null) {
                    description = event.getDescription().getValue();
                }
                startDate = formatDate(event.getStartDate().getDate());

                if (event.getEndDate() != null) {
                    endDate = formatDate(event.getEndDate().getDate());
                    duration = calculateDuration(event.getStartDate().getDate(), event.getEndDate().getDate());
                }

                // Identifying if it's an event or appointment!
                if (endDate.equals(startDate)) {
                    type = "Event";
                } else {
                    type = "Appointment";
                }
            } else if (component instanceof VToDo) {
                VToDo task = (VToDo) component;
                type = "Task";
                if (task.getSummary() != null) {
                    title = task.getSummary().getValue();
                }
                if (task.getDescription() != null) {
                    description = task.getDescription().getValue();
                }
                startDate = ""; 
                if (task.getDue() != null) {
                    endDate = formatDate(task.getDue().getDate());
                }
                completed = task.getStatus() != null && task.getStatus().equals(Status.VTODO_COMPLETED);
            } else {
                continue; // Skips if it's not an event or task!
            }

            model.addRow(new Object[]{rowNumber++, type, title, description, startDate, endDate, duration, completed});
        }
        table.repaint();
    }

    private static String calculateDuration(java.util.Date startDate, java.util.Date endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }
        long durationInMillis = endDate.getTime() - startDate.getTime();
        long seconds = (durationInMillis / 1000) % 60;
        long minutes = (durationInMillis / (1000 * 60)) % 60;
        long hours = durationInMillis / (1000 * 60 * 60);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static String formatDate(java.util.Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sdf.format(date);
    }
    private static void updateTableBasedOnFilter(String filter) {
        if (calendarManager == null) {
            return;
        }
        System.out.println("Updating table for filter: " + filter); // Debugging line!

        List<CalendarComponent> events;
        switch (filter) {
            case "All":
                events = calendarManager.getAllElements();
                break;
            case "Day":
                events = calendarManager.printDay();
                break;
            case "Week":
                events = calendarManager.printWeek();
                break;
            case "Month":
                events = calendarManager.printMonth();
                break;
            case "Past Day":
                events = calendarManager.printPastDay();
                break;
            case "Past Week":
                events = calendarManager.printPastWeek();
                break;
            case "Past Month":
                events = calendarManager.printPastMonth();
                break;
            case "Due":
                events = calendarManager.printPastDeadline();
                break;
            case "To Do":
                events = calendarManager.printNotCompleted();
                System.out.println(calendarManager.printNotCompleted());

                break;
            default:
                events = calendarManager.getAllElements();
                break;
        }
        populateTableWithEvents(events);
    }
    private static void insertData() {
        String type = (String) typeComboBox.getSelectedItem();
        String title = titleField.getText();
        String description = descriptionField.getText();

        // Getting the date and time from the date pickers and time spinners...
        java.util.Date startDate = (java.util.Date) startDatePicker.getModel().getValue();
        java.util.Date startTime = (java.util.Date) startTimeSpinner.getValue();
        java.util.Date endDate = (java.util.Date) endDatePicker.getModel().getValue();
        java.util.Date endTime = (java.util.Date) endTimeSpinner.getValue();

        // Combining date and time for start and end...
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        if (startDate != null && startTime != null) {
            startCal.setTime(startDate);
            startCal.set(Calendar.HOUR_OF_DAY, startTime.getHours());
            startCal.set(Calendar.MINUTE, startTime.getMinutes());
            startCal.set(Calendar.SECOND, startTime.getSeconds());
        }
        if (endDate != null && endTime != null) {
            endCal.setTime(endDate);
            endCal.set(Calendar.HOUR_OF_DAY, endTime.getHours());
            endCal.set(Calendar.MINUTE, endTime.getMinutes());
            endCal.set(Calendar.SECOND, endTime.getSeconds());
        }

        // Formatting the date-time for display!
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String formattedStartDate = (startDate != null && startTime != null) ? sdf.format(startCal.getTime()) : "";
        String formattedEndDate = (endDate != null && endTime != null) ? sdf.format(endCal.getTime()) : "";

        // Calculating duration for Appointments!
        String duration = "";
        if ("Appointment".equals(type) && startDate != null && endDate != null) {
            long durationMillis = endCal.getTimeInMillis() - startCal.getTimeInMillis();
            duration = String.valueOf(durationMillis / 60000) + " mins"; // Converting milliseconds to minutes!
        }

        boolean completed = false; 

        // Inserting data into the table...
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addRow(new Object[]{model.getRowCount() + 1, type, title, description, formattedStartDate, formattedEndDate, duration, completed});
        if ("Event".equals(type) || "Appointment".equals(type)) {
            VEvent newEvent = createNewEvent(formattedStartDate, formattedEndDate, title, description);
            calendarManager.addEvent(newEvent);
        } else if ("Task".equals(type)) {
            VToDo newTask = createNewTask(formattedEndDate, title, description, completed);
            calendarManager.addTask(newTask);
        }
        // Resetting fields after insertion...
        titleField.setText("");
        descriptionField.setText("");
        startDatePicker.getModel().setValue(null);
        endDatePicker.getModel().setValue(null);
        CalendarComponent newComponent;

        if ("Event".equals(type)) {
            endDatePicker.getComponent(1).setEnabled(false); 

            endDatePicker.setEnabled(false); 
            endTimeSpinner.setEnabled(false);
            newComponent = new VEvent();

        } else {
            endDatePicker.getComponent(1).setEnabled(true); 

            endDatePicker.setEnabled(true); 
            endTimeSpinner.setEnabled(true);
            newComponent = new VToDo();

        }

        if ("Task".equals(type)) {
            startDatePicker.getComponent(1).setEnabled(false); 
            startDatePicker.setEnabled(false); 
            startTimeSpinner.setEnabled(false);
        } else {
            startDatePicker.getComponent(1).setEnabled(true); 

            startDatePicker.setEnabled(true); 
            startTimeSpinner.setEnabled(true);
        }
    }
    private static VEvent createNewEvent(String start, String end, String title, String description) {
        try {           
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date startDate = sdf.parse(start);
            DateTime startDateTime = new DateTime(startDate);

            // Creating a new event..
            VEvent newEvent = new VEvent(startDateTime, title);

            // If it's an appointment, it will have an end date.
            if (end != null && !end.isEmpty()) {
                Date endDate = sdf.parse(end);
                DateTime endDateTime = new DateTime(endDate);
                newEvent.getProperties().add(new DtEnd(endDateTime));
            }
           
            if (description != null && !description.isEmpty()) {
                newEvent.getProperties().add(new Description(description));
            }

            return newEvent;
        } catch (ParseException e) {
            e.printStackTrace();
            return null; 
        }
    }
    private static VToDo createNewTask(String due, String title, String description, boolean completed) {
        try {
            // Parsing the due date
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Date dueDate = sdf.parse(due);
            DateTime dueDateTime = new DateTime(dueDate);

            // Creating a new task...
            VToDo newTask = new VToDo();
            newTask.getProperties().add(new Due(dueDateTime));
            newTask.getProperties().add(new Summary(title));

            // Adding description if available!
            if (description != null && !description.isEmpty()) {
                newTask.getProperties().add(new Description(description));
            }

            // Setting status based on completion...
            Status status = completed ? Status.VTODO_COMPLETED : Status.VTODO_NEEDS_ACTION;
            newTask.getProperties().add(status);

            return newTask;
        } catch (ParseException e) {
            e.printStackTrace();
            return null; 
        }
        
    }

}