# 🧭 DECISIONES.md — Bitácora de diseño

> **Instrucciones.** Completa **una entrada por fase**, en **primera persona** y
> **refiriéndote a tu propio código**: nombres reales de tus clases, tu tabla, tus
> líneas, tu salida real de terminal.
>
> ❌ **No puntúa** una justificación genérica que podría pegarse en cualquier proyecto
> (ej.: *"usé boundedElastic porque es una buena práctica para operaciones bloqueantes"*).
> ✅ **Sí puntúa** una justificación anclada a tu código (ej.: *"en `ProductoService`
> línea 34 envolví `productoRepository.findAll()` porque Hibernate abre la conexión
> JDBC en el hilo llamante; al probarlo sin `subscribeOn` vi en el log el hilo
> `reactor-http-nio-2`, que es el event loop de Netty"*).
>
> Estas mismas preguntas se te harán en la **defensa oral**.

---

## Datos

- **Nombre:** ALEX CANCHIGNIA
- **Cédula:** 1753484623
- **NN (dos últimos dígitos):** 23
- **Categoría asignada (según el último dígito):** Café

---

## Fase 1 — Configuración y perfiles

**1.1 ¿Qué archivo activa el perfil `prod` y qué línea exacta lo hace?**

> En mi proyecto el perfil `prod` se activa en `src/main/resources/application.properties`. La línea exacta es `spring.profiles.active=prod`. El archivo `src/main/resources/application-prod.properties` contiene el puerto, la conexión, la configuración de Hibernate y las propiedades del modelo de IA.

**1.2 Pega la línea del log de arranque donde se ve tu puerto y el perfil activo.**

```text
2026-07-31T19:58:37.996-05:00  INFO 77004 --- [agrosmart] [main] e.e.espe.agrosmart.AgrosmartApplication : The following 1 profile is active: "prod"
2026-07-31T19:58:43.518-05:00  INFO 77004 --- [agrosmart] [main] o.s.b.web.embedded.netty.NettyWebServer : Netty started on port 8123 (http)
2026-07-31T19:58:43.528-05:00  INFO 77004 --- [agrosmart] [main] e.e.espe.agrosmart.AgrosmartApplication : Started AgrosmartApplication in 6.456 seconds
```

**1.3 ¿Qué habría pasado si dejabas `ddl-auto=create-drop` en lugar de `update`?**

> Si hubiera usado `create-drop`, Hibernate habría creado el esquema al arrancar y eliminado `tbl_productos_base_23` al cerrar la aplicación. Los cinco productos sembrados se perderían y volverían a insertarse en la siguiente ejecución. Usé `spring.jpa.hibernate.ddl-auto=update` para conservar la tabla y sus registros entre reinicios.

**1.4 ¿Levantaste PostgreSQL con `compose.yaml` o con una instalación local?**

> Levanté PostgreSQL con `compose.yaml`. En mi equipo el puerto `5432` del host llegaba a otra instancia de PostgreSQL, por lo que la autenticación funcionaba dentro del contenedor, pero fallaba desde Spring. Publiqué el contenedor como `55432:5432`, configuré la URL `jdbc:postgresql://127.0.0.1:55432/agrosmart_db` y mantuve la base reproducible con Docker. La ventaja es que la base, el usuario y la contraseña están declarados en el proyecto y no dependen de una instalación manual específica.

---

## Fase 2 — Persistencia con JPA/Hibernate

**2.1 ¿Cuál es el nombre exacto de tu tabla y de dónde salió ese nombre?**

> Mi tabla se llama `tbl_productos_base_23`. El prefijo `tbl_productos_base_` lo exige el examen y `23` corresponde a los dos últimos dígitos de mi cédula `1753484623`.

**2.2 Salida de `psql` y restricciones.**

```text
[PEGAR AQUÍ LA SALIDA REAL DE:
docker exec -e PGPASSWORD=agrosmart agrosmart-postgres psql -U agrosmart -d agrosmart_db -c "\d tbl_productos_base_23"]
```

> En la salida se debe observar `nombre_producto character varying(120) NOT NULL` y el índice o restricción `UNIQUE` sobre `nombre_producto`. También se observa `precio_usd numeric(10,2)` y `id_producto` como identidad.

**2.3 ¿Por qué usaste `BigDecimal` y no `double` para `precio_usd`?**

> En `ProductoEntity` y `Producto` usé `BigDecimal` porque el precio es un valor decimal que debe conservar precisión. `double` usa representación binaria y puede producir aproximaciones, por ejemplo al sumar valores monetarios. Con `@Column(precision = 10, scale = 2)`, Hibernate generó `precio_usd numeric(10,2)` en PostgreSQL, que corresponde correctamente al uso de `BigDecimal`.

**2.4 ¿Cómo hiciste idempotente tu siembra?**

> En `DataSeeder.sembrarProductos(...)` consulté `repository.count()` y solo ejecuté `repository.saveAll(...)` cuando el conteo era cero. En el segundo arranque no se insertan registros nuevos. Sin esa condición, los mismos nombres intentarían insertarse otra vez y PostgreSQL rechazaría la operación por la restricción `unique` de `nombre_producto`.

---

## Fase 3 — Modelo inmutable y lógica funcional

**3.1 ¿Por qué tienes dos clases: `ProductoEntity` y `Producto`?**

> `ProductoEntity` representa el esquema de Hibernate. Tiene constructor vacío, campos modificables y setters porque el ORM necesita materializar y administrar la entidad. `Producto` representa el dominio usado por la API y el flujo reactivo; es `final`, no tiene setters y sus campos son `private final`. Intentar usar una única clase obligaría a mezclar las necesidades de mutabilidad del ORM con la inmutabilidad del dominio.

**3.2 Código de las dos copias defensivas.**

```java
// Producto.java, copia defensiva de entrada, líneas 30-31 de mi archivo:
this.correosNotificacion =
        new ArrayList<>(correosNotificacion);

// Producto.java, copia defensiva de salida, líneas 49-51 de mi archivo:
return Collections.unmodifiableList(
        new ArrayList<>(correosNotificacion)
);
```

> Antes de entregar verificaré los números exactos con `findstr /n "^" src\main\java\ec\edu\espe\agrosmart\domain\Producto.java`, porque pueden desplazarse si agrego comentarios.

**3.3 ¿Por qué la copia defensiva solo en el getter no sería suficiente?**

> Si el constructor guardara directamente la referencia recibida, un código externo podría crear una lista, construir `Producto` con ella y luego agregar o eliminar correos desde esa lista original. El getter podría devolver una copia perfecta, pero el estado interno ya habría sido modificado desde fuera. Por eso copio tanto al entrar como al salir.

**3.4 ¿Cómo implementaste `A_MAYUSCULAS` sin mutar el producto?**

```java
public static final Function<Producto, Producto> A_MAYUSCULAS =
        producto -> new Producto(
                producto.getId(),
                producto.getNombre().toUpperCase(Locale.ROOT),
                producto.getCategoria(),
                producto.getPrecioUsd(),
                producto.getCorreosNotificacion()
        );
```

> La función construye una nueva instancia y conserva intacto el objeto recibido.

---

## Fase 4 — Servicio reactivo y aislamiento del bloqueo

**4.1 Método `obtenerProductosComercializables()` completo.**

```java
public Flux<Producto> obtenerProductosComercializables() {

    return Mono.fromCallable(repository::findAll)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMapMany(Flux::fromIterable)
            .map(ProductoMapper::toDominio)
            .map(ProductoFilters.A_MAYUSCULAS)
            .filter(ProductoFilters.IS_VALID)
            .doOnNext(ProductoFilters.LOG_PRODUCTO)
            .defaultIfEmpty(PRODUCTO_GENERICO);
}
```

**4.2 ¿Qué pasa si eliminas `subscribeOn(boundedElastic())`?**

> No eliminé esa línea durante la ejecución final porque no quería bloquear deliberadamente el servidor. Sin ella, `repository.findAll()` se ejecutaría en el hilo que realiza la suscripción. Cuando el flujo se suscribe desde una petición HTTP, ese hilo puede ser uno de Netty, como `reactor-http-nio-*`. JDBC mantendría ocupado el event loop mientras espera PostgreSQL, reduciendo la capacidad de atender otras solicitudes. Con `subscribeOn(Schedulers.boundedElastic())`, la consulta se ejecuta en un hilo `boundedElastic-*`.

**4.3 ¿Por qué `Mono.fromCallable(...)` y no `Mono.just(repository.findAll())`?**

> `Mono.just(repository.findAll())` ejecutaría `findAll()` inmediatamente antes de construir el `Mono`, por lo que la operación bloqueante ocurriría antes de que Reactor pudiera cambiarla de scheduler. `Mono.fromCallable(repository::findAll)` difiere la consulta hasta la suscripción; así `subscribeOn(boundedElastic())` sí controla el hilo donde se ejecuta.

**4.4 ¿Dónde usaste `defaultIfEmpty` y `switchIfEmpty`?**

> En `obtenerProductosComercializables()` usé `defaultIfEmpty(PRODUCTO_GENERICO)` porque, si el filtro elimina todos los registros, quiero emitir un valor normal de respaldo. En `buscarPorId(Long id)` usé `switchIfEmpty(Mono.error(new ProductoNoEncontradoException(id)))` porque una búsqueda inexistente debe cambiar a otro publisher que termine en error y produzca HTTP 404. No son intercambiables: uno emite un elemento y el otro permite cambiar a un flujo de error.

**4.5 ¿Por qué `doOnNext` no sirve para transformar?**

> `doOnNext` recibe el producto únicamente para ejecutar un efecto lateral. En mi caso llama a `ProductoFilters.LOG_PRODUCTO` para imprimir ID y nombre. El operador vuelve a emitir el mismo objeto. Para transformar utilicé `map`, tanto en `ProductoMapper::toDominio` como en `ProductoFilters.A_MAYUSCULAS`.

---

## Fase 5 — Módulo de IA con LangChain4j

**5.1 Interfaz `AgroSmartAIService` completa.**

```java
@AiService
public interface AgroSmartAIService {

    @UserMessage("""
            Redacta una frase publicitaria de máximo 100 caracteres para vender \
            {{producto}} dirigido a {{audiencia}}.""")
    String generarPublicidad(
            @V("producto") String producto,
            @V("audiencia") String audiencia
    );
}
```

**5.2 ¿Qué hace `@V("producto")`?**

> `@V("producto")` enlaza el parámetro Java con la variable `{{producto}}` del `@UserMessage`. Lo mismo ocurre con `@V("audiencia")`. Si quitara la anotación, LangChain4j no tendría el mapeo explícito requerido para reemplazar correctamente la variable del prompt.

**5.3 ¿Dónde configuraste el modelo y por qué no declaraste un `@Bean`?**

> Configuré el modelo en `src/main/resources/application-prod.properties` con estas líneas:

```properties
langchain4j.open-ai.chat-model.api-key=demo
langchain4j.open-ai.chat-model.model-name=gpt-4o-mini
langchain4j.open-ai.chat-model.timeout=30s
langchain4j.open-ai.chat-model.log-requests=true
langchain4j.open-ai.chat-model.log-responses=true
logging.level.dev.langchain4j=DEBUG
```

> No declaré un `@Bean` porque `langchain4j-open-ai-spring-boot-starter` lee esas propiedades y autoconfigura el modelo y la interfaz anotada con `@AiService`.

**5.4 ¿Por qué la llamada de IA necesita `boundedElastic`?**

> Aunque no consulta PostgreSQL, `aiService.generarPublicidad(...)` realiza una llamada HTTP síncrona y espera la respuesta del proveedor. Esa espera también bloquea el hilo. En `PublicidadService` la envolví en `Mono.fromCallable(...)`, la ejecuté en `boundedElastic`, limité la espera con `timeout(Duration.ofSeconds(30))` y recuperé los fallos con `onErrorResume`.

**5.5 Error real del proveedor y respuesta de respaldo.**

```text
[PEGAR AQUÍ EL MENSAJE REAL DE LA PRUEBA DE PUBLICIDAD.
Si no hubo error, escribir: "Durante la ejecución final el proveedor no devolvió un error".]

Respuesta de onErrorResume:
Publicidad no disponible en este momento ([NOMBRE REAL DE LA EXCEPCIÓN])
```

---

## Fase 6 — API reactiva con WebFlux

**6.1 Salida real de los cuatro `curl`.**

```text
1) curl.exe http://localhost:8123/api/productos

[PEGAR SALIDA REAL]

2) curl.exe http://localhost:8123/api/productos/1

[PEGAR SALIDA REAL]

3) curl.exe -i http://localhost:8123/api/productos/9999

[PEGAR SALIDA REAL, INCLUYENDO HTTP/1.1 404 Not Found]

4) curl.exe "http://localhost:8123/api/agrosmart/publicidad?producto=Cafe%20arabigo%20de%20altura&audiencia=cafeterias%20de%20especialidad"

[PEGAR SALIDA REAL]
```

**6.2 ¿Cómo lograste que el ID inexistente responda 404?**

> `ProductoService.buscarPorId(...)` convierte `Optional.empty()` en un `Mono` vacío y luego usa `switchIfEmpty(Mono.error(new ProductoNoEncontradoException(id)))`. La clase `ProductoNoEncontradoException` está anotada con `@ResponseStatus(HttpStatus.NOT_FOUND)`, por lo que Spring WebFlux traduce esa excepción a HTTP 404 y no a 500.

**6.3 ¿Qué ocurriría si el controlador devolviera `List<Producto>`?**

> Una firma con `List<Producto>` podría compilar, pero dejaría de representar un resultado reactivo. Para obtener esa lista desde el `Flux` tendría que materializarla y probablemente bloquear con `block()`, lo cual está prohibido. Con `Flux<Producto>` el controlador conserva la demanda, la suscripción y el procesamiento reactivo sin bloquear el event loop.

---

## Fase 7 — Pruebas unitarias

**7.1 Salida real de las pruebas.**

```text
[PEGAR AQUÍ LA PARTE FINAL DE .\mvnw.cmd test:
Tests run: ...
Failures: 0
Errors: 0
BUILD SUCCESS]
```

**7.2 ¿Cuántos productos espera `expectNextCount(...)`?**

> Mi prueba espera `expectNextCount(3)` porque la siembra contiene exactamente tres productos válidos y dos inválidos. El conteo no se calcula con NN; es un valor fijo del examen. Los tres válidos tienen precio positivo y correos. Uno tiene precio cero y otro tiene la cadena de correos vacía.

**7.3 ¿Por qué mockeaste `ProductoRepository`?**

> Mockeé `ProductoRepository` para probar únicamente el comportamiento de `ProductoService`. Así la prueba no depende de que Docker, PostgreSQL, el puerto `55432` o los datos reales estén disponibles. También puedo controlar exactamente los cinco registros y verificar los casos límite.

**7.4 ¿Qué demuestra `assertNotSame` que `assertEquals` no demuestra?**

> `assertEquals` comprueba que dos listas tienen el mismo contenido. `assertNotSame` comprueba que no son la misma instancia en memoria. En mi prueba demuestra que el getter no expone la referencia original y que existe una copia defensiva.

**7.5 ¿Por qué una prueba de `Flux` sin `verifyComplete()` o `verify()` no prueba nada?**

> Los publishers de Reactor son lazy. Crear el `Flux` no ejecuta `repository.findAll()`, los operadores ni las aserciones. `StepVerifier.verifyComplete()` o `verify()` realiza la suscripción, consume las señales y valida que el flujo termine de la forma esperada.

---

## Fase 8 — Integración y cierre

**8.1 `git log --oneline --graph --all`.**

*   8a76d0e (HEAD -> feature/documentacion, origin/main, origin/HEAD, main) Merge pull request #7 from 161595Javier/feature/pruebas
|\
| * 63f00e4 (origin/feature/pruebas, feature/pruebas) test: agrega pruebas del modelo, logica funcional, flujo reactivo e ia
|/
*   f188faa Merge pull request #6 from 161595Javier/feature/api-reactiva
|\
| * fc3a179 (origin/feature/api-reactiva, feature/api-reactiva) feat: expone endpoints reactivos y de publicidad
|/
*   a3cbaed Merge pull request #5 from 161595Javier/feature/ia-langchain4j
|\
| * be01552 (origin/feature/ia-langchain4j, feature/ia-langchain4j) feat: integra langchain4j para publicidad de productos
|/
*   8f8a842 Merge pull request #4 from 161595Javier/feature/servicio-reactivo
|\
| * 6537523 (origin/feature/servicio-reactivo, feature/servicio-reactivo) feat: implementa servicio reactivo con boundedElastic y operadores
|/
*   fcfc8f0 Merge pull request #3 from 161595Javier/feature/modelo-inmutable
|\
| * f37d773 (origin/feature/modelo-inmutable, feature/modelo-inmutable) feat: agrega modelo inmutable de producto y logica funcional
| * 13c5ff5 feat: agrega modelo inmutable de producto y logica funcional
|/
*   df5063e Merge pull request #2 from 161595Javier/feature/persistencia-jpa
|\
| * 0c74440 (origin/feature/persistencia-jpa, feature/persistencia-jpa) feat: agrega entidad jpa de productos y siembra de datos
* | 3910e5a Merge pull request #1 from 161595Javier/feature/config-perfiles
|\|
| * f507fd7 (origin/feature/config-perfiles, feature/config-perfiles) chore: configura perfil prod con postgresql y puerto propio
|/
* 5f72fd8 chore: inicializa proyecto agrosmart y registra identidad del examen
* 4a5e319 Modify personal details in IDENTIDAD.md
* 9274613 Initial commit

**8.2 ¿Qué fase tomó más tiempo y por qué?**

> La Fase 1 tomó más tiempo del previsto por un conflicto de infraestructura. El contenedor aceptaba la contraseña internamente, pero las conexiones a `localhost:5432` llegaban a otra instancia de PostgreSQL. Lo confirmé al comparar la conexión dentro del contenedor con una conexión externa. Finalmente publiqué AgroSmart en `55432`, actualicé la URL JDBC y la aplicación inició correctamente con Hikari, JPA y Netty.

**8.3 Si tuvieras 30 minutos más, ¿qué mejorarías primero?**

> Implementaría primero un manejo centralizado de excepciones con `@RestControllerAdvice` y un enum de errores. La API ya responde 404 mediante `@ResponseStatus`, pero un manejador central produciría respuestas homogéneas, facilitaría agregar validaciones y sumaría el bonus definido en la rúbrica. Lo priorizaría sobre Swagger porque mejora directamente el comportamiento ante fallos.

**8.4 Herramientas consultadas.**

> Consulté el README y las plantillas oficiales del examen para seguir las fases, los nombres obligatorios, los operadores y la rúbrica. También consulté documentación de Spring Boot, WebFlux, JPA/Hibernate, Project Reactor y LangChain4j para revisar configuración y comportamiento. Utilicé ChatGPT y claude en sus versiones gratuitas como asistentes para organizar el procedimiento, proponer estructuras de código, revisar errores de compilación, diagnosticar el conflicto de PostgreSQL y preparar la documentación. Verifiqué el resultado ejecutando compilación, aplicación, consultas `psql`, `curl`, pruebas y comandos Git en mi propio entorno.
