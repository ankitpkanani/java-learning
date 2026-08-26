package com.learning.jdk8;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * JDK 8: java.time (JSR 310), a modern, immutable date/time API replacing the
 * mutable, thread-unsafe java.util.Date/Calendar.
 */
public class DateTimeDemo {

    public int ageInYears(LocalDate birthDate, LocalDate today) {
        return Period.between(birthDate, today).getYears();
    }

    public LocalDate nextBirthday(LocalDate birthDate, LocalDate today) {
        LocalDate thisYearsBirthday = birthDate.withYear(today.getYear());
        return thisYearsBirthday.isBefore(today) ? thisYearsBirthday.plusYears(1) : thisYearsBirthday;
    }

    public static void main(String[] args) {
        localDateBasics();
        localTimeAndLocalDateTime();
        periodVsDuration();
        formattingAndParsing();
        zonedDateTimeAndInstant();
        temporalAdjusters();
    }

    private static void localDateBasics() {
        LocalDate today = LocalDate.now();
        LocalDate specificDate = LocalDate.of(2026, 8, 27);
        LocalDate parsed = LocalDate.parse("2026-12-25"); // ISO-8601 by default

        System.out.println("LocalDate.now(): " + today);
        System.out.println("LocalDate.of(2026, 8, 27): " + specificDate);
        System.out.println("LocalDate.parse(\"2026-12-25\"): " + parsed);
        System.out.println("specificDate.getDayOfWeek(): " + specificDate.getDayOfWeek());
        System.out.println("specificDate.plusDays(10): " + specificDate.plusDays(10));
        System.out.println("specificDate.plusMonths(1).minusDays(1): " + specificDate.plusMonths(1).minusDays(1));
        System.out.println("specificDate.isLeapYear(): " + specificDate.isLeapYear());

        // LocalDate is immutable: every "mutator" returns a new instance, the original is untouched.
        LocalDate original = LocalDate.of(2000, 1, 1);
        LocalDate shifted = original.plusYears(1);
        System.out.println("original stays " + original + " after original.plusYears(1) -> " + shifted);
    }

    private static void localTimeAndLocalDateTime() {
        LocalTime time = LocalTime.of(14, 30, 15);
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(2026, 8, 27), time);

        System.out.println("LocalTime.of(14, 30, 15): " + time);
        System.out.println("LocalDateTime combining a LocalDate and LocalTime: " + dateTime);
        System.out.println("dateTime.plusHours(10): " + dateTime.plusHours(10));
        System.out.println("dateTime.toLocalDate(): " + dateTime.toLocalDate());
        System.out.println("dateTime.toLocalTime(): " + dateTime.toLocalTime());
    }

    private static void periodVsDuration() {
        LocalDate birthDate = LocalDate.of(1995, 3, 12);
        LocalDate today = LocalDate.of(2026, 8, 27);

        // Period: date-based amount (years/months/days) -- for calendar-style differences.
        Period period = Period.between(birthDate, today);
        System.out.println("Period.between(birthDate, today): " + period.getYears() + "y "
                + period.getMonths() + "m " + period.getDays() + "d");

        DateTimeDemo demo = new DateTimeDemo();
        System.out.println("ageInYears(birthDate, today): " + demo.ageInYears(birthDate, today));
        System.out.println("nextBirthday(birthDate, today): " + demo.nextBirthday(birthDate, today));

        // Duration: time-based amount (hours/minutes/seconds/nanos) -- for clock-style differences.
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 30);
        Duration workDay = Duration.between(start, end);
        System.out.println("Duration.between(09:00, 17:30): " + workDay.toHours() + "h "
                + (workDay.toMinutes() % 60) + "m (total minutes=" + workDay.toMinutes() + ")");
    }

    private static void formattingAndParsing() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 27, 14, 30);

        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

        System.out.println("ISO_LOCAL_DATE_TIME.format(dateTime): " + isoFormatter.format(dateTime));
        System.out.println("ofPattern(\"dd-MMM-yyyy HH:mm\").format(dateTime): " + customFormatter.format(dateTime));

        LocalDateTime parsedBack = LocalDateTime.parse("27-Aug-2026 14:30", customFormatter);
        System.out.println("parsing that same string back with the custom formatter: " + parsedBack);
    }

    private static void zonedDateTimeAndInstant() {
        ZonedDateTime inIndia = ZonedDateTime.of(LocalDateTime.of(2026, 8, 27, 20, 0), ZoneId.of("Asia/Kolkata"));
        ZonedDateTime inNewYork = inIndia.withZoneSameInstant(ZoneId.of("America/New_York"));

        System.out.println("ZonedDateTime in Asia/Kolkata: " + inIndia);
        System.out.println("withZoneSameInstant(America/New_York): " + inNewYork);

        // Instant: a point on the UTC timeline, independent of any zone/calendar -- good for timestamps.
        Instant now = Instant.now();
        Instant tenMinutesLater = now.plus(Duration.ofMinutes(10));
        System.out.println("Instant.now(): " + now);
        System.out.println("now.plus(Duration.ofMinutes(10)): " + tenMinutesLater);
        System.out.println("ChronoUnit.SECONDS.between(now, tenMinutesLater): "
                + ChronoUnit.SECONDS.between(now, tenMinutesLater));
    }

    private static void temporalAdjusters() {
        LocalDate someWednesday = LocalDate.of(2026, 8, 26);

        LocalDate firstDayOfMonth = someWednesday.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = someWednesday.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate nextMonday = someWednesday.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate firstMondayOfNextMonth = someWednesday.with(TemporalAdjusters.firstDayOfNextMonth())
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        System.out.println("TemporalAdjusters.firstDayOfMonth(): " + firstDayOfMonth);
        System.out.println("TemporalAdjusters.lastDayOfMonth(): " + lastDayOfMonth);
        System.out.println("TemporalAdjusters.next(MONDAY): " + nextMonday);
        System.out.println("first Monday of next month: " + firstMondayOfNextMonth);
    }
}
