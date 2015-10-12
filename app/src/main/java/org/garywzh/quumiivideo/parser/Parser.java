package org.garywzh.quumiivideo.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Parser {
    public static Document toDoc(String html) {
        final Document document = Jsoup.parse(html);
        final Document.OutputSettings settings = document.outputSettings().prettyPrint(false);
        document.outputSettings(settings);
        return document;
    }

    public static Document xmlToDoc(String xml) {
        xml = xml.replace("<root><![CDATA[", "");
        xml = xml.replace("]]></root>", "");
        final Document document = Jsoup.parse(xml);
        final Document.OutputSettings settings = document.outputSettings().prettyPrint(false);
        document.outputSettings(settings);
        return document;
    }
}
