package com.xpinjection.ai.conventions;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class ConventionsIndexingService {
    private static final String DOCUMENTS_PATH = "classpath:conventions";
    private static final String PDF_FILES = "*.pdf";
    private static final String MARKDOWN_FILES = "*.md";

    private final ResourcePatternResolver resourcePatternResolver;
    private final VectorStore vectorStore;

    public ConventionsIndexingService(ResourcePatternResolver resourcePatternResolver,
                                      VectorStore vectorStore) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.vectorStore = vectorStore;
    }

    public void index(ConventionsMode mode) {
        var documents = extractDocuments(mode);
        documents.forEach(doc -> doc.getMetadata().put("mode", mode.name()));
        vectorStore.add(documents);
    }

    private List<Document> extractDocuments(ConventionsMode mode) {
        return switch (mode) {
            case PDF -> readDocuments(PDF_FILES, PagePdfDocumentReader::new);
            case PDF_PARAGRAPH -> readDocuments(PDF_FILES, ParagraphPdfDocumentReader::new);
            case MARKDOWN -> readDocuments(MARKDOWN_FILES,
                    resource -> new MarkdownDocumentReader(resource, markdownConfig(resource)));
        };
    }

    private static MarkdownDocumentReaderConfig markdownConfig(Resource resource) {
        return MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(false)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("file_name", resource.getFilename())
                .build();
    }

    private List<Document> readDocuments(String filesPattern, Function<Resource, DocumentReader> readerSupplier) {
        var resources = getResources(filesPattern);
        return Arrays.stream(resources)
                .map(readerSupplier)
                .map(Supplier::get)
                .flatMap(List::stream)
                .toList();
    }

    private Resource[] getResources(String pattern) {
        try {
            return resourcePatternResolver.getResources(DOCUMENTS_PATH + "/" + pattern);
        } catch (IOException e) {
            throw new IllegalStateException("Can't read resources from " + DOCUMENTS_PATH + " with pattern " + pattern, e);
        }
    }
}
