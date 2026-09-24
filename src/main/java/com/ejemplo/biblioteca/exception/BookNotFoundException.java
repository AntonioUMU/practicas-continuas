package com.ejemplo.biblioteca.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("No existe el libro con id " + id);
    }
}
