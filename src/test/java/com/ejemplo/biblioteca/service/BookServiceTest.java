package com.ejemplo.biblioteca.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.ejemplo.biblioteca.domain.Book;
import com.ejemplo.biblioteca.exception.BookNotFoundException;
import com.ejemplo.biblioteca.exception.BusinessRuleException;
import com.ejemplo.biblioteca.exception.DuplicateIsbnException;
import com.ejemplo.biblioteca.repository.BookRepository;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock private BookRepository repository;

  private BookService service;

  @BeforeEach
  void setUp() {
    service = new BookService(repository);
  }

  @Test
  void createShouldSaveBookWhenBusinessRulesAreValid() {
    Book book = validBook();

    when(repository.existsByIsbn(book.getIsbn())).thenReturn(false);
    when(repository.save(book)).thenReturn(book);

    Book result = service.create(book);

    assertSame(book, result);
    verify(repository).save(book);
  }

  @Test
  void createShouldRejectDuplicatedIsbn() {
    Book book = validBook();

    when(repository.existsByIsbn(book.getIsbn())).thenReturn(true);

    assertThrows(DuplicateIsbnException.class, () -> service.create(book));
    verify(repository, never()).save(any());
  }

  @Test
  void createShouldRejectPublicationYearInTheFuture() {
    Book book = validBook();
    book.setPublicationYear(Year.now().getValue() + 1);

    assertThrows(BusinessRuleException.class, () -> service.create(book));
    verify(repository, never()).save(any());
  }

  @Test
  void findByIdShouldReturnBookWhenItExists() {
    Book book = validBook();
    book.setId(10L);

    when(repository.findById(10L)).thenReturn(Optional.of(book));

    Book result = service.findById(10L);

    assertEquals(10L, result.getId());
    verify(repository).findById(10L);
  }

  @Test
  void findByIdShouldThrowWhenBookDoesNotExist() {
    when(repository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(BookNotFoundException.class, () -> service.findById(99L));
  }

  @Test
  void updateShouldCopyFieldsAndSaveExistingBook() {
    Book existing = validBook();
    existing.setId(5L);

    Book data = validBook();
    data.setTitle("Nuevo título");
    data.setAuthor("Nuevo autor");
    data.setIsbn("9783161484100");
    data.setPublicationYear(2020);
    data.setPages(350);
    data.setAvailable(false);

    when(repository.findById(5L)).thenReturn(Optional.of(existing));
    when(repository.existsByIsbnAndIdNot(data.getIsbn(), 5L)).thenReturn(false);
    when(repository.save(existing)).thenReturn(existing);

    Book result = service.update(5L, data);

    assertEquals("Nuevo título", result.getTitle());
    assertEquals("Nuevo autor", result.getAuthor());
    assertEquals("9783161484100", result.getIsbn());
    assertEquals(350, result.getPages());
    assertFalse(result.getAvailable());
    verify(repository).save(existing);
  }

  @Test
  void updateShouldRejectIsbnUsedByAnotherBook() {
    Book existing = validBook();
    existing.setId(5L);

    Book data = validBook();
    data.setIsbn("9783161484100");

    when(repository.findById(5L)).thenReturn(Optional.of(existing));
    when(repository.existsByIsbnAndIdNot(data.getIsbn(), 5L)).thenReturn(true);

    assertThrows(DuplicateIsbnException.class, () -> service.update(5L, data));
    verify(repository, never()).save(any());
  }

  @Test
  void deleteShouldRejectBookCurrentlyLoaned() {
    Book existing = validBook();
    existing.setId(7L);
    existing.setAvailable(false);

    when(repository.findById(7L)).thenReturn(Optional.of(existing));

    assertThrows(BusinessRuleException.class, () -> service.delete(7L));
    verify(repository, never()).delete(any());
  }

  @Test
  void deleteShouldRemoveAvailableBook() {
    Book existing = validBook();
    existing.setId(8L);
    existing.setAvailable(true);

    when(repository.findById(8L)).thenReturn(Optional.of(existing));

    service.delete(8L);

    verify(repository).delete(existing);
  }

  @Test
  void findAllShouldDelegateToRepository() {
    List<Book> books = List.of(validBook(), validBook());
    when(repository.findAll()).thenReturn(books);

    assertEquals(2, service.findAll().size());
    verify(repository).findAll();
  }

  @Test
  void updateAvailabilityShouldMarkBookAsLoaned() {
    Book book = validBook();
    book.setId(1L);
    book.setAvailable(true);

    when(repository.findById(1L)).thenReturn(Optional.of(book));
    when(repository.save(book)).thenReturn(book);

    Book result = service.updateAvailability(1L, false);

    assertFalse(result.getAvailable());
    verify(repository).save(book);
  }

  @Test
  void updateAvailabilityShouldMarkBookAsAvailable() {
    Book book = validBook();
    book.setId(1L);
    book.setAvailable(false);

    when(repository.findById(1L)).thenReturn(Optional.of(book));
    when(repository.save(book)).thenReturn(book);

    Book result = service.updateAvailability(1L, true);

    assertTrue(result.getAvailable());
    verify(repository).save(book);
  }

  @Test
  void updateAvailabilityShouldThrowWhenBookDoesNotExist() {
    when(repository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(BookNotFoundException.class, () -> service.updateAvailability(999L, true));

    verify(repository, never()).save(any());
  }

  @Test
  void findAll_shouldReturnRequestedPage() {
    List<Book> books = List.of(createBook(1L, "Clean Code"), createBook(2L, "Effective Java"));

    Page<Book> expectedPage = new PageImpl<>(books, PageRequest.of(0, 2), 5);

    when(repository.findAll(PageRequest.of(0, 2))).thenReturn(expectedPage);

    Page<Book> result = service.findAll(0, 2);

    assertEquals(2, result.getContent().size());
    assertEquals(5, result.getTotalElements());
    assertEquals(0, result.getNumber());
    assertEquals(2, result.getSize());

    verify(repository).findAll(PageRequest.of(0, 2));
  }

  @Test
  void findAll_shouldReturnSecondPage() {
    List<Book> books =
        List.of(createBook(3L, "Refactoring"), createBook(4L, "Java Concurrency in Practice"));

    Page<Book> expectedPage = new PageImpl<>(books, PageRequest.of(1, 2), 4);

    when(repository.findAll(PageRequest.of(1, 2))).thenReturn(expectedPage);

    Page<Book> result = service.findAll(1, 2);

    assertEquals(2, result.getContent().size());
    assertEquals(4, result.getTotalElements());
    assertEquals(1, result.getNumber());
    assertEquals(2, result.getSize());

    verify(repository).findAll(PageRequest.of(1, 2));
  }

  @Test
  void findAll_shouldRejectInvalidPageSize() {
    assertThrows(IllegalArgumentException.class, () -> service.findAll(0, 0));

    verifyNoInteractions(repository);
  }

  @Test
  void searchByTitleShouldReturnAllWhenTitleIsMissing() {
    List<Book> books = List.of(validBook());
    when(repository.findAll()).thenReturn(books);

    assertEquals(books, service.searchByTitle(null));
    verify(repository).findAll();
    verify(repository, never()).findByTitleContainingIgnoreCase(anyString());
  }

  @Test
  void searchByTitleShouldReturnAllWhenTitleIsBlank() {
    List<Book> books = List.of(validBook());
    when(repository.findAll()).thenReturn(books);

    assertEquals(books, service.searchByTitle("   "));
    verify(repository).findAll();
  }

  @Test
  void searchByTitleShouldTrimTermAndReturnMatches() {
    List<Book> matches = List.of(validBook());
    when(repository.findByTitleContainingIgnoreCase("cLeAn")).thenReturn(matches);

    assertEquals(matches, service.searchByTitle("  cLeAn  "));
    verify(repository).findByTitleContainingIgnoreCase("cLeAn");
    verify(repository, never()).findAll();
  }

  @Test
  void searchByTitleShouldReturnEmptyListWhenNothingMatches() {
    when(repository.findByTitleContainingIgnoreCase("inexistente")).thenReturn(List.of());

    assertTrue(service.searchByTitle("inexistente").isEmpty());
  }

  @Test
  void searchByAuthorShouldReturnAllWhenAuthorIsMissing() {
    List<Book> books = List.of(validBook());
    when(repository.findAll()).thenReturn(books);

    assertEquals(books, service.searchByAuthor(null));
    verify(repository).findAll();
    verify(repository, never()).findByAuthorContainingIgnoreCase(anyString());
  }

  @Test
  void searchByAuthorShouldReturnAllWhenAuthorIsBlank() {
    List<Book> books = List.of(validBook());
    when(repository.findAll()).thenReturn(books);

    assertEquals(books, service.searchByAuthor("   "));
    verify(repository).findAll();
    verify(repository, never()).findByAuthorContainingIgnoreCase(anyString());
  }

  @Test
  void searchByAuthorShouldTrimTermAndReturnMatches() {
    List<Book> matches = List.of(validBook());
    when(repository.findByAuthorContainingIgnoreCase("mArTiN")).thenReturn(matches);

    assertEquals(matches, service.searchByAuthor("  mArTiN  "));
    verify(repository).findByAuthorContainingIgnoreCase("mArTiN");
    verify(repository, never()).findAll();
  }

  @Test
  void searchByAuthorShouldReturnEmptyListWhenNothingMatches() {
    when(repository.findByAuthorContainingIgnoreCase("inexistente")).thenReturn(List.of());

    assertTrue(service.searchByAuthor("inexistente").isEmpty());
  }

  private Book validBook() {
    return new Book(null, "Clean Code", "Robert C. Martin", "9780132350884", 2008, 464, true);
  }

  private Book createBook(Long id, String title) {
    Book book = new Book();
    book.setId(id);
    book.setTitle(title);
    book.setAuthor("Autor");
    book.setIsbn("9780132350884");
    book.setPublicationYear(2020);
    book.setPages(300);
    book.setAvailable(true);
    return book;
  }
}
