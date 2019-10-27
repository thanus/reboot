package nl.thanus.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "User not found", code = HttpStatus.NOT_FOUND)
class UserNotFoundException extends RuntimeException {
}
