package com.anandsudhir.pdfassert.domain;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.snowtide.pdf.layout.Block;
import org.apache.commons.io.FileUtils;

public class PDFDocument {

    private static final String DEFAULT_DIFF_DIRECTORY = "diffs";

    private final File pdfFile;
    private File diffPdfFile;
    private Map<Integer, List<Block>> pages;
    private Map<Integer, Difference> differences = new HashMap<Integer, Difference>();
    private String diffDirectory = DEFAULT_DIFF_DIRECTORY;

    public PDFDocument(File pdfFile) {
        this.pdfFile = pdfFile;
    }

    public File getPdfFile() {
        return pdfFile;
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

    public void setDiffDirectory(String diffDirectory) {
        this.diffDirectory = diffDirectory;
    }

    public File getDiffPdfFile() {
        if (diffPdfFile == null) {
            generateDiffPdfFile();
        }

        return diffPdfFile;
    }

    public boolean isDifferent() {
        return !differences.isEmpty();
    }

    protected void generateDiffPdfFile() {
        diffPdfFile = new File(diffDirectory, pdfFile.getName() + "_" + pdfFile.getParent() + "_diff.pdf");
        try {
            FileUtils.copyFile(pdfFile, diffPdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
