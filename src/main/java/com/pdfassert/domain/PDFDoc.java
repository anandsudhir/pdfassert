package com.pdfassert.domain;

import com.snowtide.pdf.layout.Block;

import java.io.File;
import java.util.List;
import java.util.Map;

public class PDFDoc {

    private final File pdfFile;
    private File diffPdfFile;
    private Map<Integer, List<Block>> pages;
    private Difference difference;

    public PDFDoc(String path) {
        this(new File(path));
    }

    public PDFDoc(File pdfFile) {
        this.pdfFile = pdfFile;
        difference = new Difference();
    }

    public File getPdfFile() {
        return pdfFile;
    }

    public File getDiffPdfFile() {
        if(diffPdfFile == null){
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

    public Difference getDifference() {
        return difference;
    }

    private void generateDiffPdfFile(){
        diffPdfFile = new File(pdfFile + "_diff.pdf");
    }
}
