package com.pdfassert.domain;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.snowtide.pdf.layout.Block;

import org.apache.commons.io.FileUtils;

public class PDFDocument {

    private final File pdfFile;
    private File diffPdfFile;
    private Map<Integer, List<Block>> pages;
    private Map<Integer, Difference> differences;

    public PDFDocument(File pdfFile) {
        this.pdfFile = pdfFile;
        differences = new HashMap<Integer, Difference>();
    }

    public File getPdfFile() {
        return pdfFile;
    }

    public File getDiffPdfFile() {
        if (diffPdfFile == null) {
            generateDiffPdfFile();
        }

        return diffPdfFile;
    }

    public Map<Integer, List<Block>> getPages() {
        return pages;
    }

    public void setPages(Map<Integer, List<Block>> pages) {
        this.pages = pages;
    }

    public Map<Integer, Difference> getDifferences() {
        return differences;
    }

    private void generateDiffPdfFile() {
        diffPdfFile = new File(pdfFile + "_diff.pdf");
        try {
            FileUtils.copyFile(pdfFile, diffPdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
