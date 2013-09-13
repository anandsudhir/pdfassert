package com.anandsudhir.pdfassert.diff;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.anandsudhir.pdfassert.domain.Difference;
import com.snowtide.pdf.layout.Line;
import com.snowtide.pdf.layout.Region;
import com.snowtide.pdf.layout.TextUnitImpl;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

public class TextDiff {

    private diff_match_patch dmp = new diff_match_patch();
    private List<String> ignorePatterns = new ArrayList<String>();

    public void setIgnorePatterns(List<String> ignorePatterns) {
        this.ignorePatterns = ignorePatterns;
    }

    /**
     * Gets the {@code Region}s where differences were found in the text
     * in expected {@code Line} and actual {@code Line}
     */
    public Difference getDifferences(Line expected, Line actual) {
        String expectedString = getLineAsString(expected);
        String actualString = getLineAsString(actual);

        for (String ignorePattern : ignorePatterns) {
            if (expectedString.matches(ignorePattern)) {
                return null;
            }
        }

        List<Diff> diffs = getDifferences(expectedString, actualString);
        if (diffs.size() > 1) {
            return getRegionsFromDiffs(diffs, expected, actual);
        }

        return null;
    }

    /**
     * Gets the Levenshtein distance between text in expected {@code Line} and actual {@code Line}
     */
    public int getLevenshteinDistance(Line expected, Line actual) {
        String expectedString = getLineAsString(expected);
        String actualString = getLineAsString(actual);

        //CHECKSTYLE:OFF
        LinkedList<Diff> diffs = getDifferences(expectedString, actualString);
        //CHECKSTYLE:ON

        return dmp.diff_levenshtein(diffs);
    }

    /**
     * Checks if the text in expected {@code Line} and actual {@code Line} are similar. <br/>
     * This is done by cheching if the Levenshtein distance between the texts is within an acceptable range. <br/>
     * Currently unused
     */
    // TODO Think of a more "scientific" way to figure out if 2 lines are similar
    public boolean isSimilar(Line expected, Line actual) {
        String expectedString = getLineAsString(expected);
        String actualString = getLineAsString(actual);

        //CHECKSTYLE:OFF
        LinkedList<Diff> diffs = getDifferences(expectedString, actualString);
        //CHECKSTYLE:ON

        int levenshteinDistance = dmp.diff_levenshtein(diffs);
        return levenshteinDistance < (Math.min(expectedString.length(), actualString.length()));
    }

    //CHECKSTYLE:OFF
    private LinkedList<Diff> getDifferences(String expected, String actual) {
        return dmp.diff_main(expected, actual);
    }
    //CHECKSTYLE:ON

    /**
     * Gets the {@code Region}s where differences were found
     */
    @SuppressWarnings("unchecked")
    private Difference getRegionsFromDiffs(List<Diff> diffs, Line expected, Line actual) {
        Difference difference = new Difference();
        int p1 = 0;
        int p2 = 0;
        List<Region> expectedTextUnits = expected.getTextUnits();
        List<Region> actualTextUnits = actual.getTextUnits();

        for (Diff diff : diffs) {
            switch (diff.operation) {
                case EQUAL:
                    p1 += diff.text.length();
                    p2 += diff.text.length();
                    break;
                case INSERT:
                    difference.addDiffsInActual(actualTextUnits.subList(p2, p2 + diff.text.length()));
                    p2 += diff.text.length();
                    break;
                case DELETE:
                    difference.addDiffsInExpected(expectedTextUnits.subList(p1, p1 + diff.text.length()));
                    p1 += diff.text.length();
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown diff operation");
            }
        }

        return difference;
    }

    private String getLineAsString(Line line) {
        if (line == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (int j = line.getTextUnitCnt(); i < j; i++) {
            char[] arrayOfChar = line.getTextUnit(i).getCharacterSequence();

            if (arrayOfChar == null) {
                sb.append((char) ((TextUnitImpl) line.getTextUnit(i)).charCode);
            } else {
                sb.append(arrayOfChar);
            }
        }

        return sb.toString();
    }

}
