package com.sankalp.marketservice.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sankalp.marketservice.dto.MarketStatusResponse;

@Service
public class MarketHoursService {

	private final ZoneId marketZone;
	private final LocalTime openTime;
	private final LocalTime closeTime;
	private final Set<LocalDate> holidays;

	public MarketHoursService(
			@Value("${market.timezone:Asia/Kolkata}") String timezone,
			@Value("${market.open-time:09:15}") String openTime,
			@Value("${market.close-time:15:30}") String closeTime,
			@Value("${market.holidays:}") String holidayCsv) {
		this.marketZone = ZoneId.of(timezone);
		this.openTime = LocalTime.parse(openTime);
		this.closeTime = LocalTime.parse(closeTime);
		this.holidays = parseHolidays(holidayCsv);
	}

	public ZoneId marketZone() {
		return marketZone;
	}

	public boolean isMarketOpen(ZonedDateTime time) {
		ZonedDateTime normalized = time.withZoneSameInstant(marketZone);
		if (!isTradingDay(normalized.toLocalDate())) {
			return false;
		}

		LocalTime localTime = normalized.toLocalTime();
		return !localTime.isBefore(openTime) && !localTime.isAfter(closeTime);
	}

	public MarketStatusResponse getCurrentStatus() {
		ZonedDateTime now = ZonedDateTime.now(marketZone);
		boolean open = isMarketOpen(now);
		String reason = resolveReason(now, open);
		LocalDateTime nextOpen = open ? now.toLocalDateTime() : nextOpenFrom(now).toLocalDateTime();

		return new MarketStatusResponse(
				open,
				marketZone.getId(),
				now.toLocalDate(),
				now.toLocalDateTime(),
				openTime,
				closeTime,
				reason,
				nextOpen);
	}

	public boolean isTradingDay(LocalDate date) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		return dayOfWeek != DayOfWeek.SATURDAY
				&& dayOfWeek != DayOfWeek.SUNDAY
				&& !holidays.contains(date);
	}

	private String resolveReason(ZonedDateTime now, boolean open) {
		if (open) {
			return "OPEN";
		}

		if (!isTradingDay(now.toLocalDate())) {
			return "HOLIDAY_OR_WEEKEND";
		}

		if (now.toLocalTime().isBefore(openTime)) {
			return "PRE_OPEN";
		}

		return "POST_CLOSE";
	}

	private ZonedDateTime nextOpenFrom(ZonedDateTime from) {
		LocalDate date = from.toLocalDate();
		LocalTime time = from.toLocalTime();

		if (isTradingDay(date) && time.isBefore(openTime)) {
			return ZonedDateTime.of(date, openTime, marketZone);
		}

		do {
			date = date.plusDays(1);
		} while (!isTradingDay(date));

		return ZonedDateTime.of(date, openTime, marketZone);
	}

	private Set<LocalDate> parseHolidays(String holidayCsv) {
		if (holidayCsv == null || holidayCsv.isBlank()) {
			return Set.of();
		}

		Set<LocalDate> parsed = new HashSet<>();
		Arrays.stream(holidayCsv.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(LocalDate::parse)
				.forEach(parsed::add);
		return parsed;
	}
}
