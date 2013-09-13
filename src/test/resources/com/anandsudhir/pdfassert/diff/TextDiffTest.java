package com.anandsudhir.pdfassert.diff;

import com.anandsudhir.pdfassert.domain.Difference;
import com.snowtide.pdf.AbstractPage;
import com.snowtide.pdf.CharacterSeq;
import com.snowtide.pdf.FontMapping;
import com.snowtide.pdf.Page;
import com.snowtide.pdf.layout.Line;
import com.snowtide.pdf.layout.Region;
import com.snowtide.pdf.layout.TextUnit;
import com.snowtide.pdf.layout.TextUnitImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TextDiffTest {

    private static final char[] EXPECTED_CHARS = "abcdefg".toCharArray();
    private static final char[] ACTUAL_CHARS = "acdbefg".toCharArray();

    private TextDiff textDiff;
    private Line expectedLine;
    private Line actualLine;

    // TODO: Test handling of nulls
    @Before
    public void setup() {
        textDiff = new TextDiff();

        expectedLine = mock(Line.class);
        TextUnit expectedTextUnit = mock(TextUnit.class);
        List<Region> expectedRegions = new ArrayList<Region>();
        for (char expectedChar : EXPECTED_CHARS) {
            expectedRegions.add(new TextUnitImpl(
                    mock(AbstractPage.class),
                    0,
                    new CharacterSeq(new char[]{expectedChar}),
                    mock(FontMapping.class),
                    0,
                    0,
                    mock(TextUnitImpl.Baseline.class)));
        }
        when(expectedLine.getTextUnits()).thenReturn(expectedRegions);
        when(expectedLine.getTextUnitCnt()).thenReturn(1);
        when(expectedLine.getTextUnit(anyInt())).thenReturn(expectedTextUnit);
        when(expectedTextUnit.getCharacterSequence()).thenReturn(EXPECTED_CHARS);

        actualLine = mock(Line.class);
        TextUnit actualTextUnit = mock(TextUnit.class);
        List<Region> actualRegions = new ArrayList<Region>();
        for (char actualChar : ACTUAL_CHARS) {
            actualRegions.add(new TextUnitImpl(
                    mock(AbstractPage.class),
                    0,
                    new CharacterSeq(new char[] {actualChar}),
                    mock(FontMapping.class),
                    0,
                    0,
                    mock(TextUnitImpl.Baseline.class)));
        }
        when(actualLine.getTextUnits()).thenReturn(actualRegions);
        when(actualLine.getTextUnitCnt()).thenReturn(1);
        when(actualLine.getTextUnit(anyInt())).thenReturn(actualTextUnit);
        when(actualTextUnit.getCharacterSequence()).thenReturn(ACTUAL_CHARS);
    }

    @Test
    public void testGetDifferences() throws Exception {
        Difference difference = textDiff.getDifferences(expectedLine, actualLine);
        assertEquals(1, difference.getDiffsInExpected().size());
        assertEquals(1, difference.getDiffsInActual().size());
    }

}
