package com.ejemplo.biblioteca.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(
    name = "books",
    uniqueConstraints = @UniqueConstraint(name = "uk_books_isbn", columnNames = "isbn")
)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "El autor es obligatorio")
    @Size(max = 150, message = "El autor no puede superar los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String author;

    @NotBlank(message = "El ISBN es obligatorio")
    @Pattern(
        regexp = "^(97[89])[- ]?\\d{1,5}[- ]?\\d{1,7}[- ]?\\d{1,7}[- ]?[\\dX]$",
        message = "El ISBN debe tener un formato ISBN-10 o ISBN-13 válido"
    )
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @NotNull(message = "El año de publicación es obligatorio")
    @Min(value = 1, message = "El año de publicación debe ser positivo")
    @Max(value = 2100, message = "El año de publicación no puede superar 2100")
    @Column(nullable = false)
    private Integer publicationYear;

    @NotNull(message = "El número de páginas es obligatorio")
    @Positive(message = "El número de páginas debe ser mayor que cero")
    @Max(value = 10000, message = "El número de páginas no puede superar 10000")
    @Column(nullable = false)
    private Integer pages;

    @NotNull(message = "La disponibilidad es obligatoria")
    @Column(nullable = false)
    private Boolean available = true;

    public Book() {
    }

    public Book(Long id, String title, String author, String isbn,
                Integer publicationYear, Integer pages, Boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.pages = pages;
        this.available = available;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
