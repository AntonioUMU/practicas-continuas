package com.ejemplo.biblioteca.service;

import com.ejemplo.biblioteca.domain.Book;
import com.ejemplo.biblioteca.exception.BookNotFoundException;
import com.ejemplo.biblioteca.exception.BusinessRuleException;
import com.ejemplo.biblioteca.exception.DuplicateIsbnException;
import com.ejemplo.biblioteca.repository.BookRepository;
import java.time.Year;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookService {

  private final BookRepository repository;

  public BookService(BookRepository repository) {
    this.repository = repository;
  }

  public Page<Book> findAll(int page, int size) {
    return repository.findAll(PageRequest.of(page, size));
  }

  @Transactional(readOnly = true)
  public List<Book> findAll() {
    return repository.findAll();
  }

  @Transactional(readOnly = true)
  public List<Book> searchByTitle(String title) {
    if (title == null || title.isBlank()) {
      return findAll();
    }

    return repository.findByTitleContainingIgnoreCase(title.strip());
  }

  @Transactional(readOnly = true)
  public List<Book> searchByAuthor(String author) {
    if (author == null || author.isBlank()) {
      return findAll();
    }

    return repository.findByAuthorContainingIgnoreCase(author.strip());
  }

  @Transactional(readOnly = true)
  public Book findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
  }

  public Book create(Book book) {
    validateBusinessRules(book, null);
    return repository.save(book);
  }

  public Book update(Long id, Book data) {
    Book existing = findById(id);
    validateBusinessRules(data, id);

    existing.setTitle(data.getTitle());
    existing.setAuthor(data.getAuthor());
    existing.setIsbn(data.getIsbn());
    existing.setPublicationYear(data.getPublicationYear());
    existing.setPages(data.getPages());
    existing.setAvailable(data.getAvailable());

    return repository.save(existing);
  }

  public void delete(Long id) {
    Book existing = findById(id);

    if (Boolean.FALSE.equals(existing.getAvailable())) {
      throw new BusinessRuleException(
          "No se puede eliminar un libro que está actualmente prestado");
    }

    repository.delete(existing);
  }

  public Book updateAvailability(Long id, Boolean available) {
    Book book = findById(id);
    book.setAvailable(available);
    return repository.save(book);
  }

  private void validateBusinessRules(Book book, Long currentId) {
    if (book.getPublicationYear() != null && book.getPublicationYear() > Year.now().getValue()) {
      throw new BusinessRuleException("El año de publicación no puede ser posterior al año actual");
    }

    if (book.getIsbn() != null) {
      boolean duplicated =
          currentId == null
              ? repository.existsByIsbn(book.getIsbn())
              : repository.existsByIsbnAndIdNot(book.getIsbn(), currentId);

      if (duplicated) {
        throw new DuplicateIsbnException(book.getIsbn());
      }
    }
  }
}
