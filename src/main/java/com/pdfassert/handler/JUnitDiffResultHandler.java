package com.pdfassert.handler;

import com.pdfassert.PDFAssert;
import com.pdfassert.domain.PDFDocument;

import static org.junit.Assert.fail;

public class JUnitDiffResultHandler implements DiffResultHandler {

    @Override
    public void handleDifferences(PDFDocument expectedPdfDoc, PDFDocument actualPdfDoc, PDFAssert.ComparisonResultMode comparisonResultMode) throws Exception {
        if (actualPdfDoc.isDifferent()) {
            fail(expectedPdfDoc.getPdfFile() + " and " + actualPdfDoc.getPdfFile() + " are different");
        }
    }
}
