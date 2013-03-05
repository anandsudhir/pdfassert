package com.pdfassert;

import com.pdfassert.domain.PDFDocument;
import com.pdfassert.handler.DiffResultHandler;
import com.pdfassert.handler.SwingDiffResultHandler;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PDFAssert {

    static Logger logger = Logger.getLogger(PDFAssert.class.getName());

    private DiffResultHandler diffResultHandler;
    @SuppressWarnings("unused")
    private String diffDirectory;
    @SuppressWarnings("unused")
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
        comparisonResultMode = ComparisonResultMode.DIFF_SIDE_BY_SIDE;
    }

    public static void main(String[] args) throws Exception {
        checkUsage(args);
        PDFAssert test = new PDFAssert();
        test.setIgnorePatterns(Arrays.asList(".*Demo.*"));
        //test.comparePDFs(args[0], args[1]);
         test.comparePDFs("src/test/resources/pdfs/" + "204_1.pdf", "src/test/resources/pdfs/" + "204_2.pdf");

    }

    private static void checkUsage(String[] args) {
        if(args.length < 1) {
            System.out.println("Usage: java -jar pdfassert.jar <expected pdf> <actual pdf>");
            return;
        }

        if (!new File(args[0]).exists()){
            logger.fatal("Expected file does not exist");
        }

        if(!new File(args[1]).exists()) {
            logger.fatal("Actual file does not exist");
        }
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

    public void comparePDFs(String expectedFilename, String actualFilename) throws Exception {
        PDFDocument expectedPdfDoc = createPdfDocument(expectedFilename);
        PDFDocument actualPdfDoc = createPdfDocument(actualFilename);

        PDFComparator pdfComparator = new PDFComparator(expectedPdfDoc, actualPdfDoc);
        pdfComparator.setIgnorePatterns(ignorePatterns);

        pdfComparator.compare();

        diffResultHandler.showDifferences(pdfComparator.getExpectedPdfDoc(), pdfComparator.getActualPdfDoc(),
                comparisonResultMode);
    }

    private PDFDocument createPdfDocument(String filePath) {
        // File file = new File("src/test/resources/pdfs/" + filePath);
        File file = new File(filePath);
        return new PDFDocument(file);
    }

    public enum ComparisonMode {
        TEXTUAL, VISUAL, FULL
    }

    public enum ComparisonResultMode {
        DIFF_SIDE_BY_SIDE, DIFF_UNIFIED
    }

}
