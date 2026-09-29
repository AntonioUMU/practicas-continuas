# Contribuir a Biblioteca API

## Preparar el entorno

Necesitas Git, JDK 21 y Maven 3.9 o posterior. Clona el repositorio y comprueba que Java y Maven están disponibles:

```bash
git clone https://github.com/AntonioUMU/practicas-continuas.git
cd practicas-continuas
java -version
mvn -version
```

Para activar el hook de pre-commit versionado en `.githooks` (Linux, macOS o WSL):

```bash
chmod +x .githooks/pre-commit
git config --local core.hooksPath .githooks
```

El hook ejecuta `mvn spotless:apply` antes de cada commit. Revisa los archivos modificados antes de confirmar los cambios.

## Construir y ejecutar

```bash
mvn clean package
java -jar target/biblioteca-api-0.0.1-SNAPSHOT.jar
```

La API estará disponible en `http://localhost:8080`. Consulta `README.md` para ver los endpoints y la configuración de H2.

## Proponer un cambio

1. Abre un Issue de tipo error o mejora y describe el resultado esperado.
2. Actualiza `main` y crea una rama corta, por ejemplo:

   ```bash
   git switch main
   git pull --ff-only
   git switch -c feat/busqueda-de-libros
   ```

3. Implementa el cambio y añade o ajusta los tests unitarios.
4. Antes de subirlo, ejecuta:

   ```bash
   mvn spotless:check
   mvn test
   git status
   ```

5. Crea commits con mensajes descriptivos siguiendo Conventional Commits, por ejemplo `feat: añade búsqueda de libros` o `fix: corrige validación de ISBN`.
6. Publica tu rama con `git push -u origin nombre-de-la-rama` y abre un Pull Request hacia `main`. Completa la plantilla y escribe `Closes #N` con el número del Issue.

## Revisión y fusión

No hagas push directo a `main`. La otra persona revisará el PR; atiende sus comentarios en la misma rama. Cuando estén resueltas las conversaciones y se haya aprobado el cambio, fusionadlo mediante **Create a merge commit** y eliminad la rama. Esta estrategia conserva los commits de la rama y añade un commit de fusión.
