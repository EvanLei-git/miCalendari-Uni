package gr.hua.dit.oop2;

//Built This mvn by following this Video: https://www.youtube.com/watch?v=zlHXH6maOR0
//How to run mvn with arguments https://stackoverflow.com/a/10108780


import gr.hua.dit.oop2.calendar.TimeService;
import gr.hua.dit.oop2.calendar.TimeTeller;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;


public class CalendarApp {
    public static void main(String[] args) {
        // Check if at least two arguments are provided (command and file name)
        if (args.length >= 2) {
            String command = args[0]; // The action to perform (e.g., "day", "week", "month")
            String fileName = args[1]; // The name of the .ics file

            // Check if the file name ends with .ics (case insensitive)
            if (!fileName.toLowerCase().endsWith(".ics")) {
                System.out.println("Error: Not an .ics file");
                return;
            }

            // Load calendar data from the specified file
            CalendarManager calendarManager = loadCalendarFromFile(fileName);

            //=====TESTING TO SHOW THE DATE SO WE KNOW WHAT TIME IT IS======//
            // Get current date and time based on the TimeTeller and print it
            TimeTeller timeTeller = TimeService.getTeller();
            LocalDateTime now = timeTeller.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            System.out.println("\nCurrent date and time: " + now.format(dateTimeFormatter)+ '\n');


            // Execute the command based on the first argument
            switch (command.toLowerCase()) {
                case "day":
                    calendarManager.printDay();
                    break;
                case "week":
                    calendarManager.printWeek();
                    break;
                case "month":
                    calendarManager.printMonth();
                    break;
                case "pastday":
                    calendarManager.printPastDay();
                    break;
                case "pastweek":
                    calendarManager.printPastWeek();
                    break;
                case "pastmonth":
                    calendarManager.printPastMonth();
                    break;
                case "todo":
                    calendarManager.printNotCompleted();
                    break;
                case "due":
                    calendarManager.printPastDeadline();
                    break;
                default:
                    System.out.println("Invalid command.");
                    break;
            }

            // TEST: save changes to the calendar file
            // saveCalendarToFile(calendarManager, fileName);
        }
        // If only one argument is provideD and its an .ics file
        else if (args.length == 1 && args[0].toLowerCase().endsWith(".ics")) {
            String fileName = args[0];

            // Check if the specified file exists and is a regular file
            File file = new File(fileName);
            if (!file.exists() || !file.isFile()) {
                System.out.println("Error: File does not exist or is not a regular file.");
                return;
            }

            // Load calendar data from the file
            CalendarManager calendarManager = loadCalendarFromFile(fileName);

            // Loop for the User interaction
            boolean exitLoopFlag = false;
            while (!exitLoopFlag) {
                System.out.println("Choose an option:");
                System.out.println("1. Add New Event");
                System.out.println("2. Modify Existing Event");
                System.out.println("3. Change Task State");
                System.out.println("4. Exit (+ Save Changes)");

                Scanner scanner = new Scanner(System.in);
                String choice = scanner.nextLine();

                // Execute the chosen option
                switch (choice) {
                    case "1":
                        addNewEvent(calendarManager); // Add a new event
                        break;
                    case "2":
                        modifyEvent(calendarManager); // Modify an existing event
                        break;
                    case "3":
                        changeTaskState(calendarManager); // Change the state of a task
                        break;
                    case "4":
                        exitLoopFlag = true; // Exit the program
                        break;
                    default:
                        System.out.println("Invalid option. Please choose again.");
                }
            }

            // Save changes to the calendar file
            saveCalendarToFile(calendarManager, fileName);
        } else {
            // Instructions if the command format is incorrect
            System.out.println("Command1: java -jar miCalendari-1.0.jar [print argument] [file.ics]");
            System.out.println("Command2: java -jar miCalendari-1.0.jar [file.ics]");
        }

        //Testing as well
        TimeService.stop();

    }



    private static LocalDateTime inputDateTime(Scanner scanner, String prompt) {
        LocalDateTime dateTime = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        while (dateTime == null) {
            System.out.println(prompt);
            System.out.print("Enter year (YYYY): ");
            int year = scanner.nextInt();
            System.out.print("Enter month (1-12): ");
            int month = scanner.nextInt();
            System.out.print("Enter day (1-31): ");
            int day = scanner.nextInt();
            System.out.print("Enter hour (0-23): ");
            int hour = scanner.nextInt();
            System.out.print("Enter minute (0-59): ");
            int minute = scanner.nextInt();

            try {
                dateTime = LocalDateTime.of(year, month, day, hour, minute);
            } catch (DateTimeException e) {
                System.out.println("Invalid date or time entered. Please try again.");
                scanner.nextLine(); // Clear the buffer
            }
        }

        return dateTime;
    }

    private static void addNewEvent(CalendarManager calendarManager) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the type of event (Event, Appointment, Task):");
        String type = scanner.nextLine().trim().toLowerCase();

        System.out.println("Enter title:");
        String title = scanner.nextLine();

        System.out.println("Enter description:");
        String description = scanner.nextLine();

        LocalDateTime dateTime;

        if (type.equals("event")) {
            dateTime = inputDateTime(scanner, "\nEnter start date and time:");
            calendarManager.addEvent(new Event(title, description, dateTime).toVEvent());
        } else if (type.equals("appointment")) {
            dateTime = inputDateTime(scanner, "\nEnter start date and time:");
            System.out.println("Enter Apointment's duration in minutes:");
            long durationInMinutes = scanner.nextLong();
            long durationInSeconds = durationInMinutes * 60; // Convert minutes to seconds
            calendarManager.addEvent(new Appointment(title, description, dateTime, durationInSeconds).toVAppointment());
        } else if (type.equals("task")) {
            LocalDateTime deadline = inputDateTime(scanner, "\nEnter deadline date and time:");
            TimeTeller timeTeller = TimeService.getTeller();
            dateTime = timeTeller.now(); // Use current time based on TimeTeller
            calendarManager.addTask(new Task(title, description, dateTime, deadline, false).toVToDo());
        } else {
            System.out.println("Invalid event type entered.");
            return; // Exit the method if the event type is invalid
        }
    }

    private static void modifyEvent(CalendarManager calendarManager) {
        Scanner scanner = new Scanner(System.in);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        List<CalendarComponent> allElements = calendarManager.getAllElements();

        System.out.println("Select an element to modify:");
        for (int i = 0; i < allElements.size(); i++) {
            CalendarComponent component = allElements.get(i);

            if (component instanceof VEvent) {
                VEvent event = (VEvent) component;

                // Extract the event title
                String title = event.getSummary().getValue();

                // Extract and format the start date and time
                DtStart startProperty = event.getStartDate();
                LocalDateTime start = LocalDateTime.ofInstant(startProperty.getDate().toInstant(), ZoneId.systemDefault());
                String formattedStart = start.format(formatter);

                // Determine if the event has DTEND or DURATION properties
                DtEnd dtEnd = (DtEnd) event.getProperty(Property.DTEND);
                Duration duration = (Duration) event.getProperty(Property.DURATION);

                String eventType;
                if (dtEnd != null || duration != null) {
                    eventType = "Appointment";
                } else {
                    eventType = "Event";
                }

                // Print the event type, title, and start time
                System.out.println(i + ": " + title + " (" + eventType + " at " + formattedStart + ")");
            }
            else if (component instanceof VToDo) {
                VToDo task = (VToDo) component;
                String title = task.getSummary().getValue();

                // Retrieve the Due property from the task
                Due due = (Due) task.getProperty(Property.DUE);

                LocalDateTime dueDateTime = null;
                if (due != null && due.getDate() != null) {
                    // Convert the date to LocalDateTime
                    dueDateTime = LocalDateTime.ofInstant(due.getDate().toInstant(), ZoneId.systemDefault());
                }

                // Format the due date
                String formattedDue;
                if (dueDateTime != null) {
                    formattedDue = dueDateTime.format(formatter);
                } else {
                    formattedDue = "N/A"; // Use "N/A" if the due date is not available
                }

                // Print the task title and its due date
                System.out.println(i + ": " + title + " (Task due " + formattedDue + ")");
            }
        }

        System.out.println("Element:");
        int selectedIndex = Integer.parseInt(scanner.nextLine());
        CalendarComponent selectedComponent = allElements.get(selectedIndex);

        if (selectedComponent instanceof VEvent) {
            VEvent event = (VEvent) selectedComponent;
            System.out.println("Modifying Event: " + event.getSummary().getValue());

            // Modify the title
            System.out.println("Enter new Title (Dont type anything to keep the original):");
            String newTitle = scanner.nextLine();
            if (!newTitle.isEmpty()) {
                event.getSummary().setValue(newTitle);
            }

            // Modify the description
            System.out.println("Enter new Description (Dont type anything to keep the original):");
            String newDescription = scanner.nextLine();
            if (!newDescription.isEmpty()) {
                event.getDescription().setValue(newDescription);
            }

            // Modify the start date and time using inputDateTime
            LocalDateTime newStart = inputDateTime(scanner, "Enter Start date and time (Dont type anything to keep the original):");
            event.getStartDate().setDate(new DateTime(java.util.Date.from(newStart.atZone(ZoneId.systemDefault()).toInstant())));

            // Check and modify DTEND or Duration
            DtEnd dtEnd = (DtEnd) event.getProperty(Property.DTEND);
            Duration duration = (Duration) event.getProperty(Property.DURATION);

            if (dtEnd != null) {
                // Modify DTEND
                LocalDateTime newEnd = inputDateTime(scanner, "Enter New End date and time (Dont type anything to keep the original):");
                if (newEnd != null) {
                    dtEnd.setDate(new DateTime(java.util.Date.from(newEnd.atZone(ZoneId.systemDefault()).toInstant())));
                }
            } else if (duration != null) {
                // Replace Duration with DTEND
                LocalDateTime newEnd = inputDateTime(scanner, "Enter New End date and time to replace duration:");
                if (newEnd != null) {
                    event.getProperties().remove(duration);
                    event.getProperties().add(new DtEnd(new DateTime(java.util.Date.from(newEnd.atZone(ZoneId.systemDefault()).toInstant()))));
                }
            }
        }
        if (selectedComponent instanceof VToDo) {

            VToDo task = (VToDo) selectedComponent;
            System.out.println("Modifying Task: " + task.getSummary().getValue());

            // Modify the title
            System.out.println("Enter new title (Dont type anything to keep the original):");
            String newTitle = scanner.nextLine();
            if (!newTitle.isEmpty()) {
                task.getSummary().setValue(newTitle);
            }

            // Modify the description
            System.out.println("Enter new description (Dont type anything to keep the original):");
            String newDescription = scanner.nextLine();
            if (!newDescription.isEmpty()) {
                if (task.getDescription() != null) {
                    task.getDescription().setValue(newDescription);
                } else {
                    task.getProperties().add(new Description(newDescription));
                }
            }

            // Modify the due date for the task using inputDateTime
            LocalDateTime newDueDate = inputDateTime(scanner, "Enter new due date and time:");
            if (newDueDate != null) {
                Due due = (Due) task.getProperty(Property.DUE);
                if (due != null) {
                    due.setDate(new DateTime(java.util.Date.from(newDueDate.atZone(ZoneId.systemDefault()).toInstant())));
                } else {
                    task.getProperties().add(new Due(new DateTime(java.util.Date.from(newDueDate.atZone(ZoneId.systemDefault()).toInstant()))));
                }
            }
        }

    }
    private static void changeTaskState(CalendarManager calendarManager) {
        Scanner scanner = new Scanner(System.in);

        // Display a list of tasks to the user with a counter
        System.out.println("Select a task to change its state:");
        List<VToDo> tasks = calendarManager.getTasks();
        int counter = 0;
        for (VToDo task : tasks) {
            String taskTitle = task.getSummary().getValue();
            System.out.println(counter + ": " + taskTitle); // Print each task's title with its counter
            counter++;
        }

        VToDo selectedTask = null;
        while (selectedTask == null) {
            // Prompt user to enter the counter of the task they wish to modify
            System.out.println("Enter the number of the task to change state:");
            String input = scanner.nextLine();
            int selectedCounter;
            try {
                selectedCounter = Integer.parseInt(input);
                if (selectedCounter >= 0 && selectedCounter < tasks.size()) {
                    selectedTask = tasks.get(selectedCounter);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }


        // Determine the current completion status of the task
        Status currentStatus = selectedTask.getStatus();
        boolean isCurrentlyCompleted;
        if (currentStatus != null && currentStatus.equals(Status.VTODO_COMPLETED)) {
            isCurrentlyCompleted = true;
        } else {
            isCurrentlyCompleted = false;
        }
        System.out.println("Is the task completed? (true/false) (current: " + (isCurrentlyCompleted ? "true" : "false") + "):");

        // Read user input for the new completion status
        boolean isCompleted = false; // Default value
        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false")) {
                isCompleted = input.equalsIgnoreCase("true");
                break; // Exit the loop once valid input is received
            } else {
                System.out.println("Invalid input. Please enter 'true' or 'false'.");
            }
        }

        // Update the task's status based on user input
        Status newStatus;
        if (isCompleted) {
            newStatus = Status.VTODO_COMPLETED;
        } else {
            newStatus = Status.VTODO_IN_PROCESS; // Default status for incomplete tasks
        }
        selectedTask.getProperties().remove(currentStatus); // Remove the existing status
        selectedTask.getProperties().add(newStatus); // Set the new status
    }





    private static Calendar currentCalendar;

    private static void saveCalendarToFile(CalendarManager calendarManager, String fileName) {
        try {
            // Initialize currentCalendar if it's null
            if (currentCalendar == null) {
                currentCalendar = new Calendar();
            }

            // Clear any existing components in currentCalendar
            currentCalendar.getComponents().clear();

            // Add all events from the CalendarManager to currentCalendar
            for (VEvent event : calendarManager.getEvents()) {
                currentCalendar.getComponents().add(event);
            }

            // Add all tasks from the CalendarManager to currentCalendar
            for (VToDo task : calendarManager.getTasks()) {
                currentCalendar.getComponents().add(task);
            }

            // Write the currentCalendar data to the specified file
            FileOutputStream fout = new FileOutputStream(fileName);
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(currentCalendar, fout);

            // Indicate successful saving
            System.out.println("Calendar saved to file: " + fileName);
        } catch (IOException e) {
            // Handle any IO exceptions during file writing
            System.out.println("Error saving calendar to file: " + e.getMessage());
        }
    }

    public static CalendarManager loadCalendarFromFile(String fileName) {
        // Create a new CalendarManager instance
        CalendarManager calendarManager = new CalendarManager();

        try {
            // Read the .ics file
            FileReader fileReader = new FileReader(fileName);
            CalendarBuilder builder = new CalendarBuilder();

            // Build a calendar from the file content
            currentCalendar = builder.build(fileReader);

            // Iterate through each component in the built calendar
            for (Object component : currentCalendar.getComponents()) {
                // If the component is an event, add it to CalendarManager
                if (component instanceof VEvent) {
                    calendarManager.addEvent((VEvent) component);
                }
                // If the component is a task, add it to CalendarManager
                else if (component instanceof VToDo) {
                    calendarManager.addTask((VToDo) component);
                }
            }
        } catch (IOException | ParserException e) {
            // Handle any IO or parsing exceptions during file reading
            System.out.println("Error loading calendar from file: " + e.getMessage());
        }

        // Return the populated CalendarManager
        return calendarManager;
    }
}
