package com.anandsudhir.pdfassert.result;

import com.anandsudhir.pdfassert.PDFAssert;
import com.anandsudhir.pdfassert.domain.PDFDocument;

public interface DiffResultHandler {

    public int getDifferenceCount();

    public void handleDifferences(PDFDocument expectedPdfDoc, PDFDocument actualPdfDoc,
                                  PDFAssert.ComparisonResultMode comparisonResultMode) throws Exception;
}


