package org.salespointframework.core.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.salespointframework.core.user.UserIdentifier;
import org.salespointframework.util.Iterables;
import java.util.Objects;

/**
 * 
 * This is a representation of a calendar entry which provides basic
 * functionality. <code>PersistentCalendarEntry</code> is intended to be used in
 * conjunction with <code>PersistentCalendar</code> to save entries to a
 * database, using JPA.
 * 
 * @author Stanley Förster
 * 
 */
@Entity
public class PersistentCalendarEntry implements CalendarEntry {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "ENTRY_ID"))
    private CalendarEntryIdentifier calendarEntryIdentifier = new CalendarEntryIdentifier();

    //@ElementCollection(targetClass = EnumSet.class)
    //@CollectionTable(joinColumns = @JoinColumn(referencedColumnName = "ENTRY_ID", name = "ENTRY_ID"))
    //@AttributeOverride(name = "key.id", column = @Column(name = "USER_ID"))
    //@Column(name = "ENUM")
    //private Map<UserIdentifier, EnumSet<CalendarEntryCapability>> capabilities = new HashMap<UserIdentifier, EnumSet<CalendarEntryCapability>>();

    private int                                                   repeatCount;

    private String                                                description;

    @Transient
    private Interval                                              duration;

    @Column(name="START_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date                                                  start;

    @Column(name="END_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date                                                  end;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "OWNER_ID"))
    private UserIdentifier                                        owner;

    private Period                                                period;

    private String                                                title;

    /**
     * This constructor should not be used. It only exists for persistence
     * reasons.
     */
    @Deprecated
    protected PersistentCalendarEntry() {  }

    /**
     * Creates a new calendar entry that can be persisted in a
     * {@link PersistentCalendar}.
     * 
     * @param owner
     *            The id of the user, who created this entry.
     * @param title
     *            The title of this entry.
     * @param start
     *            Start time and date.
     * @param end
     *            End time and date.
     * @throws NullPointerException
     *             if one or more arguments are <code>null</code>
     * @throws IllegalArgumentException
     *             if the end is before the start
     */
    public PersistentCalendarEntry(UserIdentifier owner, String title, DateTime start, DateTime end) {
        this(owner, title, start, end, "", Period.ZERO, 0);
    }

    /**
     * Creates a new calendar entry that can be persisted in a
     * {@link PersistentCalendar}.
     * 
     * @param owner
     *            The id of the user, who created this entry.
     * @param title
     *            The title of this entry.
     * @param start
     *            Start time and date.
     * @param end
     *            End time and date.
     * @param description
     *            The description of this entry.
     * @throws NullPointerException
     *             if one or more arguments are <code>null</code>
     */
    public PersistentCalendarEntry(UserIdentifier owner, String title, DateTime start, DateTime end, String description) {
        this(owner, title, start, end, description, Period.ZERO, 0);
    }

    /**
     * Creates a new cyclic calendar entry that will be repeated after a period.
     * There are parameters that define how often it will be repeated and how
     * much time between two repetitions will be.
     * 
     * @param owner
     *            The id of the user, who created this entry.
     * @param title
     *            The title of this entry.
     * @param start
     *            Start time and date.
     * @param end
     *            End time and date.
     * @param description
     *            The description of this entry.
     * @param period
     *            The period of one repetition of this entry. This is the time
     *            between two starts and so it has to be less than the duration.
     * @param count
     *            Times how often the event is repeated. -1 means infinitely.
     * @throws NullPointerException
     *             if one ore more arguments are <code>null</code>
     * @throws IllegalArgumentException
     *             if the time between two repetitions should be smaller than
     *             the duration of the entry.
     * 
     * @see Interval
     * @see Period
     */
    public PersistentCalendarEntry(UserIdentifier owner, String title, DateTime start, DateTime end, String description, Period period, int count) {

        detectDateAnomalies(Objects.requireNonNull(start, "start must not be null"), Objects.requireNonNull(end, "end must not be null"), count, Objects.requireNonNull(period, "period must not be null"));

        this.owner = Objects.requireNonNull(owner, "owner must not be null must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");

        this.repeatCount = count;
        this.period = period;

        setStartEnd(start.toDate(), end.toDate());

        //capabilities.put(owner, EnumSet.allOf(CalendarEntryCapability.class));
    }

    /**
     * Initializes the <code>Interval</code> field out of <code>startDate</code>
     * and <code>endDate</code> fields after they have been loaded from the
     * database. Can also be used to update <code>duration</code> if
     * <code>startDate</code> or <code>endDate</end> were changed manually.
     */
    @PostLoad
    protected void updateDuration() {
        duration = new Interval(new DateTime(start), new DateTime(end));
    }

    /*
     * Use this method to set new start and end dates!
     */
    private final void setStartEnd(Date startDate, Date endDate) {
        this.start = startDate;
        this.end = endDate;
        updateDuration();
    }

    /*
     * Throws an exceptions if a anomaly in time has been detected.
     */
    private final void detectDateAnomalies(DateTime start, DateTime end, int repeatCount, Period period) {
        Interval duration = new Interval(start, end);
        
        if (repeatCount < -1)
            throw new IllegalArgumentException("The repeat count of an appointment can't be less than zero, except -1, but was " + repeatCount);

        if ((repeatCount != 0) && duration.getStart().plus(period).isBefore(duration.getEnd()))
            throw new IllegalArgumentException("The time between two repetitions can not be less than the duration of a calendar entry!");
    }

    /*
    @Override
    public final void addCapability(UserIdentifier user, CalendarEntryCapability capability) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(capabilities, "capability");

        if (capabilities.containsKey(user))
            capabilities.get(user).add(capability);
        else
            capabilities.put(user, EnumSet.of(capability));
    }
    */

    /**
     * {@inheritDoc}
     * <p>
     * Two <code>PersistentCalendarEntry</code>s are equal to each other, if their identifiers are equal.
     * </p>
     */
    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other == this)
            return true;
        if (other instanceof PersistentCalendarEntry)
            return this.calendarEntryIdentifier.equals(((PersistentCalendarEntry)other).calendarEntryIdentifier);
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The ID is the entry's primary key, generated automatically when the entry
     * has been created.
     * </p>
     */
    @Override
    public final CalendarEntryIdentifier getIdentifier() {
        return calendarEntryIdentifier;
    }

    /*
    @Override
    public final Iterable<CalendarEntryCapability> getCapabilities(UserIdentifier user) {
        Objects.requireNonNull(user, "user");

        EnumSet<CalendarEntryCapability> caps = capabilities.get(user);
       
        return caps == null ? null : Iterables.of(caps);
    }
    */

    /**
     * Returns the number how often this appointment will be repeated, where -1
     * means indefinitely.
     * 
     * @return The number how often this appointment will be repeated.
     */
    public final long getRepeatCount() {
        return repeatCount;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    /**
     * @see Interval#getEnd()
     */
    @Override
    public final DateTime getEnd() {
        return duration.getEnd();
    }


    @Override
    public final Iterable<Interval> getEntryList(int maxEntries) {
        List<Interval> dates = new ArrayList<Interval>();
        Interval last;

        if (repeatCount != -1)
            maxEntries = (maxEntries < repeatCount) ? maxEntries : repeatCount;

        if (maxEntries == 0)
            return dates;

        dates.add(duration);
        last = duration;
        // >1, because duration itself is the first entry
        for (; maxEntries > 1; maxEntries--) {
            last = new Interval(last.getStart().plus(period), last.getEnd().plus(period));
            dates.add(last);
        }

        return Iterables.of(dates);
    }


    public final Iterable<Interval> getEntryList(Interval interval) {
        List<Interval> dates = new ArrayList<Interval>();

        int maxCount = this.repeatCount;

        maxCount = maxCount == -1 ? Integer.MAX_VALUE : maxCount;

        Interval nextInterval = this.duration;

        for (int i = 0; i <= maxCount && !interval.isBefore(nextInterval); i++) {
            if (interval.overlaps(nextInterval)) {
                dates.add(nextInterval);
            }
            nextInterval = new Interval(nextInterval.getStart().plus(this.period), nextInterval.getEnd().plus(this.period));
        }

        return Iterables.of(dates);
    }

    @Override
    public final UserIdentifier getOwner() {
        return owner;
    }

    /**
     * Returns the timespan that was defined to be between two starts of this
     * appointment.
     * 
     * @return the time between two repetitions of this appointment as a
     *         {@link Period}
     */
    public final Period getPeriod() {
        return period;
    }

    /**
     * @see Interval#getStart()
     */
    @Override
    public final DateTime getStart() {
        return duration.getStart();
    }

    @Override
    public final String getTitle() {
        return title;
    }

    /*
    @Override
    public final Iterable<UserIdentifier> getUsersByCapability(CalendarEntryCapability capability) {
        Objects.requireNonNull(capability, "capability");

        List<UserIdentifier> result = new ArrayList<UserIdentifier>();
        for (Entry<UserIdentifier, EnumSet<CalendarEntryCapability>> e : capabilities.entrySet()) {
            if (e.getValue().contains(capability))
                result.add(e.getKey());
        }

        return result;
    }
    */

    /**
     * {@inheritDoc}
     * <p>
     * The hash of this object is the hash of its primary key.
     * </p>
     * 
     * @return The hash code for this entry.
     */
    @Override
    public final int hashCode() {
        return calendarEntryIdentifier.hashCode();
    }

    /*
    @Override
    public final void removeCapability(UserIdentifier user, CalendarEntryCapability capability) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(capability, "capability");
        if (user.equals(owner))
            throw new IllegalArgumentException("Capabilities cannot be removed from the owner of the calendar entry");

        capabilities.get(user).remove(capability);

        if (capabilities.get(user).isEmpty())
            capabilities.remove(user);
    }
    */

    /**
     * Sets the number how often this appointment should be repeated. -1 means
     * infinitely.
     * 
     * @param count
     *            Number how often this appointment should be repeated. This
     *            must be a positive value, including zero. Also -1 is allowed
     *            for indicating infinite repetitions.
     * 
     * @throws IllegalArgumentException
     *             if <code>count</code> is less than -1.
     */
    public final void setCount(int count) {
        detectDateAnomalies(this.duration.getStart(), this.duration.getEnd(), count, this.period);

        this.repeatCount = count;
    }

    @Override
    public final void setDescription(String description) {
        this.description = Objects.requireNonNull(description, "description");
    }

    /**
     * @throws IllegalArgumentException
     *             if the end is before the start
     * 
     * @see Interval
     */
    @Override
    public final void setEnd(DateTime end) {
        DateTime start;
        
        if (end.isBefore(this.duration.getStart())) {
            start = end.minus(this.duration.getEndMillis() - this.duration.getStartMillis());
        } else {
            start = this.duration.getStart();
        }
        
        detectDateAnomalies(start, Objects.requireNonNull(end, "end"), this.repeatCount, this.period);

        setStartEnd(start.toDate(), end.toDate());
    }

    /**
     * Sets a new time span that should be between two starts of this entry. The
     * time span has to be larger than the duration of this appointment.
     * 
     * @param period
     *            The time span between two repetitions.
     * @throws NullPointerException
     *             if one ore more arguments are <code>null</code>
     * @throws IllegalArgumentException
     *             if the time between two repetitions should be smaller than
     *             the duration of the entry.
     */
    public final void setPeriod(Period period) {
        detectDateAnomalies(this.duration.getStart(), this.duration.getEnd(), this.repeatCount, Objects.requireNonNull(period, "period"));

        this.period = period;
    }

    /**
     * @throws IllegalArgumentException
     *             if the start is after the end
     * 
     * @see Interval
     */
    @Override
    public final void setStart(DateTime start) {
        DateTime end;
        
        if (start.isAfter(this.duration.getEnd())) {
            end = start.plus(this.duration.getEndMillis() - this.duration.getStartMillis());
        } else {
            end = this.duration.getEnd();
        }
        
        detectDateAnomalies(Objects.requireNonNull(start, "start"), end, this.repeatCount, this.period);
        setStartEnd(start.toDate(), end.toDate());
    }

    @Override
    public final void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    /**
     * Returns a string representation of this entry. The format of this string
     * is:
     * <p>
     * <code>title + " (" + getStart() + " - " + getEnd() + ")"</code>
     * </p>
     * 
     * @return The string representation of this entry.
     */
    @Override
    public String toString() {
        return title + " (" + getStart() + " - " + getEnd() + ")";
    }
}