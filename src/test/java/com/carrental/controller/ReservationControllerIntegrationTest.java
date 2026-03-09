package com.carrental.controller;

import com.carrental.AbstractIntegrationTest;
import com.carrental.dto.ReservationRequest;
import com.carrental.enums.CarType;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Sql(scripts = {"/clear-db.sql", "/controller/ReservationControllerTest.sql"})
class ReservationControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1/reservations";
    private static final LocalDateTime START = LocalDateTime.now().plusDays(1);
    private static final int DAYS = 3;

    @Test
    void shouldCreateReservationSuccessfully() {
        given()
                .contentType(ContentType.JSON)
                .body(buildRequest(CarType.SEDAN, START))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("reservationId", notNullValue())
                .body("carType", equalTo("SEDAN"))
                .body("carModel", notNullValue());
    }

    @Test
    void shouldReturn409WhenNoCarAvailable() {
        given().contentType(ContentType.JSON)
                .body(buildRequest(CarType.SEDAN, START))
                .post(BASE_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value());

        given()
                .contentType(ContentType.JSON)
                .body(buildRequest(CarType.SEDAN, START, "hektor@example.com"))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("message", containsString("SEDAN"));
    }

    @Test
    void shouldReturn400WhenStartDateIsInPast() {
        given()
                .contentType(ContentType.JSON)
                .body(buildRequest(CarType.SUV, LocalDateTime.now().minusDays(1)))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void shouldReturn400WhenCarTypeIsNull() {
        given()
                .contentType(ContentType.JSON)
                .body(buildRequest(null, START))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("validationErrors.carType", notNullValue());
    }

    @Test
    void shouldReturn400WhenNumberOfDaysIsNullOrZero() {
        given()
                .contentType(ContentType.JSON)
                .body(new ReservationRequest("John", "john@example.com", CarType.SUV, START, null))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("validationErrors.numberOfDays", notNullValue());
    }

    @Test
    void shouldReuseExistingRenterOnSecondReservation() {
        given().contentType(ContentType.JSON)
                .body(buildRequest(CarType.SEDAN, START))
                .post(BASE_URL);

        given()
                .contentType(ContentType.JSON)
                .body(buildRequest(CarType.SUV, START.plusDays(5)))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(HttpStatus.CREATED.value());
    }

    private ReservationRequest buildRequest(CarType carType, LocalDateTime start) {
        return buildRequest(carType, start, "john@example.com");
    }

    private ReservationRequest buildRequest(CarType carType, LocalDateTime start, String email) {
        return new ReservationRequest("John Doe", email, carType, start, ReservationControllerIntegrationTest.DAYS);
    }
}