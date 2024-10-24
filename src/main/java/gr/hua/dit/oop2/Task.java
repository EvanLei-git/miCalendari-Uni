package gr.hua.dit.oop2;

import gr.hua.dit.oop2.calendar.TimeService;
import gr.hua.dit.oop2.calendar.TimeTeller;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.*;
import java.time.LocalDateTime;
//  Class named "Task" that extends the "Event" class.
public class Task extends Event {
    // Declare private instance variables for storing task deadline and completion status.
    private LocalDateTime deadline;
    private boolean completed;

    // Constructor for creating a Task object with specified properties.
    public Task(String title, String description, LocalDateTime date, LocalDateTime deadline, boolean completed) {
        // Call the constructor of the parent class "Event" with title, description, and date parameters.
        super(title, description, date);

        // Initialize the deadline and completed properties with the provided values.
        this.deadline = deadline;
        this.completed = completed;
    }

    // Getter method to retrieve the task deadline.
    public LocalDateTime getDeadline() {
        return deadline;
    }

    // Setter method to update the task deadline.
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    // Getter method to check if the task is completed.
    public boolean isCompleted() {
        return completed;
    }

    // Setter method to update the completion status of the task.
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    // Method to convert the Task object into a VToDo object for iCalendar representation.
    public VToDo toVToDo() {
        // Create a new VToDo object.
        VToDo vToDo = new VToDo();

        // Add the task description and title as properties to the VToDo.
        vToDo.getProperties().add(new Description(getDescription()));
        vToDo.getProperties().add(new Summary(getTitle()));

        // Create a calendar instance and set it to the deadline date and time.
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, deadline.getYear());
        cal.set(java.util.Calendar.MONTH, deadline.getMonthValue() - 1);
        cal.set(java.util.Calendar.DAY_OF_MONTH, deadline.getDayOfMonth());
        cal.set(java.util.Calendar.HOUR_OF_DAY, deadline.getHour());
        cal.set(java.util.Calendar.MINUTE, deadline.getMinute());
        cal.set(java.util.Calendar.SECOND, deadline.getSecond());

        // Create a DateTime object for the DUE property using the calendar instance.
        DateTime dueDateTime = new DateTime(cal.getTime());
        vToDo.getProperties().add(new Due(dueDateTime));

        // Add a DtStamp property with the current date and time.
        vToDo.getProperties().add(new DtStamp(new DateTime(new java.util.Date())));

        // Add a Status property if the task is completed.
        if (completed) {
            vToDo.getProperties().add(Status.VTODO_COMPLETED);
        }

        // Return the VToDo object representing the task.
        return vToDo;
    }
}