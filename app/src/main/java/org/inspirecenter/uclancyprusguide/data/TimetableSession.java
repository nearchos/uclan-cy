package org.inspirecenter.uclancyprusguide.data;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Nearchos Paspallis
 * 22/11/2015.
 */
public class TimetableSession implements Serializable, Comparable {
    private String moduleCode;
    private String moduleName;
    private String roomCode;
    private String startTimeFormatted;
    private String endTimeFormatted;
    private String dayOfWeek;
    private int duration;
    private String lecturerName;
    private String sessionDescription;

    public TimetableSession(String moduleCode, String moduleName, String roomCode, String startTimeFormatted, String endTimeFormatted, String dayOfWeek, int duration, String lecturerName, String sessionDescription) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.roomCode = roomCode;
        this.startTimeFormatted = startTimeFormatted;
        this.endTimeFormatted = endTimeFormatted;
        this.dayOfWeek = dayOfWeek;
        this.duration = duration;
        this.lecturerName = lecturerName;
        this.sessionDescription = sessionDescription;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getStartTimeFormatted() {
        return startTimeFormatted;
    }

    public String getEndTimeFormatted() {
        return endTimeFormatted;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public int getDuration() {
        return duration;
    }

    public String getLecturerName() {
        return lecturerName == null || lecturerName.isEmpty() ? "N/A" : lecturerName;
    }

    public String getSessionDescription() {
        return sessionDescription;
    }

    private final String [] DAYS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private int compareDays(String dayOfWeekLeft, final String dayOfWeekRight) {
        int indexLeft = Arrays.binarySearch(DAYS, dayOfWeekLeft);
        int indexRight = Arrays.binarySearch(DAYS, dayOfWeekRight);
        return indexRight - indexLeft;
    }

    @Override
    public int compareTo(@NonNull Object anotherTimetableSession) {
        final TimetableSession timetableSession = (TimetableSession) anotherTimetableSession;
        int compareDays = compareDays(this.dayOfWeek, timetableSession.dayOfWeek);
        return compareDays == 0 ? startTimeFormatted.compareTo(timetableSession.startTimeFormatted) : compareDays;
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + getStartTimeFormatted() + ": " + moduleCode + " - " + moduleName;
    }

    public String toFullString() {
        return "TimetableSession{" +
                "moduleCode='" + moduleCode + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", roomCode='" + roomCode + '\'' +
                ", startTimeFormatted='" + startTimeFormatted + '\'' +
                ", endTimeFormatted='" + endTimeFormatted + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", duration=" + duration +
                ", lecturerName='" + lecturerName + '\'' +
                ", sessionDescription='" + sessionDescription + '\'' +
                '}';
    }
}