package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.dto.ReservationResponse;
import com.carrental.enums.CarType;
import com.carrental.exception.CarNotAvailableException;
import com.carrental.model.Car;
import com.carrental.model.Renter;
import com.carrental.model.Reservation;
import com.carrental.repository.CarRepository;
import com.carrental.repository.RenterRepository;
import com.carrental.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private CarRepository carRepository;
    @Mock
    private RenterRepository renterRepository;
    @Mock
    private ReservationRepository reservationRepository;

    private ReservationService service;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 6, 1, 12, 0);
    private static final LocalDateTime VALID_START = FIXED_NOW.plusDays(1);
    private static final int VALID_DAYS = 3;

    @BeforeEach
    void setUp() {
        final Clock fixedClock = Clock.fixed(
                FIXED_NOW.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        service = new ReservationService(carRepository, renterRepository, reservationRepository, fixedClock);
    }

    @Test
    @DisplayName("should create reservation and return response")
    void shouldCreateReservation() {
        Car car = testCar();
        Renter renter = testRenter();
        LocalDateTime endDate = VALID_START.plusDays(VALID_DAYS);
        Reservation saved = Reservation.builder()
                .id(UUID.randomUUID())
                .car(car)
                .renter(renter)
                .startDate(VALID_START)
                .endDate(endDate)
                .build();

        when(carRepository.findAvailableCar(eq(CarType.SEDAN), eq(VALID_START), eq(endDate)))
                .thenReturn(Optional.of(car));
        when(renterRepository.findByEmail("john@example.com")).thenReturn(Optional.of(renter));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(saved);

        ReservationResponse response = service.createReservation(validRequest());

        assertThat(response.reservationId()).isEqualTo(saved.getId());
        assertThat(response.carType()).isEqualTo(CarType.SEDAN);
        assertThat(response.carModel()).isEqualTo("Toyota Camry");
        assertThat(response.startDate()).isEqualTo(VALID_START);
        assertThat(response.endDate()).isEqualTo(endDate);

        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("should create new renter when email not found")
    void shouldCreateNewRenter() {
        Car car = testCar();
        Renter newRenter = testRenter();
        LocalDateTime endDate = VALID_START.plusDays(VALID_DAYS);

        when(carRepository.findAvailableCar(any(), any(), any())).thenReturn(Optional.of(car));
        when(renterRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(renterRepository.save(any(Renter.class))).thenReturn(newRenter);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(
                Reservation.builder().id(UUID.randomUUID()).car(car).renter(newRenter)
                        .startDate(VALID_START).endDate(endDate).build()
        );

        service.createReservation(validRequest());

        verify(renterRepository).save(any(Renter.class));
    }

    @Test
    @DisplayName("should reuse existing renter when email already exists")
    void shouldReuseExistingRenter() {
        Car car = testCar();
        Renter existingRenter = testRenter();
        LocalDateTime endDate = VALID_START.plusDays(VALID_DAYS);

        when(carRepository.findAvailableCar(any(), any(), any())).thenReturn(Optional.of(car));
        when(renterRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingRenter));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(
                Reservation.builder().id(UUID.randomUUID()).car(car).renter(existingRenter)
                        .startDate(VALID_START).endDate(endDate).build()
        );

        service.createReservation(validRequest());

        verify(renterRepository, never()).save(any(Renter.class));
    }

    @Test
    @DisplayName("should reject when start date is less than 1 hour from now")
    void shouldRejectStartDateTooSoon() {
        ReservationRequest tooSoon = new ReservationRequest(
                "John", "john@example.com", CarType.SEDAN,
                FIXED_NOW.plusMinutes(30), VALID_DAYS
        );

        assertThatThrownBy(() -> service.createReservation(tooSoon))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 1 hour in advance");

        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("should reject when rental period exceeds 90 days")
    void shouldRejectRentalPeriodOver90Days() {
        ReservationRequest tooLong = new ReservationRequest(
                "John", "john@example.com", CarType.SUV,
                VALID_START, 91
        );

        assertThatThrownBy(() -> service.createReservation(tooLong))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("90 days");

        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("should accept exactly 90 days rental period")
    void shouldAcceptExactly90Days() {
        Car car = testCar();
        Renter renter = testRenter();
        LocalDateTime endDate = VALID_START.plusDays(90);

        when(carRepository.findAvailableCar(eq(CarType.SEDAN), eq(VALID_START), eq(endDate)))
                .thenReturn(Optional.of(car));
        when(renterRepository.findByEmail(any())).thenReturn(Optional.of(renter));
        when(reservationRepository.save(any())).thenReturn(
                Reservation.builder().id(UUID.randomUUID()).car(car).renter(renter)
                        .startDate(VALID_START).endDate(endDate).build()
        );

        ReservationRequest exactly90 = new ReservationRequest(
                "John", "john@example.com", CarType.SEDAN, VALID_START, 90
        );

        ReservationResponse response = service.createReservation(exactly90);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("should throw CarNotAvailableException when no car of requested type is free")
    void shouldThrowWhenNoCarsAvailable() {
        when(carRepository.findAvailableCar(eq(CarType.VAN), any(), any()))
                .thenReturn(Optional.empty());

        ReservationRequest vanRequest = new ReservationRequest(
                "Jane", "jane@example.com", CarType.VAN, VALID_START, VALID_DAYS
        );

        assertThatThrownBy(() -> service.createReservation(vanRequest))
                .isInstanceOf(CarNotAvailableException.class)
                .hasMessageContaining("VAN");

        verifyNoInteractions(reservationRepository);
    }

    private ReservationRequest validRequest() {
        return new ReservationRequest("John Doe", "john@example.com", CarType.SEDAN, VALID_START, VALID_DAYS);
    }

    private Car testCar() {
        return Car.builder().id(UUID.randomUUID()).type(CarType.SEDAN).model("Toyota Camry").build();
    }

    private Renter testRenter() {
        return Renter.builder().id(UUID.randomUUID()).name("John Doe").email("john@example.com").build();
    }
}
