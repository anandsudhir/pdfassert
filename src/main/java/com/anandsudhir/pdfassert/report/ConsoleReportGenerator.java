package com.anandsudhir.pdfassert.report;

import com.anandsudhir.pdfassert.domain.Difference;
import com.snowtide.pdf.layout.Line;
import com.snowtide.pdf.layout.Region;
import com.snowtide.pdf.layout.TextUnit;

public class ConsoleReportGenerator implements ReportGenerator {

    private QuietMode quietMode;

    @Override
    public void setQuietMode(QuietMode quietMode) {
        this.quietMode = quietMode;
    }

    @Override
    public void createNewReport(String expectedPDF, String actualPDF) {
        // Nothing to do here!
    }

    @Override
    public void reportDifference(Region expected, Region actual, Difference difference) {
        if (quietMode == QuietMode.ON) {
            return;
        }

        if (expected instanceof Line && actual instanceof Line) {
            reportLineDifference(expected, actual, difference);
        }
    }

    protected void reportLineDifference(Region expected, Region actual, Difference difference) {
        System.out.println("expected: " + expected);
        System.out.println("actual: " + actual);
        for (Region r : difference.getDiffsInExpected()) {
            System.out.print(((TextUnit) r).getCharacterSequence());
        }
        System.out.println("\n");

        for (Region r : difference.getDiffsInActual()) {
            System.out.print(((TextUnit) r).getCharacterSequence());
        }
        System.out.println("\n");
    }
}
