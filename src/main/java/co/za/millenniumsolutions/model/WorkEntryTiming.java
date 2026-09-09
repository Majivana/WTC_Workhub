package co.za.millenniumsolutions.model;

import java.time.LocalTime;

public record WorkEntryTiming(LocalTime startTime, LocalTime endTime, int breakMinutes) {
}
