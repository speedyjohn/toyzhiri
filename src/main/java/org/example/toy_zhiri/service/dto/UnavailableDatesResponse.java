package org.example.toy_zhiri.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnavailableDatesResponse {

    private List<LocalDate> blockedByPartner;

    private List<LocalDate> bookedDates;

    private List<LocalDate> allUnavailableDates;
}