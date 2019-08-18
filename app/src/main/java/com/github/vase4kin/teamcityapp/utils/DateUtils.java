/*
 * Copyright 2019 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.utils;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Date utils class
 */
public class DateUtils {

    private static final String DATE_PARSE_PATTERN = "yyyyMMdd'T'HHmmss";
    private static final String BUILD_START_DATE_FORMAT_PATTERN = "dd MMM yy HH:mm";
    private static final String BUILD_START_DATE = "dd MMMM";
    private static final String BUILD_FINISHED_DATE_FORMAT_TITLE = "HH:mm";

    private Date startDate;
    private Date finishedDate;

    private DateUtils(Date startDate) {
        this.startDate = startDate;
    }

    private DateUtils(Date startDate, Date finishedDate) {
        this.startDate = startDate;
        this.finishedDate = finishedDate;
    }

    /**
     * Init date utils with date in UTC format
     *
     * @param startDate
     * @return
     */
    public static DateUtils initWithDate(String startDate) {
        Date parsedStartDate = parseDate(startDate);
        return new DateUtils(parsedStartDate);
    }

    /**
     * Init date utils with start and finished in UTC format
     *
     * @param startDate
     * @return
     */
    public static DateUtils initWithDate(String startDate, String finishedDate) {
        Date parsedStartDate = parseDate(startDate);
        Date parsedFinishedDate = parseDate(finishedDate);
        return new DateUtils(parsedStartDate, parsedFinishedDate);
    }

    /**
     * Parse date
     *
     * @param date - Date to parse
     * @return Date
     */
    private static Date parseDate(String date) {
        SimpleDateFormat df = DateUtils.getSimpleDateFormat(DATE_PARSE_PATTERN);
        try {
            return df.parse(date);
        } catch (ParseException e) {
            // Just return today time
            return DateTime.now().toDate();
        }
    }

    /**
     * Format start date to build title
     *
     * @return Formatted date
     */
    public String formatStartDateToBuildTitle() {
        //20 Jun 15 12:05
        SimpleDateFormat format1 = DateUtils.getSimpleDateFormat(BUILD_START_DATE_FORMAT_PATTERN);
        return format1.format(startDate);
    }

    /**
     * Format date to build list header
     *
     * @return Formatted date
     */
    public String formatStartDateToBuildListItemHeader() {
        //20 June
        SimpleDateFormat format1 = DateUtils.getSimpleDateFormat(BUILD_START_DATE);
        return format1.format(startDate);
    }

    /**
     * Format finished data
     *
     * @return Formatted date
     */
    private String formatFinishedDate() {
        // Build can end in the next day
        // 20 Apr 15 16:14:28 - 21 Apr 15 16:14:46 (18s)
        SimpleDateFormat format1 = DateUtils.getSimpleDateFormat(BUILD_FINISHED_DATE_FORMAT_TITLE);
        return format1.format(finishedDate);
    }

    /**
     * Format date to Overview screen to format "20 Apr 15 16:14:28 - 16:14:46 (18s)"
     */
    public String formatDateToOverview() {
        String formattedStartDate = formatStartDateToBuildTitle();
        String formattedFinishedDate = formatFinishedDate();

        Duration duration = new Duration(new DateTime(finishedDate).minus(startDate.getTime()).getMillis());
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix("d:")
                .appendHours()
                .appendSuffix("h:")
                .appendMinutes()
                .appendSuffix("m:")
                .appendSeconds()
                .appendSuffix("s")
                .toFormatter();
        String formatted = formatter.print(duration.toPeriod());
        return String.format("%s - %s (%s)", formattedStartDate, formattedFinishedDate, formatted);
    }

    /**
     * Get simple date format instance
     *
     * @param pattern - Patter to make {@link SimpleDateFormat}
     * @return
     */
    private static SimpleDateFormat getSimpleDateFormat(String pattern) {
        return new SimpleDateFormat(pattern, Locale.US);
    }
}
