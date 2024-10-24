package gr.hua.dit.oop2;

import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.*;


import java.time.LocalDateTime;
import java.util.UUID;

public class Event {
    protected String title;
    protected String description;
    private LocalDateTime date; //java.util.Date, java.time.LocalDateTime test

    public Event(String title, String description, LocalDateTime date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public VEvent toVEvent() {
        // Create a calendar instance

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, date.getYear());
        cal.set(java.util.Calendar.MONTH, date.getMonthValue() - 1);
        cal.set(java.util.Calendar.DAY_OF_MONTH, date.getDayOfMonth());
        cal.set(java.util.Calendar.HOUR_OF_DAY, date.getHour());
        cal.set(java.util.Calendar.MINUTE, date.getMinute());
        cal.set(java.util.Calendar.SECOND, date.getSecond());

        // Create a DateTime object for the appointment start.

        DateTime start = new DateTime(cal.getTime());

        // The rest are just simply adding Event Properties
        VEvent vEvent = new VEvent();
        vEvent.getProperties().add(new Uid(UUID.randomUUID().toString()));
        vEvent.getProperties().add(new Summary(getTitle()));
        vEvent.getProperties().add(new Description(getDescription()));
        vEvent.getProperties().add(new DtStart(start));
        return vEvent;
    }
}