package ase.cogniprice.controller.global.exception.handler;

import ase.cogniprice.exception.ConflictException;
import ase.cogniprice.exception.DomainValidationException;
import ase.cogniprice.exception.EmailAlreadyExistsException;
import ase.cogniprice.exception.ExpiredTokenException;
import ase.cogniprice.exception.ForbiddenException;
import ase.cogniprice.exception.GtinLookupException;
import ase.cogniprice.exception.InvalidPasswordException;
import ase.cogniprice.exception.InvalidUrlException;
import ase.cogniprice.exception.NotFoundException;
import ase.cogniprice.exception.StoreProductAlreadyExistsException;
import ase.cogniprice.exception.UrlNotReachableException;
import ase.cogniprice.exception.UserLockedException;
import ase.cogniprice.exception.UsernameAlreadyExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Register all your Java exceptions here to map them into meaningful HTTP exceptions.
 * If you have special cases which are only important for specific endpoints, use ResponseStatusExceptions
 * https://www.baeldung.com/exception-handling-for-rest-with-spring#responsestatusexception
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbiddenException(ForbiddenException ex) {
        LOG.warn("Forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        LOG.error("Access Denied: {}", ex.getMessage());
        return new ResponseEntity<>("Access Denied: " + ex.getMessage(), HttpStatus.UNAUTHORIZED); // 401 Unauthorized
    }

    // Handle AuthenticationException (for failed authentication)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) {
        LOG.error("Authentication Failed: {}", ex.getMessage());
        return new ResponseEntity<>("Authentication Failed: " + ex.getMessage(), HttpStatus.UNAUTHORIZED); // 401 Unauthorized
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        LOG.error("Entity not found: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // 404
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        LOG.error("Illegal argument: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // 400
    }

    @ExceptionHandler(GtinLookupException.class)
    public ResponseEntity<String> handleGtinLookup(GtinLookupException ex) {
        LOG.error("GTIN lookup error: {}", ex.getMessage());
        return new ResponseEntity<>("Error with GTIN lookup: " + ex.getMessage(), HttpStatus.BAD_REQUEST); // 400
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<String> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        LOG.warn("Username already exists: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<String> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        LOG.warn("Email already exists: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<String> handleInvalidPassword(InvalidPasswordException ex) {
        LOG.debug("Invalid password for user: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED); // 401 Unauthorized
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException ex) {
        LOG.debug("User not found: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED); // 401 Unauthorized
    }

    @ExceptionHandler(UserLockedException.class)
    public ResponseEntity<String> handleUserLocked(UserLockedException ex) {
        LOG.debug("User account locked: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.LOCKED); // 423 Locked
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElement(NoSuchElementException ex) {
        LOG.warn("Element not found: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
    }

    @ExceptionHandler(StoreProductAlreadyExistsException.class)
    public ResponseEntity<String> handleStoreProductAlreadyExists(StoreProductAlreadyExistsException ex) {
        LOG.warn("StoreProductAlreadyExists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<String> handleInvalidUrl(InvalidUrlException ex) {
        LOG.warn("Url was invalid: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // 404 Not Found
    }

    @ExceptionHandler(UrlNotReachableException.class)
    public ResponseEntity<String> handleUrlNotReachable(UrlNotReachableException ex) {
        LOG.warn("Url was not reachable: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST); // 404 Not Found
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<String> handleDomainValidation(DomainValidationException ex) {
        LOG.warn("DomainValidation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflict(ConflictException ex) {
        LOG.warn("Conflict: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<String> handleExpiredTokenException(ExpiredTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleExpiredTokenException(Exception ex) {
        LOG.warn("InternalServerError: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server error");
    }

    /**
     * Override methods from ResponseEntityExceptionHandler to send a customized HTTP response for a know exception
     * from e.g. Spring
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        LOG.warn(ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        //Get all errors
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .collect(Collectors.toList());
        body.put("Validation errors", errors);

        return new ResponseEntity<>(body.toString(), headers, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /* Override Exception ConstrainViolationException for batch import */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        LOG.warn("Validation error: {}", ex.getMessage());

        String firstErrorMessage = ex.getConstraintViolations()
                .stream()
                .findFirst() // Get the first validation error
                .map(violation -> violation.getMessage()) // Get the error message
                .orElse("Validation error"); // Fallback message in case no violations are found

        // Create a response body
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        responseBody.put("error", firstErrorMessage);

        return new ResponseEntity<>(responseBody, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
