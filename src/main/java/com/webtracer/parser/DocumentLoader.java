package com.webtracer.parser;

import com.webtracer.ApiException;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.util.Optional;

/**
 * The {@code DocumentLoader} interface defines a contract for loading a JSoup {@link Document}
 * from a given {@link URI}. Implementations of this interface should handle the specifics
 * of retrieving the document content, whether from a local file or a remote server, and return
 * an {@link Optional<Document>} that contains the parsed document.
 * <p>
 * If the document cannot be loaded for any reason (e.g., the URI is invalid, the remote server
 * is unreachable, or an I/O error occurs), the method should throw an {@link ApiException}.
 * <p>
 * This interface allows for injecting different document loading strategies, which is particularly
 * useful in testing scenarios where you may want to mock or simulate various document retrieval behaviors.
 *
 */
public interface DocumentLoader {

    /**
     * Loads a JSoup {@link Document} from the given {@link URI}.
     * <p>
     * This method attempts to retrieve the content of the document located at the specified URI
     * and parse it into a {@link Document} object. The resulting {@link Document} is returned
     * inside an {@link Optional}. If the document cannot be loaded, an {@link ApiException}
     * is thrown to indicate the failure.
     * <p>
     * Implementations of this method should handle various potential issues, such as network errors,
     * invalid URIs, or file I/O problems, and should ensure that appropriate exceptions are thrown.
     *
     * @param uri the {@link URI} of the document to be loaded
     * @return an {@link Optional<Document>} containing the parsed document if successful
     * @throws ApiException if the document cannot be loaded due to an error (e.g., I/O error, invalid URI)
     */
    Optional<Document> loadDocument(URI uri) throws ApiException;

}