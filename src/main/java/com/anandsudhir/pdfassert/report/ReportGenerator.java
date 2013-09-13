package com.anandsudhir.pdfassert.report;

import com.anandsudhir.pdfassert.domain.Difference;
import com.snowtide.pdf.layout.Region;

public interface ReportGenerator {

    void createNewReport(String expectedPDF, String actualPDF);

    void reportDifference(Region expected, Region actual, Difference difference);

    void setQuietMode(QuietMode quietMode);

    public enum QuietMode {
        ON, OFF
    }
}
