package com.ercode.productdescription.adapter.in.web;

import com.ercode.productdescription.application.LlmUnavailableException;
import com.ercode.productdescription.domain.IncompleteDescriptionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps domain/application/validation failures to RFC 7807 problem responses. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");
        problem.setDetail(ex.getBindingResult().getAllErrors().stream()
                .map(e -> e.getDefaultMessage() == null ? e.toString() : e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Invalid request"));
        return problem;
    }

    @ExceptionHandler(IncompleteDescriptionException.class)
    public ProblemDetail onIncomplete(IncompleteDescriptionException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setTitle("Incomplete description");
        problem.setDetail(ex.getMessage());
        problem.setProperty("missingSections", ex.missingSections());
        return problem;
    }

    @ExceptionHandler(LlmUnavailableException.class)
    public ProblemDetail onLlmUnavailable(LlmUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        problem.setTitle("LLM gateway error");
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
