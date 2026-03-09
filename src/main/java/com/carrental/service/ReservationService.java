package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.dto.ReservationResponse;
import com.carrental.exception.CarNotAvailableException;
import com.carrental.model.Car;
import com.carrental.model.Renter;
import com.carrental.model.Reservation;
import com.carrental.repository.CarRepository;
import com.carrental.repository.RenterRepository;
import com.carrental.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final CarRepository carRepository;
    private final RenterRepository renterRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public ReservationResponse createReservation(final ReservationRequest request) {
        validateRentalPeriod(request.startDate(), request.endDate());

        final Car car = carRepository.findAvailableCar(request.carType(), request.startDate(), request.endDate())
                .orElseThrow(() -> new CarNotAvailableException("No " + request.carType() + " available for these dates."));

        final Renter renter = renterRepository.findByEmail(request.renterEmail())
                .orElseGet(() -> renterRepository.save(
                        Renter.builder().name(request.renterName()).email(request.renterEmail()).build()
                ));

        final Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .car(car)
                        .renter(renter)
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .build()
        );

        return ReservationResponse.from(reservation);
    }

    private void validateRentalPeriod(final LocalDateTime startDate, final LocalDateTime endDate) {
        if (startDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("Reservation must be made at least 1 hour in advance");
        }
        if (endDate.isAfter(startDate.plusDays(90))) {
            throw new IllegalArgumentException("Maximum rental period is 90 days");
        }
    }
}
