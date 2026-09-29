# Biblioteca API

API REST de gestión de biblioteca construida con:

- Java 21
- Spring Boot 3.4.10
- Spring Web
- Spring Data JPA + Hibernate
- H2 en memoria
- Jakarta Bean Validation
- JUnit 5 + Mockito
- Maven

## Arquitectura

```text
controller/
service/
repository/
domain/
exception/
```

El flujo principal es:

```text
HTTP -> Controller -> Service -> Repository -> Hibernate/JPA -> H2
```

El controlador se ocupa del protocolo HTTP y de activar la validación de entrada. La lógica de negocio está en `BookService`. La persistencia se delega a `BookRepository`.

## Reglas de negocio

Además de las validaciones Bean Validation del recurso `Book`, el servicio aplica estas reglas:

1. No se puede crear un libro con un ISBN ya existente.
2. No se puede actualizar un libro usando el ISBN de otro libro.
3. El año de publicación no puede ser posterior al año actual.
4. No se puede eliminar un libro que esté prestado (`available=false`).
5. Un recurso inexistente produce HTTP 404 mediante `@RestControllerAdvice`.

## Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/books` | Lista todos los libros |
| GET | `/api/books/{id}` | Obtiene un libro |
| GET | `/api/books?title=texto` | Busca por parte del título, sin distinguir mayúsculas y minúsculas |
| POST | `/api/books` | Crea un libro |
| PUT | `/api/books/{id}` | Actualiza un libro |
| DELETE | `/api/books/{id}` | Elimina un libro |
| PATCH | `/api/books/{id}/availability` | Cambia la disponibilidad de un libro |

Ejemplo de `POST /api/books`:

```json
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "publicationYear": 2008,
  "pages": 464,
  "available": true
}
```

Ejemplo de `PATCH /api/books/{id}/availability`:

```json
{
  "available": false
}
```

### Búsqueda por título

El parámetro `title` es opcional. Si se omite o solo contiene espacios, se devuelven todos los libros. Si no hay coincidencias, se devuelve una lista vacía.

```bash
curl 'http://localhost:8080/api/books?title=clean'
```

## Compilar y ejecutar

Requiere Maven 3.9+ y JDK 21.

```bash
mvn clean package
java -jar target/biblioteca-api-0.0.1-SNAPSHOT.jar
```

`spring-boot-maven-plugin` empaqueta el proyecto como JAR ejecutable con las dependencias incluidas.

## Tests

Los tests están en `src/test/java` y son exclusivamente unitarios. No utilizan `@SpringBootTest`, `@DataJpaTest`, una base de datos ni levantan el contexto de Spring.

Para ejecutarlos:

```bash
mvn test
```

La clase `BookServiceTest` comprueba reglas de negocio reales, incluyendo ISBN duplicado, año futuro, recurso inexistente, actualización, borrado de libros prestados y borrado permitido.

## H2

Durante la ejecución, H2 está disponible en memoria.

Consola:

```text
http://localhost:8080/h2-console
```

Datos de conexión:

```text
JDBC URL: jdbc:h2:mem:biblioteca
User: sa
Password: (vacío)
```

La base de datos se crea al arrancar y se elimina al apagar la aplicación.
