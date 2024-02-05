package com.webtracer.parser;

/**
 * The {@code AbstractPageParserFactory} interface serves as the base interface for an Abstract Factory
 * that is responsible for creating instances of {@link PageParser}. The Abstract Factory design pattern
 * allows the creation of families of related objects without specifying their concrete classes.
 *
 * <p>This interface defines a single method, {@link #createParserInstance()}, which must be implemented
 * by concrete factories to produce different types of {@link PageParser} objects. This approach ensures
 * that the client code can remain decoupled from the specific types of parsers that are created, allowing
 * for greater flexibility and scalability.
 *
 * <p>This pattern is particularly useful when the application needs to produce a variety of parsers
 * that share a common interface but differ in their concrete implementations. By using this factory
 * pattern, new parser types can be added easily without modifying existing client code.
 */
public interface AbstractPageParserFactory {

    /**
     * Creates an instance of a {@link PageParser}. The specific type of parser returned by this method
     * is determined by the concrete implementation of the factory. This method is the core of the Abstract
     * Factory pattern, allowing for the creation of various types of parsers without coupling the client
     * code to any specific implementation.
     *
     * @return a new instance of a {@link PageParser}, representing the parser created by this factory.
     */
    PageParser createParserInstance(String url);

}
