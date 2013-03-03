package com.pdfassert.handler;

import com.pdfassert.PDFAssert;
import com.pdfassert.domain.Difference;
import com.pdfassert.domain.PDFDocument;
import com.snowtide.pdf.layout.Region;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class DiffResultHandler {

    public void showDifferences(PDFDocument expectedPdfDoc, PDFDocument actualPdfDoc, PDFAssert.ComparisonResultMode comparisonResultMode) throws Exception {
        if (comparisonResultMode == PDFAssert.ComparisonResultMode.DIFF_UNIFIED) {
            for (Map.Entry<Integer, Difference> entry : actualPdfDoc.getDifferences().entrySet()) {
                drawDifferences(actualPdfDoc.getPdfFile().getAbsolutePath(), actualPdfDoc.getDiffPdfFile().getAbsolutePath(), entry.getValue().getDiffsInActual());
                drawDifferences(actualPdfDoc.getPdfFile().getAbsolutePath(), actualPdfDoc.getDiffPdfFile().getAbsolutePath(), entry.getValue().getDiffsInExpected());
            }
        } else if (comparisonResultMode == PDFAssert.ComparisonResultMode.DIFF_SIDE_BY_SIDE) {
            for (Map.Entry<Integer, Difference> entry : actualPdfDoc.getDifferences().entrySet()) {
                drawDifferences(actualPdfDoc.getPdfFile().getAbsolutePath(), actualPdfDoc.getDiffPdfFile().getAbsolutePath(), entry.getValue().getDiffsInActual());
            }

            for (Map.Entry<Integer, Difference> entry : expectedPdfDoc.getDifferences().entrySet()) {
                drawDifferences(expectedPdfDoc.getPdfFile().getAbsolutePath(), expectedPdfDoc.getDiffPdfFile().getAbsolutePath(), entry.getValue().getDiffsInExpected());
            }
        }
    }

    private void drawDifferences(String pdf, String diffPdf, List<Region> regions) throws Exception {
        PDDocument doc = null;
        try {
            doc = PDDocument.load(pdf);

            @SuppressWarnings("unchecked")
            List<PDPage> pages = doc.getDocumentCatalog().getAllPages();

            for (PDPage page : pages) {
                PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true, true);

                contentStream.setNonStrokingColor(Color.GREEN);
                contentStream.setStrokingColor(Color.RED);
                contentStream.moveTo(0, 0);

                /* C -------------------- D
                *    |                   |
                *    |                   |
                *    |                   |
                *    |                   |
                *    |                   |
                *  A -------------------- B
                */
                for (Region region : regions) {
                    // Horizontal Start -- > End ( A--> B)
                    contentStream.drawLine(
                            region.xpos(),
                            region.ypos(),
                            region.endxpos(),
                            region.ypos()
                    );

                    // Vertical End -- > End ( B--> D)
                    contentStream.drawLine(
                            region.endxpos(),
                            region.ypos(),
                            region.endxpos(),
                            region.endypos()
                    );

                    // Horizontal End -- > Start ( D --> C)
                    contentStream.drawLine(
                            region.endxpos(),
                            region.endypos(),
                            region.xpos(),
                            region.endypos()
                    );

                    // Vertical Start -- > Start
                    contentStream.drawLine(
                            region.xpos(),
                            region.endypos(),
                            region.xpos(),
                            region.ypos()
                    );
                }

                contentStream.close();
            }

            doc.save(diffPdf);
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }

}