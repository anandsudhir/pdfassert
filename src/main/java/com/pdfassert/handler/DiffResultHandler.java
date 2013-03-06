package com.pdfassert.handler;

import com.pdfassert.PDFAssert;
import com.pdfassert.domain.PDFDocument;

public interface DiffResultHandler {

    public void handleDifferences(PDFDocument expectedPdfDoc, PDFDocument actualPdfDoc,
                                  PDFAssert.ComparisonResultMode comparisonResultMode) throws Exception;
}


