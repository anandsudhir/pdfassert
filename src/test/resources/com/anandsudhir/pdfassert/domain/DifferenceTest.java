package com.anandsudhir.pdfassert.domain;

import com.snowtide.pdf.layout.RectImpl;
import com.snowtide.pdf.layout.Region;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DifferenceTest {

    @Test
    public void testMerge() throws Exception {
        Difference initialDifference = new Difference();
        Difference newDifference = new Difference();

        List<Region> newDiffRegions = new ArrayList<Region>();
        newDiffRegions.add(new RectImpl());
        newDiffRegions.add(new RectImpl());
        newDifference.addDiffsInActual(newDiffRegions);
        newDifference.addDiffsInExpected(newDiffRegions);

        initialDifference.merge(newDifference);

        assertEquals(2, initialDifference.getDiffsInActual().size());
        assertEquals(2, initialDifference.getDiffsInExpected().size());
    }

    @Test
    public void testMergeWithNoDifferencesDoesNothing() throws Exception {
        Difference initialDifference = new Difference();
        Difference newDifference = new Difference();

        initialDifference.merge(newDifference);

        assertEquals(0, initialDifference.getDiffsInActual().size());
        assertEquals(0, initialDifference.getDiffsInExpected().size());
    }
}
