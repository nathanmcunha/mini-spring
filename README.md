# Mini-Spring Framework

**Mini-Spring** is a lightweight, educational Java framework built from scratch. It mimics the core principles of the Spring Framework (Dependency Injection & Web MVC) but implements them using modern Java 21 features and a **"Functional Core, Imperative Shell"** architecture.

> **Goal:** To understand *how* frameworks work by building one, focusing on clean architecture, immutability, and "Honest" functional programming patterns.

> **Disclaimer**: You can find some code inconsistencies, like using the switch pattern (beautiful stuff) and some others using 'if' statements. This is on purpose to serve as an example, because this project will be used for training in the future.

## 🚀 Key Features

*   **Dependency Injection (DI) Container**:
    *   **Robust Resolution**: Recursive dependency resolution with interface support and ambiguity detection.
    *   **Circular Dependency Detection**: Prevents infinite recursion during bean instantiation.
    *   **Component Scanning**: Automatic discovery of `@Component` and `@Rest` beans.
    *   **Thread-Safe**: Registry backed by `ConcurrentHashMap` for safe runtime access.
*   **Web MVC Layer**:
    *   **Honest Functions**: Method signatures tell the whole truth. Control flow is handled via the `Result<T, E>` monad instead of exceptions.
    *   **Action-Based Routing**: Clean separation between `Router` (finding a handler) and `RouteAction` (executing it).
    *   **Exhaustive Pattern Matching**: The `DispatcherServlet` leverages Java 21 switch expressions to explicitly handle every success and failure case (e.g., 404s, 500s) at the "End of the World".
*   **Modern Java**: Built with Java 21, utilizing Records, Sealed Interfaces, Virtual Threads, and Pattern Matching for Switch.

## 🏗️ Architecture

This project deliberately avoids the standard "Service/Impl" package structure in favor of an **Action-Based Organization**:

```text
com.nathanmcunha.minispring
├── common              # Shared primitives (Result Monad)
├── container           # The DI Engine
│   ├── boot            # Context startup & lifecycle (MiniApplicationContext)
│   ├── wiring          # Dependency graph resolution (DependencyResolver)
│   ├── registry        # Bean storage & retrieval (DefaultBeanFactory)
│   ├── discovery       # Classpath scanning (ComponentScanner)
│   └── metadata        # BeanDefinition models
├── server              # The Web Layer
│   ├── dispatch        # DispatcherServlet (Imperative Shell)
│   │   └── protocol    # HTTP models (Response, HttpStatus)
│   └── router          # Routing logic (Router, RouterRegistry)
└── error               # Consolidated FrameworkError sealed hierarchy
```

## 🛠️ Getting Started

### Prerequisites
*   Java 21 or higher
*   Gradle (wrapper included)

### Building the Project
```bash
./gradlew build
```

### Running the Tests
```bash
./gradlew test
```

### Running the Example App
The project includes a sample `MiniSpringApp` that starts a server on port 8080.

```bash
./gradlew run
```

Once running, you can test the endpoints (defined in test components):
```bash
curl http://localhost:8080/getTest
```

## 📝 Example Usage

### Creating a Controller
Just annotate your class with `@Rest` and methods with `@Get` or `@Post`.

```java
@Rest
public class MyController {

    @Get("/hello")
    public String sayHello() {
        return "Hello, World!";
    }

    @Get("/json")
    public Response<MyData> getJson() {
        return Response.Builder(HttpStatus.OK.value())
                       .body(new MyData("data"));
    }
}
```

### Creating a Service
Use `@Component` to mark a class as a bean. It will be automatically injected where needed.

```java
@Component
public class MyService {
    public String doSomething() {
        return "Done!";
    }
}
```

## 🤝 Contributing
This is an educational project, but suggestions and improvements are welcome! Feel free to open issues to discuss architecture or refactoring ideas.

## 📄 License
MIT

---
*Disclaimer: This README and the project's architectural documentation were generated with the assistance of Google Gemini.*
