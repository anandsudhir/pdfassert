package com.pdfassert;

import com.pdfassert.domain.PDFDocument;
import com.pdfassert.handler.DiffResultHandler;
import com.pdfassert.handler.SwingDiffResultHandler;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PDFAssert {

    private DiffResultHandler diffResultHandler;
    private String diffDirectory;
    private ComparisonMode comparisonMode;
    private ComparisonResultMode comparisonResultMode;
    private List<String> ignorePatterns = new ArrayList<String>();


    public PDFAssert() {
        if (GraphicsEnvironment.isHeadless()) {
            diffResultHandler = new DiffResultHandler();
        } else {
            diffResultHandler = new SwingDiffResultHandler();
        }

        comparisonMode = ComparisonMode.TEXTUAL;
        comparisonResultMode = ComparisonResultMode.DIFF_UNIFIED;
    }

    public static void main(String[] args) throws Exception {
        PDFAssert test = new PDFAssert();
        test.setIgnorePatterns(Arrays.asList(".*blah.*"));
        test.comparePDFs("11.pdf", "22.pdf");
    }

    public void setDiffResultHandler(DiffResultHandler diffResultHandler) {
        this.diffResultHandler = diffResultHandler;
    }

    public void setComparisonResultMode(ComparisonResultMode comparisonResultMode) {
        this.comparisonResultMode = comparisonResultMode;
    }

    public void setComparisonMode(ComparisonMode comparisonMode) {
        this.comparisonMode = comparisonMode;
    }

    public void setIgnorePatterns(List<String> ignorePatterns) {
        this.ignorePatterns = ignorePatterns;
    }

    protected void comparePDFs(String expectedFilename, String actualFilename) throws Exception {
        PDFDocument expectedPdfDoc = createPdfDocument(expectedFilename);
        PDFDocument actualPdfDoc = createPdfDocument(actualFilename);

        PDFComparator pdfComparator = new PDFComparator(expectedPdfDoc, actualPdfDoc);
        pdfComparator.setIgnorePatterns(ignorePatterns);

        pdfComparator.compare();

        diffResultHandler.showDifferences(pdfComparator.getExpectedPdfDoc(), pdfComparator.getActualPdfDoc(), comparisonResultMode);
    }

    private PDFDocument createPdfDocument(String filePath) {
        File file = new File("src/test/resources/pdfs/" + filePath);
        return new PDFDocument(file);
    }

    public enum ComparisonMode {
        TEXTUAL, VISUAL, FULL
    }

    public enum ComparisonResultMode {
        DIFF_SIDE_BY_SIDE, DIFF_UNIFIED
    }

}
