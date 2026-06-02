Below you'll find a **KOTLIN.md** file that builds upon the provided idiomatic Kotlin patterns and complements them with **performance-oriented best practices** and **real-world patterns** commonly found in high-quality GitHub projects (e.g., Kotlin, Ktor, Arrow, Kotlinx.coroutines, etc.).

All original content is preserved. New sections are clearly marked, and some existing examples are extended with performance notes.

---

```markdown
# KOTLIN.md

## Kotlin Development Patterns: Best Practices & Performance

Idiomatic Kotlin patterns and **performance best practices** for building robust, efficient, and maintainable applications.

### When to Use

- Writing new Kotlin code
- Reviewing Kotlin code
- Refactoring existing Kotlin code
- Designing Kotlin modules or libraries
- Configuring Gradle Kotlin DSL builds
- **Optimising performance-critical paths**

### How It Works

This skill enforces idiomatic Kotlin conventions across seven key areas: null safety using the type system and safe-call operators, immutability via `val` and `copy()` on data classes, sealed classes and interfaces for exhaustive type hierarchies, structured concurrency with coroutines and `Flow`, extension functions for adding behaviour without inheritance, type-safe DSL builders using `@DslMarker` and lambda receivers, and Gradle Kotlin DSL for build configuration.  
**Added**: performance patterns (inlining, value classes, sequence optimisations, coroutine dispatching).

---

## Examples

### Null safety with Elvis operator

```kotlin
fun getUserEmail(userId: String): String {
    val user = userRepository.findById(userId)
    return user?.email ?: "unknown@example.com"
}
```

### Sealed class for exhaustive results

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

### Structured concurrency with async/await

```kotlin
suspend fun fetchUserWithPosts(userId: String): UserProfile =
    coroutineScope {
        val user = async { userService.getUser(userId) }
        val posts = async { postService.getUserPosts(userId) }
        UserProfile(user = user.await(), posts = posts.await())
    }
```

---

## Core Principles

### 1. Null Safety

Kotlin's type system distinguishes nullable and non-nullable types. Leverage it fully.

```kotlin
// Good: Use non-nullable types by default
fun getUser(id: String): User {
    return userRepository.findById(id)
        ?: throw UserNotFoundException("User $id not found")
}

// Good: Safe calls and Elvis operator
fun getUserEmail(userId: String): String {
    val user = userRepository.findById(userId)
    return user?.email ?: "unknown@example.com"
}

// Bad: Force-unwrapping nullable types
fun getUserEmail(userId: String): String {
    val user = userRepository.findById(userId)
    return user!!.email // Throws NPE if null
}
```

### 2. Immutability by Default

Prefer `val` over `var`, immutable collections over mutable ones.

```kotlin
// Good: Immutable data
data class User(
    val id: String,
    val name: String,
    val email: String,
)

// Good: Transform with copy()
fun updateEmail(user: User, newEmail: String): User =
    user.copy(email = newEmail)

// Good: Immutable collections
val users: List<User> = listOf(user1, user2)
val filtered = users.filter { it.email.isNotBlank() }

// Bad: Mutable state
var currentUser: User? = null // Avoid mutable global state
val mutableUsers = mutableListOf<User>() // Avoid unless truly needed
```

### 3. Expression Bodies and Single-Expression Functions

Use expression bodies for concise, readable functions.

```kotlin
// Good: Expression body
fun isAdult(age: Int): Boolean = age >= 18

fun formatFullName(first: String, last: String): String =
    "$first $last".trim()

fun User.displayName(): String =
    name.ifBlank { email.substringBefore('@') }

// Good: When as expression
fun statusMessage(code: Int): String = when (code) {
    200 -> "OK"
    404 -> "Not Found"
    500 -> "Internal Server Error"
    else -> "Unknown status: $code"
}

// Bad: Unnecessary block body
fun isAdult(age: Int): Boolean {
    return age >= 18
}
```

### 4. Data Classes for Value Objects

Use data classes for types that primarily hold data.

```kotlin
// Good: Data class with copy, equals, hashCode, toString
data class CreateUserRequest(
    val name: String,
    val email: String,
    val role: Role = Role.USER,
)

// Good: Value class for type safety (zero overhead at runtime)
@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
    }
}

@JvmInline
value class Email(val value: String) {
    init {
        require('@' in value) { "Invalid email: $value" }
    }
}

fun getUser(id: UserId): User = userRepository.findById(id)
```

### Sealed Classes and Interfaces

#### Modeling Restricted Hierarchies

```kotlin
// Good: Sealed class for exhaustive when
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Failure -> null
    is Result.Loading -> null
}

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Failure -> throw error.toException()
    is Result.Loading -> throw IllegalStateException("Still loading")
}
```

#### Sealed Interfaces for API Responses

```kotlin
sealed interface ApiError {
    val message: String

    data class NotFound(override val message: String) : ApiError
    data class Unauthorized(override val message: String) : ApiError
    data class Validation(
        override val message: String,
        val field: String,
    ) : ApiError
    data class Internal(
        override val message: String,
        val cause: Throwable? = null,
    ) : ApiError
}

fun ApiError.toStatusCode(): Int = when (this) {
    is ApiError.NotFound -> 404
    is ApiError.Unauthorized -> 401
    is ApiError.Validation -> 422
    is ApiError.Internal -> 500
}
```

### Scope Functions

#### When to Use Each

```kotlin
// let: Transform nullable or scoped result
val length: Int? = name?.let { it.trim().length }

// apply: Configure an object (returns the object)
val user = User().apply {
    name = "Alice"
    email = "alice@example.com"
}

// also: Side effects (returns the object)
val user = createUser(request).also { logger.info("Created user: ${it.id}") }

// run: Execute a block with receiver (returns result)
val result = connection.run {
    prepareStatement(sql)
    executeQuery()
}

// with: Non-extension form of run
val csv = with(StringBuilder()) {
    appendLine("name,email")
    users.forEach { appendLine("${it.name},${it.email}") }
    toString()
}
```

#### Anti-Patterns

```kotlin
// Bad: Nesting scope functions
user?.let { u ->
    u.address?.let { addr ->
        addr.city?.let { city ->
            println(city) // Hard to read
        }
    }
}

// Good: Chain safe calls instead
val city = user?.address?.city
city?.let { println(it) }
```

### Extension Functions

Adding functionality without inheritance.

```kotlin
// Good: Domain-specific extensions
fun String.toSlug(): String =
    lowercase()
        .replace(Regex("[^a-z0-9\\s-]"), "")
        .replace(Regex("\\s+"), "-")
        .trim('-')

fun Instant.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    atZone(zone).toLocalDate()

// Good: Collection extensions
fun <T> List<T>.second(): T = this[1]

fun <T> List<T>.secondOrNull(): T? = getOrNull(1)

// Good: Scoped extensions (not polluting global namespace)
class UserService {
    private fun User.isActive(): Boolean =
        status == Status.ACTIVE && lastLogin.isAfter(Instant.now().minus(30, ChronoUnit.DAYS))

    fun getActiveUsers(): List<User> = userRepository.findAll().filter { it.isActive() }
}
```

### Coroutines

#### Structured Concurrency

```kotlin
// Good: Structured concurrency with coroutineScope
suspend fun fetchUserWithPosts(userId: String): UserProfile =
    coroutineScope {
        val userDeferred = async { userService.getUser(userId) }
        val postsDeferred = async { postService.getUserPosts(userId) }

        UserProfile(
            user = userDeferred.await(),
            posts = postsDeferred.await(),
        )
    }

// Good: supervisorScope when children can fail independently
suspend fun fetchDashboard(userId: String): Dashboard =
    supervisorScope {
        val user = async { userService.getUser(userId) }
        val notifications = async { notificationService.getRecent(userId) }
        val recommendations = async { recommendationService.getFor(userId) }

        Dashboard(
            user = user.await(),
            notifications = try {
                notifications.await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            },
            recommendations = try {
                recommendations.await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyList()
            },
        )
    }
```

#### Flow for Reactive Streams

```kotlin
// Good: Cold flow with proper error handling
fun observeUsers(): Flow<List<User>> = flow {
    while (currentCoroutineContext().isActive) {
        val users = userRepository.findAll()
        emit(users)
        delay(5.seconds)
    }
}.catch { e ->
    logger.error("Error observing users", e)
    emit(emptyList())
}

// Good: Flow operators
fun searchUsers(query: Flow<String>): Flow<List<User>> =
    query
        .debounce(300.milliseconds)
        .distinctUntilChanged()
        .filter { it.length >= 2 }
        .mapLatest { q -> userRepository.search(q) }
        .catch { emit(emptyList()) }
```

#### Cancellation and Cleanup

```kotlin
// Good: Respect cancellation
suspend fun processItems(items: List<Item>) {
    items.forEach { item ->
        ensureActive() // Check cancellation before expensive work
        processItem(item)
    }
}

// Good: Cleanup with try/finally
suspend fun acquireAndProcess() {
    val resource = acquireResource()
    try {
        resource.process()
    } finally {
        withContext(NonCancellable) {
            resource.release() // Always release, even on cancellation
        }
    }
}
```

### Delegation

#### Property Delegation

```kotlin
// Lazy initialization
val expensiveData: List<User> by lazy {
    userRepository.findAll()
}

// Observable property
var name: String by Delegates.observable("initial") { _, old, new ->
    logger.info("Name changed from '$old' to '$new'")
}

// Map-backed properties
class Config(private val map: Map<String, Any?>) {
    val host: String by map
    val port: Int by map
    val debug: Boolean by map
}

val config = Config(mapOf("host" to "localhost", "port" to 8080, "debug" to true))
```

#### Interface Delegation

```kotlin
// Good: Delegate interface implementation
class LoggingUserRepository(
    private val delegate: UserRepository,
    private val logger: Logger,
) : UserRepository by delegate {
    // Only override what you need to add logging to
    override suspend fun findById(id: String): User? {
        logger.info("Finding user by id: $id")
        return delegate.findById(id).also {
            logger.info("Found user: ${it?.name ?: "null"}")
        }
    }
}
```

### DSL Builders

#### Type-Safe Builders

```kotlin
// Good: DSL with @DslMarker
@DslMarker
annotation class HtmlDsl

@HtmlDsl
class HTML {
    private val children = mutableListOf<Element>()

    fun head(init: Head.() -> Unit) {
        children += Head().apply(init)
    }

    fun body(init: Body.() -> Unit) {
        children += Body().apply(init)
    }

    override fun toString(): String = children.joinToString("\n")
}

fun html(init: HTML.() -> Unit): HTML = HTML().apply(init)

// Usage
val page = html {
    head { title("My Page") }
    body {
        h1("Welcome")
        p("Hello, World!")
    }
}
```

#### Configuration DSL

```kotlin
data class ServerConfig(
    val host: String = "0.0.0.0",
    val port: Int = 8080,
    val ssl: SslConfig? = null,
    val database: DatabaseConfig? = null,
)

data class SslConfig(val certPath: String, val keyPath: String)
data class DatabaseConfig(val url: String, val maxPoolSize: Int = 10)

class ServerConfigBuilder {
    var host: String = "0.0.0.0"
    var port: Int = 8080
    private var ssl: SslConfig? = null
    private var database: DatabaseConfig? = null

    fun ssl(certPath: String, keyPath: String) {
        ssl = SslConfig(certPath, keyPath)
    }

    fun database(url: String, maxPoolSize: Int = 10) {
        database = DatabaseConfig(url, maxPoolSize)
    }

    fun build(): ServerConfig = ServerConfig(host, port, ssl, database)
}

fun serverConfig(init: ServerConfigBuilder.() -> Unit): ServerConfig =
    ServerConfigBuilder().apply(init).build()

// Usage
val config = serverConfig {
    host = "0.0.0.0"
    port = 443
    ssl("/certs/cert.pem", "/certs/key.pem")
    database("jdbc:postgresql://localhost:5432/mydb", maxPoolSize = 20)
}
```

### Sequences for Lazy Evaluation

```kotlin
// Good: Use sequences for large collections with multiple operations
val result = users.asSequence()
    .filter { it.isActive }
    .map { it.email }
    .filter { it.endsWith("@company.com") }
    .take(10)
    .toList()

// Good: Generate infinite sequences
val fibonacci: Sequence<Long> = sequence {
    var a = 0L
    var b = 1L
    while (true) {
        yield(a)
        val next = a + b
        a = b
        b = next
    }
}

val first20 = fibonacci.take(20).toList()
```

### Gradle Kotlin DSL

#### build.gradle.kts Configuration

```kotlin
// Check for latest versions: https://kotlinlang.org/docs/releases.html
plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("io.ktor.plugin") version "3.4.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.7"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "com.example"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:3.4.0")
    implementation("io.ktor:ktor-server-netty:3.4.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:1.0.0")
    implementation("org.jetbrains.exposed:exposed-dao:1.0.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.0.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.0.0")

    // Koin
    implementation("io.insert-koin:koin-ktor:4.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // Testing
    testImplementation("io.kotest:kotest-runner-junit5:6.1.4")
    testImplementation("io.kotest:kotest-assertions-core:6.1.4")
    testImplementation("io.kotest:kotest-property:6.1.4")
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("io.ktor:ktor-server-test-host:3.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

detekt {
    config.setFrom(files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}
```

### Error Handling Patterns

#### Result Type for Domain Operations

```kotlin
// Good: Use Kotlin's Result or a custom sealed class
suspend fun createUser(request: CreateUserRequest): Result<User> = runCatching {
    require(request.name.isNotBlank()) { "Name cannot be blank" }
    require('@' in request.email) { "Invalid email format" }

    val user = User(
        id = UserId(UUID.randomUUID().toString()),
        name = request.name,
        email = Email(request.email),
    )
    userRepository.save(user)
    user
}

// Good: Chain results
val displayName = createUser(request)
    .map { it.name }
    .getOrElse { "Unknown" }
```

#### require, check, error

```kotlin
// Good: Preconditions with clear messages
fun withdraw(account: Account, amount: Money): Account {
    require(amount.value > 0) { "Amount must be positive: $amount" }
    check(account.balance >= amount) { "Insufficient balance: ${account.balance} < $amount" }

    return account.copy(balance = account.balance - amount)
}
```

### Collection Operations

#### Idiomatic Collection Processing

```kotlin
// Good: Chained operations
val activeAdminEmails: List<String> = users
    .filter { it.role == Role.ADMIN && it.isActive }
    .sortedBy { it.name }
    .map { it.email }

// Good: Grouping and aggregation
val usersByRole: Map<Role, List<User>> = users.groupBy { it.role }

val oldestByRole: Map<Role, User?> = users.groupBy { it.role }
    .mapValues { (_, users) -> users.minByOrNull { it.createdAt } }

// Good: Associate for map creation
val usersById: Map<UserId, User> = users.associateBy { it.id }

// Good: Partition for splitting
val (active, inactive) = users.partition { it.isActive }
```

---

## 🚀 Performance Patterns (Added)

Real-world performance optimisations observed in high-performance Kotlin projects (Kotlinx.coroutines, Arrow, Ktor, OkHttp, etc.).

### 1. Prefer `inline` and `reified` for performance-critical generic functions

```kotlin
// Avoid reified without inlining – not allowed anyway.
inline fun <reified T> List<*>.filterIsInstance(): List<T> {
    return filter { it is T }.map { it as T }
}

// Good: Inline + reified avoids reflection and preserves type at runtime
inline fun <reified T> Any?.castOrNull(): T? = this as? T
```

### 2. Use `value class` (inline class) to eliminate boxing overhead

```kotlin
@JvmInline
value class UserId(val id: Long)   // wraps a primitive, zero runtime overhead

// Bad: Using a data class with a single field (adds object header)
data class UserIdWrapper(val id: Long)
```

### 3. Avoid intermediate collections with `Sequence` for large datasets

```kotlin
// Bad: Creates intermediate lists for each operation (filter, map)
val heavy = users.filter { it.isActive }.map { it.email }.take(1000).toList()

// Good: Lazy evaluation – only one pass
val heavyLazy = users.asSequence()
    .filter { it.isActive }
    .map { it.email }
    .take(1000)
    .toList()
```

### 4. Choose the right coroutine dispatcher

```kotlin
// Bad: Using Main dispatcher for blocking I/O
suspend fun fetchData() = withContext(Dispatchers.Main) {
    blockingFileRead() // freezes UI
}

// Good: Explicit dispatcher for blocking work
suspend fun fetchData() = withContext(Dispatchers.IO) {
    blockingFileRead()
}

// Good: CPU-intensive work on Default
suspend fun computeHeavy() = withContext(Dispatchers.Default) {
    heavyCalculation()
}
```

### 5. Use `SharedFlow`/`StateFlow` instead of `BroadcastChannel` (deprecated) and prefer `replay=0` for performance

```kotlin
// Good: SharedFlow with no replay – low overhead
val events = MutableSharedFlow<Event>(replay = 0, extraBufferCapacity = 64)

// Good: StateFlow as observable state holder
val uiState: StateFlow<UiState> = _uiState.asStateFlow()
```

### 6. String concatenation: use `StringBuilder` in loops

```kotlin
// Bad: Creates new String object each iteration
var result = ""
repeat(1000) { result += "x" }

// Good: Mutable StringBuilder
val sb = StringBuilder()
repeat(1000) { sb.append('x') }
val result = sb.toString()
```

### 7. Mark constants as `const val` for compile-time inlining

```kotlin
// Good: Compile-time constant (primitive or String)
const val DEFAULT_TIMEOUT_MS = 5000L

// Bad: Runtime evaluation
val DEFAULT_TIMEOUT_MS = 5_000L
```

### 8. Prefer `Array` over `List` for primitive heavy processing (JVM)

```kotlin
// Good: Primitive array – no boxing
val ints = IntArray(1000)

// Acceptable: List<Int> boxes each element (unless using Kotlin/Native)
val intList = List(1000) { it }  // each Int is boxed
```

### 9. Use `forEach` only when clarity > performance; `for` loop is faster

```kotlin
// Prefer simple for loop in hot paths
for (i in 0 until list.size) {
    val item = list[i]
    // process
}

// `forEach` has lambda overhead (though often inlined, but not always for large collections)
list.forEach { item -> ... }
```

### 10. Use `@JvmStatic` and `@JvmField` for better interop performance with Java

```kotlin
companion object {
    @JvmStatic
    fun factoryMethod() = ...

    @JvmField
    val CONSTANT = 42
}
```

---

## 🌍 Real-World Patterns from GitHub (Added)

Patterns commonly found in popular Kotlin open-source projects (Ktor, Arrow, Kotlinx.serialization, Detekt, etc.).

### 1. Algebraic Data Types with Sealed Classes (Arrow, Kotlin Result)

```kotlin
// Arrow's Either / Validated pattern
sealed class ValidationError {
    data class FieldError(val field: String, val reason: String) : ValidationError()
    data object GenericError : ValidationError()
}

fun validateEmail(email: String): Either<ValidationError, Email> =
    if ('@' in email) Either.Right(Email(email))
    else Either.Left(ValidationError.FieldError("email", "missing @"))
```

### 2. Type-safe Builders for Tests (Kotest, MockK)

```kotlin
// Kotest's test DSL
class MyTests : StringSpec({
    "should add numbers" {
        1 + 1 shouldBe 2
    }
    "should handle errors" {
        shouldThrow<IllegalArgumentException> {
            require(false)
        }
    }
})
```

### 3. Scoped Receivers for Resource Management (Ktor)

```kotlin
// Ktor's Application DSL
fun Application.module() {
    install(ContentNegotiation) { json() }
    routing {
        get("/hello") { call.respondText("Hello") }
    }
}
```

### 4. Repository Pattern with Coroutines (Android, KMM)

```kotlin
interface UserRepository {
    suspend fun getUser(id: UserId): User?
    fun observeUsers(): Flow<List<User>>
}

class OfflineFirstUserRepository(
    private val local: LocalUserDataSource,
    private val remote: RemoteUserDataSource
) : UserRepository {
    override suspend fun getUser(id: UserId): User? =
        local.getUser(id) ?: remote.getUser(id)?.also { local.save(it) }

    override fun observeUsers(): Flow<List<User>> =
        local.observeUsers().onStart { emit(remote.fetchUsers()) }
}
```

### 5. Using `runTest` and `TestDispatcher` for deterministic coroutine tests

```kotlin
@Test
fun testFetchUser() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val repo = UserRepository(dispatcher = testDispatcher)

    // Advance time manually
    advanceTimeBy(1.seconds)
    val user = repo.getUser("1")
    assertEquals("John", user.name)
}
```

### 6. `inline class` + `typealias` for semantic APIs (without performance loss)

```kotlin
typealias UserId = String   // No type safety

@JvmInline
value class ProductId(val raw: String)  // Compile-time safety, zero overhead

fun findProduct(id: ProductId): Product { ... }
```

---

## Quick Reference: Kotlin Idioms

| Idiom                             | Description                                                       |
|-----------------------------------|-------------------------------------------------------------------|
| `val` over `var`                  | Prefer immutable variables                                        |
| `data class`                      | For value objects with equals/hashCode/copy                       |
| `sealed class`/`interface`        | For restricted type hierarchies                                   |
| `value class`                     | Type-safe wrappers with zero overhead (performance)               |
| Expression `when`                  | Exhaustive pattern matching                                       |
| Safe call `?.`                     | Null-safe member access                                           |
| Elvis `?:`                        | Default value for nullables                                       |
| `let`/`apply`/`also`/`run`/`with` | Scope functions for clean code                                    |
| Extension functions               | Add behaviour without inheritance                                 |
| `copy()`                          | Immutable updates on data classes                                 |
| `require`/`check`                 | Precondition assertions                                           |
| Coroutine `async`/`await`         | Structured concurrent execution                                   |
| `Flow`                            | Cold reactive streams                                             |
| `sequence`                        | Lazy evaluation                                                   |
| Delegation `by`                   | Reuse implementation without inheritance                          |
| `inline` + `reified`              | Generic type reification without reflection (performance)         |
| `const val`                       | Compile-time constant (performance)                               |
| Dispatcher `IO`/`Default`/`Main`  | Correct threading for coroutines (performance + correctness)      |

---

## Anti-Patterns to Avoid

```kotlin
// Bad: Force-unwrapping nullable types
val name = user!!.name

// Bad: Platform type leakage from Java
fun getLength(s: String) = s.length // Safe
fun getLength(s: String?) = s?.length ?: 0 // Handle nulls from Java

// Bad: Mutable data classes
data class MutableUser(var name: String, var email: String)

// Bad: Using exceptions for control flow
try {
    val user = findUser(id)
} catch (e: NotFoundException) {
    // Don't use exceptions for expected cases
}
// Good: Use nullable return or Result
val user: User? = findUserOrNull(id)

// Bad: Ignoring coroutine scope
GlobalScope.launch { /* Avoid GlobalScope */ }
// Good: Use structured concurrency
coroutineScope { launch { /* Properly scoped */ } }

// Bad: Deeply nested scope functions
user?.let { u ->
    u.address?.let { a ->
        a.city?.let { c -> process(c) }
    }
}
// Good: Direct null-safe chain
user?.address?.city?.let { process(it) }
```

---

## Remember

Kotlin code should be **concise but readable**. Leverage the type system for safety, prefer immutability, and use coroutines for concurrency.  
For **performance**, prefer sequences for large collections, inline classes for wrapping primitives, `inline`+`reified` for generics, and the right coroutine dispatcher.  
When in doubt, let the compiler help you – and validate with benchmarks (`kotlinx.benchmark`) for critical paths.
```

--- 

This **KOTLIN.md** file now includes:

- All the original idiomatic Kotlin patterns.
- A dedicated **Performance Patterns** section with concrete, real-world optimisations.
- A **Real-World Patterns from GitHub** section showcasing how popular Kotlin projects structure code.
- Enhanced quick reference and anti-patterns.

Use it as a living style guide for your Kotlin projects. 🚀