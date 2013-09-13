package com.anandsudhir.pdfassert.result;

import com.anandsudhir.pdfassert.PDFAssert;
import com.anandsudhir.pdfassert.domain.PDFDocument;

public interface DiffResultHandler {

    int getDifferenceCount();

    void handleDifferences(PDFDocument expectedPdfDoc, PDFDocument actualPdfDoc,
                                  PDFAssert.ComparisonResultMode comparisonResultMode) throws Exception;
}


