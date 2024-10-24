package gr.hua.dit.oop2;

import gr.hua.dit.oop2.calendar.TimeService;
import gr.hua.dit.oop2.calendar.TimeTeller;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalendarManager {
    // List just for events and appointments (we can easily tell apart which is which)
    private List<VEvent> events;
    // List saving tasks
    private List<VToDo> tasks;

    // List to save both tasks and events
    private List<CalendarComponent> allElements; // Combined list

    public CalendarManager() {
        events = new ArrayList<>();
        tasks = new ArrayList<>();
        allElements = new ArrayList<>(); // Initialize the combined list

    }
    // Add Event to List
    public void addEvent(VEvent event) {
        events.add(event);
        // Adding allElements to the combined list
        allElements.add(event);
        sortAllElements();
    }

    // Add Task to List
    public void addTask(VToDo task) {
        tasks.add(task);
        // Adding allElements to the combined list
        allElements.add(task);
        sortAllElements();
    }


    private void sortAllElements() {
        // Outer loop goes through all elements
        for (int i = 0; i < allElements.size(); i++) {
            // Inner loop compares each element with the next one
            for (int j = 0; j < allElements.size() - 1; j++) {
                // Initialize dates to null
                LocalDateTime date1 = null;
                LocalDateTime date2 = null;

                // Extract dates for comparison based on component type
                if (allElements.get(j) instanceof VEvent) {
                    DtStart dtStart1 = (DtStart) allElements.get(j).getProperty(Property.DTSTART);
                    if (dtStart1 != null) {
                        date1 = LocalDateTime.ofInstant(dtStart1.getDate().toInstant(), ZoneId.systemDefault());
                    }
                } else if (allElements.get(j) instanceof VToDo) {
                    Due due1 = (Due) allElements.get(j).getProperty(Property.DUE);
                    if (due1 != null) {
                        date1 = LocalDateTime.ofInstant(due1.getDate().toInstant(), ZoneId.systemDefault());
                    }
                }

                if (allElements.get(j + 1) instanceof VEvent) {
                    DtStart dtStart2 = (DtStart) allElements.get(j + 1).getProperty(Property.DTSTART);
                    if (dtStart2 != null) {
                        date2 = LocalDateTime.ofInstant(dtStart2.getDate().toInstant(), ZoneId.systemDefault());
                    }
                } else if (allElements.get(j + 1) instanceof VToDo) {
                    Due due2 = (Due) allElements.get(j + 1).getProperty(Property.DUE);
                    if (due2 != null) {
                        date2 = LocalDateTime.ofInstant(due2.getDate().toInstant(), ZoneId.systemDefault());
                    }
                }

                // Compare the two dates and swap if necessary
                if ((date1 != null && date2 != null && date1.isAfter(date2)) ||
                        (date1 == null && date2 != null)) {
                    Collections.swap(allElements, j, j + 1);
                }
            }
        }
    }


    // Getters for our Lists
    public List<CalendarComponent> getAllElements() {
        return new ArrayList<>(allElements);
    }

    public List<VEvent> getEvents() {
        return new ArrayList<>(events);
    }

    public List<VToDo> getTasks() {
        return new ArrayList<>(tasks);
    }



    // Edit method for Events(+ Appointments) not used YET
    public void editEvent(int index, VEvent updatedEvent) {
        if (index >= 0 && index < events.size()) {
            events.set(index, updatedEvent);
            allElements.set(index, updatedEvent); // Update in combined list too
        }
    }

    public void editTask(int index, VToDo updatedTask) {
        if (index >= 0 && index < tasks.size()) {
            tasks.set(index, updatedTask);
            allElements.set(index, updatedTask); // Update in combined list too
        }
    }

    // Method to control the Calculation of the day, week, month
    private List<CalendarComponent> endPeriodCalculator(LocalDateTime tellernow, LocalDateTime endPeriod) {
        List<CalendarComponent> filteredEvents = new ArrayList<>();
        for (CalendarComponent comp : allElements) {
            // Existing logic inside your loop
            // Instead of printing, add the component to filteredEvents if it matches the condition
            if (comp instanceof VEvent) {
                VEvent event = (VEvent) comp;
                DtStart dtStart = event.getStartDate();
                if (dtStart != null) {
                    LocalDateTime start = LocalDateTime.ofInstant(dtStart.getDate().toInstant(), ZoneId.systemDefault());
                    if (start.isAfter(tellernow) && start.isBefore(endPeriod)) {
                        filteredEvents.add(comp);
                    }
                }
            } else if (comp instanceof VToDo) {
                VToDo task = (VToDo) comp;
                Due due = task.getDue();
                if (due != null) {
                    LocalDateTime dueDateTime = LocalDateTime.ofInstant(due.getDate().toInstant(), ZoneId.systemDefault());
                    if (!dueDateTime.isBefore(tellernow) && dueDateTime.isBefore(endPeriod)) {
                        filteredEvents.add(comp);
                    }
                }
            }
        }
        return filteredEvents;
    }
    // Method to control the Calculation of the pastday, pastweek, pastmonth

    private List<CalendarComponent> pastPeriodCalculator(LocalDateTime tellernow, LocalDateTime startPeriod) {
        List<CalendarComponent> filteredEvents = new ArrayList<>();
        for (CalendarComponent comp : allElements) {
            // Existing logic inside your loop
            // Instead of printing, add the component to filteredEvents if it matches the condition
            if (comp instanceof VEvent) {
                VEvent event = (VEvent) comp;
                DtStart dtStart = event.getStartDate();
                if (dtStart != null) {
                    LocalDateTime start = LocalDateTime.ofInstant(dtStart.getDate().toInstant(), ZoneId.systemDefault());
                    if (start.isAfter(startPeriod) && start.isBefore(tellernow)) {
                        filteredEvents.add(comp);
                    }
                }
            } else if (comp instanceof VToDo) {
                VToDo task = (VToDo) comp;
                Due due = task.getDue();
                if (due != null) {
                    LocalDateTime dueDateTime = LocalDateTime.ofInstant(due.getDate().toInstant(), ZoneId.systemDefault());
                    if (dueDateTime.isAfter(startPeriod) && dueDateTime.isBefore(tellernow)) {
                        filteredEvents.add(comp);
                    }
                }
            }
        }
        return filteredEvents;
    }


    //Finding the end of each Day
    //https://stackoverflow.com/questions/60607789/how-to-get-next-day-end-of-day-date-time-from-given-instant-java

    public List<CalendarComponent> printDay() {
        LocalDateTime now = LocalDateTime.now();
        return endPeriodCalculator(now, now.toLocalDate().atTime(23, 59, 59));
    }

    // Finding the week based on the day it is before sunday + Month implementation
    // https://stackoverflow.com/questions/9235845/get-the-week-start-and-end-date-given-a-current-date-and-week-start
    // https://stackoverflow.com/a/21325595

    public List<CalendarComponent> printWeek() {
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime now = timeTeller.now();

        // Calculate the number of days to add to get to the end of the week (Sunday)
        int daysToAdd = 7 - now.getDayOfWeek().getValue();

        // Calculate the date for the end of the week
        LocalDate dateEndOfWeek = now.toLocalDate().plusDays(daysToAdd);

        // Define the end of the week as the end of that day
        LocalDateTime endOfWeek = dateEndOfWeek.atTime(23, 59, 59);
        return endPeriodCalculator(now, endOfWeek);
    }

    public List<CalendarComponent> printMonth() {
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime now = timeTeller.now();

        // Get the length of the current month
        int lastDayOfMonth = now.toLocalDate().lengthOfMonth();

        // Calculate the date for the end of the month
        LocalDate dateEndOfMonth = now.toLocalDate().withDayOfMonth(lastDayOfMonth);

        // Define the end of the month as the end of that day
        LocalDateTime endOfMonth = dateEndOfMonth.atTime(23, 59, 59);

        return endPeriodCalculator(now, endOfMonth);
    }
    public List<CalendarComponent> printPastDay() {
        // Get the current time from TimeTeller
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime now = timeTeller.now();

        // Calculate the start of the current day
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();

        return pastPeriodCalculator(now, startOfDay);

    }
    public List<CalendarComponent>printPastWeek() {
        // Get the current time from TimeTeller
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime now = timeTeller.now();

        // Calculate the start of the current week
        LocalDateTime startOfWeek = now.toLocalDate().minusDays(now.getDayOfWeek().getValue() - 1).atStartOfDay();

        return pastPeriodCalculator(now, startOfWeek);

    }
    public List<CalendarComponent> printPastMonth() {
        // Get the current time from TimeTeller
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime now = timeTeller.now();

        // Calculate the start of the current month
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        return pastPeriodCalculator(now, startOfMonth);

    }
    public List<CalendarComponent> printNotCompleted() {
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime now = timeTeller.now();
        List<CalendarComponent> notCompletedTasks = new ArrayList<>();

        for (VToDo task : tasks) {
            Status statusProperty = (Status) task.getProperty(Status.STATUS);
            Due dueProperty = (Due) task.getProperty(Due.DUE);

            if (statusProperty != null && !statusProperty.getValue().equals(Status.VTODO_COMPLETED.getValue())) {
                if (dueProperty != null) {
                    LocalDateTime taskDeadline = LocalDateTime.ofInstant(dueProperty.getDate().toInstant(), ZoneId.systemDefault());
                    if (taskDeadline.isAfter(now)) {
                        notCompletedTasks.add(task);
                    }
                }
            }
        }
        return notCompletedTasks;
    }
    public CalendarComponent getMostRecentEvent() {
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime now = timeTeller.now();
        CalendarComponent mostRecent = null;
        LocalDateTime closestTime = null;

        for (CalendarComponent comp : allElements) {
            LocalDateTime componentTime = null;

            if (comp instanceof VEvent) {
                VEvent event = (VEvent) comp;
                DtStart dtStart = event.getStartDate();
                if (dtStart != null) {
                    componentTime = LocalDateTime.ofInstant(dtStart.getDate().toInstant(), ZoneId.systemDefault());
                }
            } else if (comp instanceof VToDo) {
                VToDo task = (VToDo) comp;
                Due due = task.getDue();
                if (due != null) {
                    componentTime = LocalDateTime.ofInstant(due.getDate().toInstant(), ZoneId.systemDefault());
                }
            }

            if (componentTime != null && componentTime.isAfter(now) &&
                    (closestTime == null || componentTime.isBefore(closestTime))) {
                closestTime = componentTime;
                mostRecent = comp;
            }
        }

        return mostRecent;
    }
    public List<CalendarComponent> printPastDeadline() {
        TimeTeller timeTeller = TimeService.getTeller();
        LocalDateTime now = timeTeller.now();
        List <CalendarComponent> pastDeadlineTasks = new ArrayList<>();
        for (VToDo task : tasks) {
            Status statusProperty = (Status) task.getProperty(Status.STATUS);
            Due dueProperty = (Due) task.getProperty(Due.DUE);

            if (statusProperty != null && !statusProperty.getValue().equals(Status.VTODO_COMPLETED.getValue())) {
                if (dueProperty != null) {
                    LocalDateTime taskDeadline = LocalDateTime.ofInstant(dueProperty.getDate().toInstant(), ZoneId.systemDefault());
                    if (taskDeadline.isBefore(now)) {
                        pastDeadlineTasks.add(task);
                    }
                }
            }
        }
        return pastDeadlineTasks;
    }

    // printing Event as is on the ics format
    private String testFormatEvent(VEvent event) {
        return event.toString();
    }
    //Printing Task as is on the ics format
    private String testFormatTask(VToDo task) {
        return task.toString();
    }
    private String formatEvent(VEvent event) {
        StringBuilder sb = new StringBuilder();

        // Determine if the event is an Appointment (has DTEND or DURATION)
        String eventType = (event.getEndDate() != null || event.getProperty(Property.DURATION) != null) ? "Appointment" : "Event";
        sb.append("Type: ").append(eventType).append("\n");

        // Append the title
        Summary summary = event.getSummary();
        if (summary != null) {
            sb.append("Title: ").append(summary.getValue()).append("\n");
        }

        // Append the start date and time
        DtStart start = event.getStartDate();
        if (start != null && start.getDate() != null) {
            sb.append("Start: ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(start.getDate())).append("\n");
        }

        // Append the end date and time for appointments
        if ("Appointment".equals(eventType)) {
            DtEnd end = event.getEndDate();
            if (end != null && end.getDate() != null) {
                sb.append("End: ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(end.getDate())).append("\n");
            }
        }

        return sb.toString();
    }
    private String formatTask(VToDo task) {
        StringBuilder sb = new StringBuilder();

        // Append the title
        Summary summary = task.getSummary();
        if (summary != null) {
            sb.append("Title: ").append(summary.getValue()).append("\n");
        }

        // Append the due date
        Due due = (Due) task.getProperty(Property.DUE);
        if (due != null && due.getDate() != null) {
            sb.append("Due: ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(due.getDate())).append("\n");
        }

        return sb.toString();
    }
}
