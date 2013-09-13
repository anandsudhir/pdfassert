package com.anandsudhir.pdfassert;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.anandsudhir.pdfassert.domain.PDFDocument;
import com.anandsudhir.pdfassert.report.HTMLReportGenerator;
import com.anandsudhir.pdfassert.result.DiffResultHandler;
import com.anandsudhir.pdfassert.result.JUnitDiffResultHandler;
import com.anandsudhir.pdfassert.result.SwingHighlightingDiffResultHandler;
import org.apache.log4j.Logger;

public class PDFAssert {

    private static final Logger logger = Logger.getLogger(PDFAssert.class.getName());
    private DiffResultHandler diffResultHandler;
    @SuppressWarnings("unused")
    private String diffDirectory;
    @SuppressWarnings("unused")
    private ComparisonMode comparisonMode;
    private ComparisonResultMode comparisonResultMode;
    private List<String> ignorePatterns = new ArrayList<String>();

    public PDFAssert() {
        if (!GraphicsEnvironment.isHeadless()) {
            diffResultHandler = new JUnitDiffResultHandler();
        } else {
            diffResultHandler = new SwingHighlightingDiffResultHandler();
        }

        comparisonMode = ComparisonMode.TEXTUAL;
        comparisonResultMode = ComparisonResultMode.DIFF_SIDE_BY_SIDE;
    }

    public static void main(String[] args) throws Exception {
        checkUsage(args);
        PDFAssert test = new PDFAssert();
        test.setIgnorePatterns(Arrays.asList(".*Demo.*"));
        //test.comparePDFs(args[0], args[1]);
        test.assertSimilarPDFs("pdfs/" + "204_1.pdf", "pdfs/" + "204_2.pdf");

    }

    private static void checkUsage(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar pdfassert.jar <expected pdf> <actual pdf>");
            return;
        }

        if (!new File(args[0]).exists()) {
            logger.error("Expected file does not exist");
        }

        if (!new File(args[1]).exists()) {
            logger.error("Actual file does not exist");
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

    public void assertSimilarPDFs(String expectedFilename, String actualFilename) throws Exception {
        PDFDocument expectedPdfDoc = createPdfDocument(expectedFilename);
        PDFDocument actualPdfDoc = createPdfDocument(actualFilename);

        PDFComparator pdfComparator = new PDFComparator(expectedPdfDoc, actualPdfDoc);
        pdfComparator.setIgnorePatterns(ignorePatterns);

        HTMLReportGenerator reportGenerator = new HTMLReportGenerator();
        pdfComparator.setReportGenerator(reportGenerator);

        pdfComparator.compare();

        diffResultHandler.handleDifferences(pdfComparator.getExpectedPdfDoc(), pdfComparator.getActualPdfDoc(),
                comparisonResultMode);

        Writer out = new FileWriter(new File("report.html"));
        out.write(reportGenerator.getReport());
        out.flush();
        out.close();

        logger.info("Done");
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
