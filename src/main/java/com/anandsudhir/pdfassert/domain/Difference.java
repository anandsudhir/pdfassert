package com.anandsudhir.pdfassert.domain;

import java.util.ArrayList;
import java.util.List;

import com.snowtide.pdf.layout.Region;

public class Difference {
    private List<Region> diffsInExpected = new ArrayList<Region>();
    private List<Region> diffsInActual = new ArrayList<Region>();

    public List<Region> getDiffsInExpected() {
        return diffsInExpected;
    }

    public void addDiffsInExpected(List<Region> diffs) {
        this.diffsInExpected.addAll(diffs);
    }

    public List<Region> getDiffsInActual() {
        return diffsInActual;
    }

    public void addDiffsInActual(List<Region> diffs) {
        this.diffsInActual.addAll(diffs);
    }

    public void merge(Difference diff) {
        if (diff != null) {
            this.diffsInActual.addAll(diff.getDiffsInActual());
            this.diffsInExpected.addAll(diff.getDiffsInExpected());
        }
    }

    public boolean isEmpty() {
        return diffsInExpected.isEmpty() && diffsInActual.isEmpty();
    }

    @Override
    public String toString() {
        return "Difference{" +
                "diffsInExpected=" + diffsInExpected +
                ", diffsInActual=" + diffsInActual +
                '}';
    }
}