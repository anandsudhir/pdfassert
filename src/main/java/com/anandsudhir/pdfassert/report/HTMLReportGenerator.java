package com.anandsudhir.pdfassert.report;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.anandsudhir.pdfassert.domain.Difference;
import com.snowtide.pdf.layout.Line;
import com.snowtide.pdf.layout.Region;
import com.snowtide.pdf.layout.TextUnit;
import com.snowtide.pdf.layout.TextUnitImpl;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HTMLReportGenerator implements ReportGenerator {

    public static final String NEWLINE = "\n";
    public static final String TH = "th";
    private static Logger logger = Logger.getLogger(HTMLReportGenerator.class.getName());
    private final Map<String, String> attrs = new HashMap<String, String>();
    private QuietMode quietMode;
    private Document htmlDocument;
    private Element htmlElement;
    private Element htmlBodyElement;
    private Element report;

    public HTMLReportGenerator() {
        init();
    }

    @Override
    public void setQuietMode(QuietMode quietMode) {
        this.quietMode = quietMode;
    }

    public String getReport() {
        StringWriter writer = new StringWriter();
        try {
            serializeXMLDocument(htmlDocument, writer);
        } catch (TransformerException e) {
            logger.error("Something horrible happened!", e);
        }

        return writer.toString();
    }

    protected void init() {
        try {
            quietMode = QuietMode.OFF;
            htmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            htmlElement = htmlDocument.createElement("html");
            htmlDocument.appendChild(htmlElement);
            buildHead();
            htmlBodyElement = htmlDocument.createElement("body");
            htmlElement.appendChild(htmlBodyElement);
        } catch (ParserConfigurationException e) {
            logger.error("Something horrible happened!", e);
        }
    }

    private void buildHead() {
        Element head = htmlDocument.createElement("head");
        Element style = htmlDocument.createElement("style");
        style.setAttribute("type", "text/css");
        style.setTextContent(NEWLINE +
                "        body {\n" +
                "            margin: 0;\n" +
                "            font-family: Arial,Helvetica,sans-serif;\n" +
                "            font-size: 76%;\n" +
                "        }\n" +
                NEWLINE +
                "        table {\n" +
                "            border-top: 1px solid #9EADC0;\n" +
                "            border-left: 1px solid #9EADC0;\n" +
                "            border-collapse: collapse;\n" +
                "            margin-left: 20px;\n" +
                "        }\n" +
                NEWLINE +
                "        table caption {\n" +
                "            color: #0E4993;\n" +
                "            font-size: 16px;\n" +
                "            font-weight: bold;\n" +
                "            padding-top: 8px;\n" +
                "            padding-bottom: 8px;\n" +
                "            text-align: left;\n" +
                "        }\n" +
                NEWLINE +
                "        thead tr {\n" +
                "            background: none repeat scroll 0 0 #DEE3E9;\n" +
                "        }\n" +
                NEWLINE +
                "        thead tr th {\n" +
                "            text-align: left;\n" +
                "            padding: 4px;\n" +
                "            vertical-align: top;\n" +
                "            border-top: 1px solid #9EADC0;\n" +
                "            border-bottom: 1px solid #9EADC0;\n" +
                "            border-right: 1px solid #9EADC0;\n" +
                "        }\n" +
                NEWLINE +
                "        tbody tr:nth-child(odd) {\n" +
                "            background-color: #EEEEEF;\n" +
                "        }\n" +
                NEWLINE +
                "        tbody tr:nth-child(even) {\n" +
                "            background-color: #FFFFFF;\n" +
                "        }\n" +
                NEWLINE +
                "        tbody tr td {\n" +
                "            border-bottom: 1px solid #9EADC0;\n" +
                "            border-right: 1px solid #9EADC0;\n" +
                "            padding: 4px;\n" +
                "            vertical-align: top;\n" +
                "        }");
        head.appendChild(style);
        htmlElement.appendChild(head);
    }

    @Override
    public void createNewReport(String expectedPDF, String actualPDF) {
        Element table = htmlDocument.createElement("table");
        htmlBodyElement.appendChild(table);
        Element caption = htmlDocument.createElement("caption");
        caption.appendChild(htmlDocument.createTextNode(expectedPDF + " Vs. " + actualPDF));
        table.appendChild(caption);

        Element thead = htmlDocument.createElement("thead");
        table.appendChild(thead);
        Element tr = htmlDocument.createElement("tr");
        thead.appendChild(tr);
        Element th1 = htmlDocument.createElement(TH);
        th1.appendChild(htmlDocument.createTextNode("Content in " + expectedPDF));
        tr.appendChild(th1);
        Element th2 = htmlDocument.createElement(TH);
        th2.appendChild(htmlDocument.createTextNode("Content in " + actualPDF));
        tr.appendChild(th2);
        Element th3 = htmlDocument.createElement(TH);
        th3.appendChild(htmlDocument.createTextNode("Difference in " + expectedPDF));
        tr.appendChild(th3);
        Element th4 = htmlDocument.createElement(TH);
        th4.appendChild(htmlDocument.createTextNode("Difference in " + actualPDF));
        tr.appendChild(th4);

        report = htmlDocument.createElement("tbody");
        table.appendChild(report);
    }

    @Override
    public void reportDifference(Region expected, Region actual, Difference difference) {
        if (quietMode == QuietMode.ON) {
            return;
        }

        if (expected instanceof Line && actual instanceof Line) {
            reportLineDifference(expected, actual, difference);
        }
    }

    protected void reportLineDifference(Region expected, Region actual, Difference difference) {
        report.appendChild(buildDiffRow(expected, actual, difference));
        /*//System.out.println("expected: " + expected);
        //System.out.println("actual: " + actual);
        for (Region r : difference.getDiffsInExpected()) {
            textUnit((TextUnit) r);
            //System.out.print(((TextUnit) r).getCharacterSequence());
        }
        //System.out.println("\n");

        for (Region r : difference.getDiffsInActual()) {
            textUnit((TextUnit) r);
            //System.out.print(((TextUnit) r).getCharacterSequence());
        }
        //System.out.println("\n");*/
    }

    private void textUnit(TextUnit tu) {
        attrs.clear();
        attrs.put("style", "position:absolute;top:" + (tu.ypos()) +
                ";left:" + tu.xpos() + ";font-size:" + ((TextUnitImpl) tu).lineHeight());

        String txt = tu.getCharacterSequence() == null ?
                Character.toString((char) tu.getCharCode()) : new String(tu.getCharacterSequence());

        report.appendChild(buildTextElt(htmlDocument, "span", txt, attrs));
    }

    private Element buildTextElt(Document doc, String elttype, String contents, Map attributes) {
        Element te = doc.createElement(elttype);
        if (contents != null && contents.length() > 0) {
            te.appendChild(doc.createTextNode(contents));
        }

        if (attributes != null) {
            Map.Entry attr;
            for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                attr = (Map.Entry) iter.next();
                te.setAttribute(String.valueOf(attr.getKey()), String.valueOf(attr.getValue()));
            }
        }

        return te;
    }

    private Element buildDiffRow(Region expected, Region actual, Difference difference) {
        Element tr = htmlDocument.createElement("tr");
        String e = getTextUnitsAsString(((Line) expected).getTextUnits());
        tr.appendChild(buildDiffColumn(e));
        String a = getTextUnitsAsString(((Line) actual).getTextUnits());
        tr.appendChild(buildDiffColumn(a));
        String de = getTextUnitsAsString(difference.getDiffsInExpected());
        tr.appendChild(buildDiffColumn(de));
        String da = getTextUnitsAsString(difference.getDiffsInActual());
        tr.appendChild(buildDiffColumn(da));
        return tr;
    }

    private Element buildDiffColumn(String text) {
        Element td = htmlDocument.createElement("td");
        td.appendChild(htmlDocument.createTextNode(text));
        return td;
    }

    private String getTextUnitsAsString(List<?> textUnits) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (int j = textUnits.size(); i < j; i++) {
            char[] arrayOfChar = ((TextUnit) textUnits.get(i)).getCharacterSequence();

            if (arrayOfChar == null) {
                sb.append((char) ((TextUnitImpl) textUnits.get(i)).charCode);
            } else {
                sb.append(arrayOfChar);
            }
        }

        return sb.toString();
    }

    // serialize XML document using identity transformation
    private void serializeXMLDocument(Document doc, Writer output) throws TransformerException {
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        trans.transform(source, result);
    }
}
