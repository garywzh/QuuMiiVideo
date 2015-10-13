package org.garywzh.quumiivideo.parser;

/**
 * Created by garywzh on 2015/10/9.
 */

import org.jsoup.nodes.Document;

public class VideoUrlParser extends Parser {

    public static String parseDocForVideoUrl(Document doc) {
        return doc.select("iframe").get(0).attr("src");
    }
}