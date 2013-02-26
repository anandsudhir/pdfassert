package com.pdfassert;

import com.pdfassert.diff.TextDiff;
import com.pdfassert.domain.Difference;
import com.pdfassert.domain.PDFDoc;
import com.pdfassert.handler.DiffResultHandler;
import com.pdfassert.handler.SwingDiffResultHandler;
import com.snowtide.pdf.OutputHandler;
import com.snowtide.pdf.PDFTextStream;
import com.snowtide.pdf.Page;
import com.snowtide.pdf.layout.Block;
import com.snowtide.pdf.layout.Line;
import com.snowtide.pdf.layout.Region;
import com.snowtide.pdf.layout.TextUnit;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFAssert {

    private TextDiff textDiff = new TextDiff();
    private DiffResultHandler diffResultHandler;

    private PDFDoc expectedPdfDoc;
    private PDFDoc actualPdfDoc;

    private ComparisonMode comparisonMode;
    private ComparisonResultMode comparisonResultMode;

    public PDFAssert(String expectedFilename, String actualFilename) throws Exception {
        this.expectedPdfDoc = new PDFDoc("src/test/resources/pdfs/" + expectedFilename);
        this.actualPdfDoc = new PDFDoc("src/test/resources/pdfs/" + actualFilename);

        if(GraphicsEnvironment.isHeadless()){
            diffResultHandler = new DiffResultHandler();
        } else {
            diffResultHandler = new SwingDiffResultHandler();
        }

        comparisonMode = ComparisonMode.TEXTUAL;
        comparisonResultMode = ComparisonResultMode.DIFF_UNIFIED;
    }

    public static void main(String[] args) throws Exception {
        PDFAssert test = new PDFAssert("11.pdf", "22.pdf");
        test.comparePDFs();
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

    protected void comparePDFs() throws Exception {
        extractPageTextFromPDF(expectedPdfDoc);
        extractPageTextFromPDF(actualPdfDoc);

        comparePages(expectedPdfDoc, actualPdfDoc);
        showDifferences();
    }

    protected void showDifferences() throws Exception {
        diffResultHandler.showDifferences(expectedPdfDoc, actualPdfDoc, comparisonResultMode);
    }

    protected void extractPageTextFromPDF(PDFDoc pdf) throws Exception {
        PDFTextStream stream = new PDFTextStream(pdf.getPdfFile());
        LocalOutputHandler outputHandler = new LocalOutputHandler();
        stream.pipe(outputHandler);

        pdf.setPages(outputHandler.getPages());
    }

    private void comparePages(PDFDoc expectedPdfDoc, PDFDoc actualPdfDoc) {
        Map<Integer, List<Block>> expectedPages = expectedPdfDoc.getPages();
        Map<Integer, List<Block>> actualPages = actualPdfDoc.getPages();

        if (expectedPages.size() != actualPages.size()) {
            System.err.println("PDFs have different page count");
            System.exit(0);
        }

        for (Integer i : actualPages.keySet()) {
            Difference diff = compareBlocks(expectedPages.get(i), actualPages.get(i));
            if (diff != null && !diff.isEmpty()) {
                expectedPdfDoc.getDifference().merge(diff);
                actualPdfDoc.getDifference().merge(diff);
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
            System.out.println("expected: " + expected);
            System.out.println("actual: " + actual);
            for (Region r : diff.getDiffsInExpected()) {
                System.out.print(((TextUnit) r).getCharacterSequence());
            }
            System.out.println("\n");

            for (Region r : diff.getDiffsInActual()) {
                System.out.print(((TextUnit) r).getCharacterSequence());
            }
            System.out.println("\n");
        }

        return diff;
    }

    protected Block getMostProbableActualBlock(Block block, List<Block> blocksToSearchFrom) {
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
    protected Line getNearestLineWithSimilarContent(Line line, List<Line> linesToSearchFrom) {
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

        double part1 = Math.pow((rect1.getCenterX() - rect2.getCenterX()), 2);
        double part2 = Math.pow((rect1.getCenterY() - rect2.getCenterY()), 2);
        double underRadical = part1 + part2;

        return (float) Math.sqrt(underRadical);
    }

    @SuppressWarnings("unused")
    private float getOverlapArea(Region region1, Region region2) {
        float x11 = region1.xpos(), y11 = region1.ypos(), x12 = region1.endxpos(), y12 = region1.endypos();
        float x21 = region2.xpos(), y21 = region2.ypos(), x22 = region2.endxpos(), y22 = region2.endypos();

        /*float x_overlap = x12 < x21 || x11 > x22 ? 0 : Math.min(x12, x22) - Math.max(x11, x21);
        float y_overlap = y12 < y21 || y11 > y22 ? 0 : Math.min(y12, y22) - Math.max(y11, y21);*/

        float xOverlap = Math.max(0, Math.min(x12, x22) - Math.max(x11, x21));
        float yOverlap = Math.max(0, Math.min(y12, y22) - Math.max(y11, y21));

        return (xOverlap * yOverlap);
    }

    private float getOverlapArea2(Region region1, Region region2) {
        double coverageRect1, coverageRect2;

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

    public enum ComparisonMode {
        TEXTUAL, VISUAL, FULL
    }

    public enum ComparisonResultMode {
        DIFF_SIDE_BY_SIDE, DIFF_UNIFIED
    }

    private static class LocalOutputHandler extends OutputHandler {
        private int currentPage = 0;
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
