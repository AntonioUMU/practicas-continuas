package com.ejemplo.biblioteca.service;

import com.ejemplo.biblioteca.domain.Book;
import com.ejemplo.biblioteca.exception.BookNotFoundException;
import com.ejemplo.biblioteca.exception.BusinessRuleException;
import com.ejemplo.biblioteca.exception.DuplicateIsbnException;
import com.ejemplo.biblioteca.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

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

    private Book validBook() {
        return new Book(
            null,
            "Clean Code",
            "Robert C. Martin",
            "9780132350884",
            2008,
            464,
            true
        );
    }
}
