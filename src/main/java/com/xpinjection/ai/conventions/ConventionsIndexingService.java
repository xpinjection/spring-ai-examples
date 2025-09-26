package com.xpinjection.ai.conventions;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class ConventionsIndexingService {
    private static final String PDF_DOCUMENTS_PATH = "classpath:conventions";

    private final ResourcePatternResolver resourcePatternResolver;
    private final VectorStore vectorStore;

    public ConventionsIndexingService(ResourcePatternResolver resourcePatternResolver,
                                      VectorStore vectorStore) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.vectorStore = vectorStore;
    }

    public void index() {
        var allDocs = extractDocuments();

        vectorStore.add(allDocs);
    }

    private List<Document> extractDocuments() {
        var resources = getPdfResources();
        return Arrays.stream(resources)
                .map(PagePdfDocumentReader::new)
                .map(PagePdfDocumentReader::get)
                .flatMap(List::stream)
                .toList();
    }

    private Resource[] getPdfResources() {
        try {
            return resourcePatternResolver.getResources(PDF_DOCUMENTS_PATH + "/*.pdf");
        } catch (IOException e) {
            throw new IllegalStateException("Can't read PDF resources from " + PDF_DOCUMENTS_PATH, e);
        }
    }
}
