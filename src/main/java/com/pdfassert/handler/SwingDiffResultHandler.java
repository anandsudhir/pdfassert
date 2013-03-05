package com.pdfassert.handler;

import com.pdfassert.PDFAssert;
import com.pdfassert.domain.PDFDocument;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;

public class SwingDiffResultHandler extends DiffResultHandler {

    @Override
    public void showDifferences(PDFDocument expectedPdfDoc, PDFDocument actualPdfDoc, PDFAssert.ComparisonResultMode comparisonResultMode) throws Exception {
        if(!actualPdfDoc.isDifferent()) {
            System.out.println("No differences found between pdfs");
            return;
        }

        super.showDifferences(expectedPdfDoc, actualPdfDoc, comparisonResultMode);

        if (comparisonResultMode == PDFAssert.ComparisonResultMode.DIFF_SIDE_BY_SIDE) {
                showDifferences(expectedPdfDoc.getDiffPdfFile(), actualPdfDoc.getDiffPdfFile());
        } else {
                showDifferences(actualPdfDoc.getDiffPdfFile());
        }
    }

    private void showDifferences(File expectedPdfFile, File actualPdfFile) {
        JFrame applicationFrame = new JFrame();
        GridLayout layout = new GridLayout(1, 2);
        applicationFrame.setLayout(layout);

        JPanel expectedPdfPanel = getPDFPanel(expectedPdfFile);
        JPanel actualPdfPanel = getPDFPanel(actualPdfFile);

        applicationFrame.getContentPane().add(expectedPdfPanel, BorderLayout.WEST);
        applicationFrame.getContentPane().add(actualPdfPanel, BorderLayout.EAST);

        applicationFrame.pack();
        applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applicationFrame.setVisible(true);
    }

    private void showDifferences(File actualPdfFile) {
        JFrame applicationFrame = new JFrame();

        JPanel actualPdfPanel = getPDFPanel(actualPdfFile);
        applicationFrame.getContentPane().add(actualPdfPanel, BorderLayout.CENTER);

        applicationFrame.pack();
        applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applicationFrame.setVisible(true);
    }

    private JPanel getPDFPanel(File pdfFile) {
        SwingController controller = new SwingController();
        SwingViewBuilder factory = new SwingViewBuilder(controller);
        JPanel viewerComponentPanel = factory.buildViewerPanel();

        ComponentKeyBinding.install(controller, viewerComponentPanel);
        controller.getDocumentViewController().setAnnotationCallback(
                new org.icepdf.ri.common.MyAnnotationCallback(
                        controller.getDocumentViewController()));

        controller.openDocument(pdfFile.getAbsolutePath());

        return viewerComponentPanel;
    }

    @SuppressWarnings("unused")
    private void createSynchronizedScrollingPanels(JPanel expectedPdfPanel, JPanel actualPdfPanel) {
        JFrame applicationFrame = new JFrame();
        applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JScrollPane jsp1 = new JScrollPane(expectedPdfPanel);
        JScrollPane jsp2 = new JScrollPane(actualPdfPanel);

        final JScrollBar vScroll1 = jsp1.getVerticalScrollBar();
        final JScrollBar vScroll2 = jsp2.getVerticalScrollBar();

        vScroll1.addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!e.getValueIsAdjusting()) {
                    return;
                }

                int range1 = vScroll1.getMaximum() - vScroll1.getMinimum()
                        - vScroll1.getModel().getExtent();
                int range2 = vScroll2.getMaximum() - vScroll2.getMinimum()
                        - vScroll2.getModel().getExtent();

                float percent = (float) (vScroll1.getValue()) / range1;

                int newVal = (int) (percent * range2);

                vScroll2.setValue(newVal);
            }

        });

        vScroll2.addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!e.getValueIsAdjusting()) {
                    return;
                }

                int range1 = vScroll1.getMaximum() - vScroll1.getMinimum()
                        - vScroll1.getModel().getExtent();
                int range2 = vScroll2.getMaximum() - vScroll2.getMinimum()
                        - vScroll2.getModel().getExtent();

                float percent = (float) vScroll2.getValue() / range2;

                int newVal = (int) (percent * range1);

                vScroll1.setValue(newVal);
            }

        });

        applicationFrame.getContentPane().setLayout(new GridLayout(1, 2));

        applicationFrame.getContentPane().add(jsp1);
        applicationFrame.getContentPane().add(jsp2);

        applicationFrame.pack();
        applicationFrame.setVisible(true);
    }
}
