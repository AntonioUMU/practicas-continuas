package com.ejemplo.biblioteca.controller;

import com.ejemplo.biblioteca.domain.Book;
import com.ejemplo.biblioteca.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService service;

  public BookController(BookService service) {
    this.service = service;
  }

  @GetMapping(params = {"!page", "!size"})
  public List<Book> findAll(@RequestParam(name = "title", required = false) String title) {
    return service.searchByTitle(title);
  }

  @GetMapping
  public Page<Book> findAll(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return service.findAll(page, size);
  }

  @GetMapping("/search")
  public List<Book> searchByAuthor(@RequestParam(name = "author") String author) {
    return service.searchByAuthor(author);
  }

  @GetMapping("/{id}")
  public Book findById(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Book create(@Valid @RequestBody Book book) {
    return service.create(book);
  }

  @PutMapping("/{id}")
  public Book update(@PathVariable Long id, @Valid @RequestBody Book book) {
    return service.update(id, book);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }

  @PatchMapping("/{id}/availability")
  public Book updateAvailability(
      @PathVariable Long id, @Valid @RequestBody AvailabilityRequest request) {
    return service.updateAvailability(id, request.available());
  }
}
