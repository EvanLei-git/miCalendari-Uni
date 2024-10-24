package gr.hua.dit.oop2;

import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.DateTime;

import net.fortuna.ical4j.model.property.*;
import java.time.LocalDateTime;
import java.util.UUID;


//  Class named "Apointment" that extends the "Event" class.
public class Appointment extends Event {
    // Duration for the appointment in seconds
    // We are thinking of removing this. and just keeping everything as DTEND
    private long duration;


    // Constructor for Appointment
    public Appointment(String title, String description, LocalDateTime date, long duration) {
        super(title, description, date);
        this.duration = duration;
    }

    // Getters and Setters for duration
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    // Custom method to convert Appointment to VEvent
    public VEvent toVAppointment() {
        // Create a calendar instance and set it to the appointment start date and time.
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, getDate().getYear());
        cal.set(java.util.Calendar.MONTH, getDate().getMonthValue() - 1); // Months are zero-based
        cal.set(java.util.Calendar.DAY_OF_MONTH, getDate().getDayOfMonth());
        cal.set(java.util.Calendar.HOUR_OF_DAY, getDate().getHour());
        cal.set(java.util.Calendar.MINUTE, getDate().getMinute());
        cal.set(java.util.Calendar.SECOND, getDate().getSecond());

        // Create a DateTime object for the appointment start.
        DateTime start = new DateTime(cal.getTime());

        // Calculated the end time of the appointment based on the duration in milliseconds.
        DateTime end = new DateTime(start.getTime() + duration * 1000);

        // Created a new VEvent object for the appointment.
        VEvent vEvent = new VEvent();

        // Add a unique identifier (UID) property to the VEvent.
        vEvent.getProperties().add(new Uid(UUID.randomUUID().toString()));

        // Add the appointment title and description as properties to the VEvent.
        vEvent.getProperties().add(new Summary(getTitle()));
        vEvent.getProperties().add(new Description(getDescription()));

        // Add the start and end date/time properties to the VEvent.
        vEvent.getProperties().add(new DtStart(start));
        vEvent.getProperties().add(new DtEnd(end));

        return vEvent;
    }
}