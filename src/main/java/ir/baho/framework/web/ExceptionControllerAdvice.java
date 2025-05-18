package ir.baho.framework.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoWriteException;
import ir.baho.framework.exception.ConflictException;
import ir.baho.framework.exception.DependencyException;
import ir.baho.framework.exception.Error;
import ir.baho.framework.exception.FieldError;
import ir.baho.framework.exception.HttpError;
import ir.baho.framework.exception.InvalidFileException;
import ir.baho.framework.exception.MetadataConstraintAccessException;
import ir.baho.framework.exception.MetadataConvertException;
import ir.baho.framework.exception.MetadataFieldAccessException;
import ir.baho.framework.exception.NotFoundException;
import ir.baho.framework.exception.NotModifiedException;
import ir.baho.framework.exception.ObjectError;
import ir.baho.framework.exception.PreconditionFailedException;
import ir.baho.framework.exception.ServiceUnavailableException;
import ir.baho.framework.i18n.MessageResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.PropertyMatches;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoConfiguration
@ControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor
@Slf4j
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    private final MessageResource messageResource;
    private final ObjectMapper objectMapper;

    @ResponseBody
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<?> handleHttpClientErrorException(HttpClientErrorException e) {
        try {
            return ResponseEntity.status(e.getStatusCode()).body(objectMapper.readTree(e.getResponseBodyAsString()));
        } catch (JsonProcessingException ex) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateKeyException.class)
    public HttpError handleDuplicateKeyException() {
        return new HttpError(HttpStatus.CONFLICT, messageResource.getMessage("entity.duplicate.key"));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public HttpError handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        try {
            Class.forName("org.hibernate.exception.ConstraintViolationException");
            if (e.getCause() instanceof ConstraintViolationException exception) {
                String name = exception.getConstraintName();
                if (name.contains(".")) {
                    name = name.substring(name.lastIndexOf('.') + 1);
                }
                return new HttpError(HttpStatus.CONFLICT, messageResource.getMessageOrDefault(name.toUpperCase(),
                        messageResource.getMessage("entity.integrity.violation")));
            }
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("com.mongodb.MongoWriteException");
            if (e.getCause() instanceof MongoWriteException exception) {
                return new HttpError(HttpStatus.CONFLICT, exception.getMessage(), new Error(exception.getError().getMessage()));
            }
        } catch (ClassNotFoundException ignored) {
        }
        return new HttpError(HttpStatus.CONFLICT, e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public HttpError handleOptimisticLockingFailureException() {
        return new HttpError(HttpStatus.CONFLICT, messageResource.getMessage("entity.concurrent.modify"));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public HttpError handleConflictException(ConflictException e) {
        List<Error> errors = e.getKeyParams().entrySet().stream()
                .map(entry -> new Error(messageResource.getMessageOrDefault(entry.getKey(), entry.getKey(), entry.getValue().toArray(Object[]::new))))
                .toList();
        return new HttpError(HttpStatus.CONFLICT, e.getMessage(), errors);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
    @ExceptionHandler(DependencyException.class)
    public HttpError handleDependencyException(DependencyException e) {
        List<Error> errors = e.getKeyParams().entrySet().stream()
                .map(entry -> new Error(messageResource.getMessageOrDefault(entry.getKey(), entry.getKey(), entry.getValue().toArray(Object[]::new))))
                .toList();
        return new HttpError(HttpStatus.FAILED_DEPENDENCY, e.getMessage(), errors);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public HttpError handleIncorrectResultSizeDataAccessException(IncorrectResultSizeDataAccessException e) {
        return new HttpError(HttpStatus.FAILED_DEPENDENCY, messageResource.getMessage("entity.incorrect.size", e.getExpectedSize()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public HttpError handleEmptyResultDataAccessException() {
        return new HttpError(HttpStatus.NOT_FOUND, messageResource.getMessage("entity.not.found"));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public HttpError handleNotFoundException(NotFoundException e) {
        if (e.getKeyParams() == null) {
            return new HttpError(HttpStatus.NOT_FOUND, messageResource.getMessage("entity.not.found"));
        }
        List<Error> errors = e.getKeyParams().entrySet().stream()
                .map(entry -> new Error(messageResource.getMessageOrDefault(entry.getKey(), entry.getKey(), entry.getValue().toArray(Object[]::new))))
                .toList();
        return new HttpError(HttpStatus.NOT_FOUND, e.getMessage(), errors);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ExceptionHandler(InvalidFileException.class)
    public HttpError handleInvalidFileException(InvalidFileException e) {
        if (e.getKeyParams() == null) {
            return new HttpError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, messageResource.getMessage("invalid.file"));
        }
        List<Error> errors = e.getKeyParams().entrySet().stream()
                .map(entry -> new Error(messageResource.getMessageOrDefault(entry.getKey(), entry.getKey(), entry.getValue().toArray(Object[]::new))))
                .toList();
        return new HttpError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage(), errors);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_MODIFIED)
    @ExceptionHandler(NotModifiedException.class)
    public void handleNotModifiedException() {
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ExceptionHandler(PreconditionFailedException.class)
    public HttpError handlePreconditionFailedException(PreconditionFailedException e) {
        return new HttpError(HttpStatus.PRECONDITION_FAILED, messageResource.getMessage("entity.current.version", e.getVersion()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public HttpError handleConstraintViolationException(jakarta.validation.ConstraintViolationException e) {
        return new HttpError(HttpStatus.BAD_REQUEST, e.getMessage(), e.getConstraintViolations().stream()
                .map(violation -> new FieldError(violation.getMessage(),
                        violation.getRootBeanClass().getSimpleName(), violation.getPropertyPath().toString(),
                        violation.getInvalidValue(), violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()))
                .collect(Collectors.toList()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PropertyReferenceException.class)
    public HttpError handlePropertyReferenceException(PropertyReferenceException e) {
        String name = e.getPropertyName();
        String type = e.getType().getType().getSimpleName();
        StringBuilder builder = new StringBuilder(messageResource.getMessage("property.not.found", name, type));

        String[] potentialMatches = Stream.of(PropertyMatches.forField(e.getPropertyName(), e.getType().getType()).getPossibleMatches(),
                        PropertyMatches.forProperty(e.getPropertyName(), e.getType().getType()).getPossibleMatches()).flatMap(Stream::of)
                .distinct().toArray(String[]::new);
        if (potentialMatches.length > 0) {
            String matches = StringUtils.arrayToCommaDelimitedString(potentialMatches);
            builder.append(" ").append(messageResource.getMessage("property.potential.matches", matches));
            name = potentialMatches[0];
        }

        String message = builder.toString();
        return new HttpError(HttpStatus.BAD_REQUEST, e.getMessage(), new FieldError(message, type, name, e.getPropertyName(), potentialMatches));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MetadataConvertException.class)
    public HttpError handleMetadataConvertException(MetadataConvertException e) {
        String name = e.getPropertyName();
        String type = e.getType().getSimpleName();
        String message = messageResource.getMessage("metadata.convert.error", name);
        return new HttpError(HttpStatus.BAD_REQUEST, e.getMessage(), new FieldError(message, type, name, e.getValue()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MetadataFieldAccessException.class)
    public HttpError handleMetadataFieldAccessException(MetadataFieldAccessException e) {
        String name = e.getPropertyName();
        String type = e.getType().getSimpleName();
        String message = messageResource.getMessage("metadata.field.access", name);
        return new HttpError(HttpStatus.BAD_REQUEST, e.getMessage(), new FieldError(message, type, name, null));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MetadataConstraintAccessException.class)
    public HttpError handleMetadataConstraintAccessException(MetadataConstraintAccessException e) {
        String name = e.getPropertyName();
        String type = e.getType().getSimpleName();
        String message = messageResource.getMessage("metadata.constraint.access", e.getConstraint(), name);
        return new HttpError(HttpStatus.BAD_REQUEST, e.getMessage(), new FieldError(message, type, name, e.getConstraint()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMediaTypeException.class)
    public HttpError handleHttpMediaTypeException(HttpMediaTypeException e) {
        return new HttpError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public HttpError handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return new HttpError(HttpStatus.BAD_REQUEST, e.getMessage(), new FieldError(e.getMessage(),
                e.getParameter().getContainingClass().getSimpleName(), e.getName(), e.getValue()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MultipartException.class)
    public HttpError handleMissingServletRequestPartException(MultipartException e) {
        return new HttpError(HttpStatus.BAD_REQUEST, e.getMessage(), new Error(e.getMessage()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageConversionException.class)
    public HttpError handleHttpMessageConversionException(HttpMessageConversionException e) {
        return new HttpError(HttpStatus.BAD_REQUEST, e.getMessage(), new Error(e.getMessage()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ServiceUnavailableException.class)
    public HttpError handleException(ServiceUnavailableException e) {
        log.error(e.getMessage(), e);
        return new HttpError(HttpStatus.SERVICE_UNAVAILABLE, messageResource.getMessage("service.unavailable", e.getService()));
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public HttpError handleException(Exception e) {
        log.error(e.getMessage(), e);
        return new HttpError(HttpStatus.INTERNAL_SERVER_ERROR, messageResource.getMessage("internal.server.error"));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<Error> errors = new ArrayList<>();
        e.getAllErrors().forEach(error -> {
            if (error instanceof org.springframework.validation.FieldError fe) {
                String name = fe.getObjectName();
                String key = StringUtils.endsWithIgnoreCase(name, "dto") ?
                        (name.substring(0, name.length() - 3) + "." + fe.getField()) : (name + "." + fe.getField());
                errors.add(new FieldError(fe.getDefaultMessage(), fe.getObjectName(),
                        messageResource.getMessageOrDefault(StringUtils.capitalize(key), fe.getField()), fe.getRejectedValue(), fe.getCodes()));
            } else {
                errors.add(new ObjectError(error.getDefaultMessage(), error.getObjectName()));
            }
        });
        return ResponseEntity.badRequest().body(new HttpError(HttpStatus.BAD_REQUEST, e.getMessage(), errors));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(new HttpError(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

}
