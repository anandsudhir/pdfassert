package com.anandsudhir.pdfassert.diff;

import com.anandsudhir.pdfassert.domain.Difference;
import com.snowtide.pdf.layout.Line;
import com.snowtide.pdf.layout.Region;
import com.snowtide.pdf.layout.TextUnitImpl;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TextDiff {

    private diff_match_patch dmp = new diff_match_patch();
    private List<String> ignorePatterns = new ArrayList<String>();

    public void setIgnorePatterns(List<String> ignorePatterns) {
        this.ignorePatterns = ignorePatterns;
    }

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

    public int getLevenshteinDistance(Line expected, Line actual) {
        String expectedString = getLineAsString(expected);
        String actualString = getLineAsString(actual);

        LinkedList<Diff> diffs = getDifferences(expectedString, actualString);

        return dmp.diff_levenshtein(diffs);
    }

    // TODO Think of a more "scientific" way to figure out if 2 lines are similar
    @SuppressWarnings("unused")
    private boolean isSimilar(Line expected, Line actual) {
        String expectedString = getLineAsString(expected);
        String actualString = getLineAsString(actual);

        LinkedList<Diff> diffs = getDifferences(expectedString, actualString);

        int levenshteinDistance = dmp.diff_levenshtein(diffs);
        return levenshteinDistance < (Math.min(expectedString.length(), actualString.length()));
    }

    private LinkedList<Diff> getDifferences(String expected, String actual) {
        return dmp.diff_main(expected, actual);
    }

    @SuppressWarnings("unchecked")
    private Difference getRegionsFromDiffs(List<Diff> diffs, Line expected, Line actual) {
        Difference difference = new Difference();
        int p1 = 0, p2 = 0;
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
