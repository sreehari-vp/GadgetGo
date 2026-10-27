package com.gadgetgo.api;

import com.gadgetgo.customer.service.CustomerConflictException;
import com.gadgetgo.customer.service.InvalidCredentialsException;
import com.gadgetgo.gadget.service.GadgetNotFoundException;
import com.gadgetgo.gadget.service.InvalidGadgetException;
import com.gadgetgo.booking.service.BookingNotFoundException;
import com.gadgetgo.booking.service.InvalidBookingException;
import com.gadgetgo.booking.service.InvalidOfferStateException;
import com.gadgetgo.booking.service.InvalidRepairStateException;
import com.gadgetgo.booking.service.OfferNotFoundException;
import com.gadgetgo.booking.service.UnauthorizedJobAccessException;
import com.gadgetgo.security.ProviderProfileNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = {"com.gadgetgo.customer", "com.gadgetgo.gadget", "com.gadgetgo.technician", "com.gadgetgo.supplier", "com.gadgetgo.booking"})
public class ApiExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ProblemDetail> bookingNotFound(BookingNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ProblemDetail> invalidBooking(InvalidBookingException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(OfferNotFoundException.class)
    public ResponseEntity<ProblemDetail> offerNotFound(OfferNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidOfferStateException.class)
    public ResponseEntity<ProblemDetail> invalidOfferState(InvalidOfferStateException exception) {
        return problem(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidRepairStateException.class)
    public ResponseEntity<ProblemDetail> invalidRepairState(InvalidRepairStateException exception) {
        return problem(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedJobAccessException.class)
    public ResponseEntity<ProblemDetail> unauthorizedJobAccess(UnauthorizedJobAccessException exception) {
        return problem(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(ProviderProfileNotFoundException.class)
    public ResponseEntity<ProblemDetail> providerProfileNotFound(ProviderProfileNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(CustomerConflictException.class)
    public ResponseEntity<ProblemDetail> customerConflict(CustomerConflictException exception) {
        return problem(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> invalidCredentials(InvalidCredentialsException exception) {
        return problem(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(GadgetNotFoundException.class)
    public ResponseEntity<ProblemDetail> gadgetNotFound(GadgetNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidGadgetException.class)
    public ResponseEntity<ProblemDetail> invalidGadget(InvalidGadgetException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> invalidRequest(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid.");
        detail.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ProblemDetail> invalidInput(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "The request contains invalid input.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> dataConflict(DataIntegrityViolationException exception) {
        return problem(HttpStatus.CONFLICT, "The requested change conflicts with existing data.");
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String message) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, message);
        return ResponseEntity.status(status).body(body);
    }
}
