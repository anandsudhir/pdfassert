package com.anandsudhir.pdfassert;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.anandsudhir.pdfassert.diff.TextDiff;
import com.anandsudhir.pdfassert.domain.Difference;
import com.anandsudhir.pdfassert.domain.PDFDocument;
import com.anandsudhir.pdfassert.report.ConsoleReportGenerator;
import com.anandsudhir.pdfassert.report.ReportGenerator;
import com.snowtide.pdf.OutputHandler;
import com.snowtide.pdf.PDFTextStream;
import com.snowtide.pdf.Page;
import com.snowtide.pdf.layout.Block;
import com.snowtide.pdf.layout.Line;
import com.snowtide.pdf.layout.Region;
import org.apache.log4j.Logger;

public class PDFComparator {

    private static Logger logger = Logger.getLogger(PDFComparator.class.getName());
    private TextDiff textDiff = new TextDiff();
    private PDFDocument expectedPdfDoc;
    private PDFDocument actualPdfDoc;
    private ReportGenerator reportGenerator;

    public PDFComparator(PDFDocument expectedPdfDoc, PDFDocument actualPdfDoc) {
        this.expectedPdfDoc = expectedPdfDoc;
        this.actualPdfDoc = actualPdfDoc;
        this.reportGenerator = new ConsoleReportGenerator();
        reportGenerator.setQuietMode(ReportGenerator.QuietMode.OFF);

        // Use log4j instead of the defalut/custom implementation from pdfts
        System.setProperty("pdfts.loggingtype", "log4j");
    }

    public PDFDocument getExpectedPdfDoc() {
        return expectedPdfDoc;
    }

    public PDFDocument getActualPdfDoc() {
        return actualPdfDoc;
    }

    public void setReportGenerator(ReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    public void setIgnorePatterns(List<String> ignorePatterns) {
        textDiff.setIgnorePatterns(ignorePatterns);
    }

    public void compare() throws Exception {
        reportGenerator.createNewReport(expectedPdfDoc.getPdfFile().getName(), actualPdfDoc.getPdfFile().getName());

        extractPageTextFromPDF(expectedPdfDoc);
        extractPageTextFromPDF(actualPdfDoc);

        comparePages(expectedPdfDoc, actualPdfDoc);
    }

    private void extractPageTextFromPDF(PDFDocument pdf) throws Exception {
        PDFTextStream stream = new PDFTextStream(pdf.getPdfFile());
        LocalOutputHandler outputHandler = new LocalOutputHandler();
        stream.pipe(outputHandler);

        pdf.setPages(outputHandler.getPages());
    }

    private void comparePages(PDFDocument expectedPdfDoc, PDFDocument actualPdfDoc) {
        Map<Integer, List<Block>> expectedPages = expectedPdfDoc.getPages();
        Map<Integer, List<Block>> actualPages = actualPdfDoc.getPages();

        if (expectedPages.size() != actualPages.size()) {
            logger.info("PDFs have different page count");
            return;
        }

        for (Map.Entry<Integer, List<Block>> entry : expectedPages.entrySet()) {
            Difference diff = compareBlocks(entry.getValue(), actualPages.get(entry.getKey()));
            if (diff != null && !diff.isEmpty()) {
                expectedPdfDoc.getDifferences().put(entry.getKey(), diff);
                actualPdfDoc.getDifferences().put(entry.getKey(), diff);
            }
        }
    }

    private Difference compareBlocks(List<Block> expectedBlocks, List<Block> actualBlocks) {
        Difference difference = new Difference();
        for (Block expectedBlock : expectedBlocks) {
            Block mostProbableActualBlock = getMostProbableActualBlock(expectedBlock, actualBlocks);
            difference.merge(compareBlocks(expectedBlock, mostProbableActualBlock));
        }

        return difference;
    }

    // TODO: Handle overflow
    @SuppressWarnings("unchecked")
    private Difference compareBlocks(Block expectedBlock, Block actualBlock) {
        Difference difference = new Difference();
        for (int i = 0; i < expectedBlock.getLineCnt(); i++) {
            Line mostProbableActualLine = getMostProbableActualLine(expectedBlock.getLine(i), actualBlock.getLines());
            // TODO: double check this logic
            if (mostProbableActualLine == null) {
                difference.merge(compareLines(expectedBlock.getLine(i), actualBlock.getLine(i)));
            }

            difference.merge(compareLines(expectedBlock.getLine(i), mostProbableActualLine));
        }

        return difference;
    }

    private Difference compareLines(Line expected, Line actual) {
        Difference diff = textDiff.getDifferences(expected, actual);
        if (diff != null && !diff.isEmpty()) {
            reportGenerator.reportDifference(expected, actual, diff);
        }

        return diff;
    }

    private Block getMostProbableActualBlock(Block block, List<Block> blocksToSearchFrom) {
        Block mostProbableActualBlock;

        mostProbableActualBlock = (Block) getRegionWithHighestCoverage(block, blocksToSearchFrom);

        if (mostProbableActualBlock == null) {
            mostProbableActualBlock = (Block) getNearestRegion(block, blocksToSearchFrom);
        }

        return mostProbableActualBlock;
    }

    // TODO: Implement this feature
    private Line getMostProbableActualLine(Line line, List<Line> linesToSearchFrom) {
        return getNearestLineWithSimilarContent(line, linesToSearchFrom);
    }

    // TODO: Implement this feature
    private Line getNearestLineWithSimilarContent(Line line, List<Line> linesToSearchFrom) {
        Line mostProbableActualLine = null;

        int levenshteinDistance = Integer.MAX_VALUE;
        for (Line line2 : linesToSearchFrom) {
            int d = textDiff.getLevenshteinDistance(line, line2);
            if (d < levenshteinDistance) {
                levenshteinDistance = d;
                mostProbableActualLine = line2;
            }
        }

        return mostProbableActualLine;
    }

    private Region getRegionWithHighestCoverage(Region region1, List<? extends Region> regionsToSearchFrom) {
        Region blockWithHighestCoverage = null;
        float highestCoverage = 0f;
        for (Region region2 : regionsToSearchFrom) {
            float coverage = getOverlapArea2(region1, region2);
            if (coverage > highestCoverage) {
                highestCoverage = coverage;
                blockWithHighestCoverage = region2;
            }
        }

        return blockWithHighestCoverage;
    }

    private Region getNearestRegion(Region region1, List<? extends Region> regionsToSearchFrom) {
        Region nearestRegion = null;
        float leastDistance = Float.MAX_VALUE;

        for (Region region2 : regionsToSearchFrom) {
            float distance = getDistance(region1, region2);
            if (distance < leastDistance) {
                leastDistance = distance;
                nearestRegion = region2;
            }
        }

        return nearestRegion;
    }

    private float getDistance(Region region1, Region region2) {
        Rectangle2D rect1 = new Rectangle2D.Double(region1.xpos(), region1.ypos(), region1.width(), region1.height());
        Rectangle2D rect2 = new Rectangle2D.Double(region2.xpos(), region2.ypos(), region2.width(), region2.height());

        double part1 = Math.pow(rect1.getCenterX() - rect2.getCenterX(), 2);
        double part2 = Math.pow(rect1.getCenterY() - rect2.getCenterY(), 2);
        double underRadical = part1 + part2;

        return (float) Math.sqrt(underRadical);
    }

    @SuppressWarnings("unused")
    private float getOverlapArea(Region region1, Region region2) {
        float x11 = region1.xpos();
        float y11 = region1.ypos();
        float x12 = region1.endxpos();
        float y12 = region1.endypos();
        float x21 = region2.xpos();
        float y21 = region2.ypos();
        float x22 = region2.endxpos();
        float y22 = region2.endypos();

        /*float x_overlap = x12 < x21 || x11 > x22 ? 0 : Math.min(x12, x22) - Math.max(x11, x21);
        float y_overlap = y12 < y21 || y11 > y22 ? 0 : Math.min(y12, y22) - Math.max(y11, y21);*/

        float xOverlap = Math.max(0, Math.min(x12, x22) - Math.max(x11, x21));
        float yOverlap = Math.max(0, Math.min(y12, y22) - Math.max(y11, y21));

        return xOverlap * yOverlap;
    }

    private float getOverlapArea2(Region region1, Region region2) {
        double coverageRect1;
        double coverageRect2;

        Rectangle2D rect1 = new Rectangle2D.Double(region1.xpos(), region1.ypos(), region1.width(), region1.height());
        Rectangle2D rect2 = new Rectangle2D.Double(region2.xpos(), region2.ypos(), region2.width(), region2.height());

        if (!rect1.intersects(rect2)) {
            return 0.0f;
        }

        Rectangle2D result = new Rectangle2D.Float();
        Rectangle2D.intersect(rect1, rect2, result);

        double resultArea = result.getWidth() * result.getHeight();
        coverageRect1 = resultArea / (rect1.getWidth() * rect1.getHeight());
        coverageRect2 = resultArea / (rect2.getWidth() * rect2.getHeight());

        return (float) Math.min(coverageRect1, coverageRect2);
    }

    private static class LocalOutputHandler extends OutputHandler {
        private int currentPage;
        private Map<Integer, List<Block>> pages = new HashMap<Integer, List<Block>>();
        private List<Block> blocks = new ArrayList<Block>();

        public Map<Integer, List<Block>> getPages() {
            return pages;
        }

        @Override
        public void startBlock(Block block) {
            blocks.add(block);
        }

        @Override
        public void startPage(Page page) {
            currentPage++;
        }

        @Override
        public void endPage(Page page) {
            pages.put(currentPage, blocks);
            blocks = new ArrayList<Block>();
        }
    }
}
