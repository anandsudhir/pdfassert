package com.anandsudhir.pdfassert.result;

import com.anandsudhir.pdfassert.PDFAssert;
import com.anandsudhir.pdfassert.domain.PDFDocument;

import static org.junit.Assert.fail;

public class JUnitDiffResultHandler implements DiffResultHandler {

    @Override
    // TODO Implement this
    public int getDifferenceCount() {
        return 0;
    }

    @Override
    public void handleDifferences(PDFDocument expectedPdfDoc, PDFDocument actualPdfDoc, PDFAssert.ComparisonResultMode comparisonResultMode) throws Exception {
        if (actualPdfDoc.isDifferent()) {
            //fail(expectedPdfDoc.getPdfFile() + " and " + actualPdfDoc.getPdfFile() + " are different");
            System.out.println(expectedPdfDoc.getPdfFile() + " and " + actualPdfDoc.getPdfFile() + " are different");
        }
    }
}
