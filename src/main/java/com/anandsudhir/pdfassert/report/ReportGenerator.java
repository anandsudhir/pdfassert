package com.anandsudhir.pdfassert.report;

import com.anandsudhir.pdfassert.domain.Difference;
import com.snowtide.pdf.layout.Region;

public interface ReportGenerator {

    public void createNewReport(String expectedPDF, String actualPDF);

    public void reportDifference(Region expected, Region actual, Difference difference);

    public void setQuietMode(QuietMode quietMode);

    public enum QuietMode {
        ON, OFF
    }
}
