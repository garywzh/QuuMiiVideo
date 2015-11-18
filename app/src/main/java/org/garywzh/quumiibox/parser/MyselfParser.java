package org.garywzh.quumiibox.parser;

import org.jsoup.nodes.Document;

/**
 * Created by WZH on 2015/11/10.
 */
public class MyselfParser extends Parser {

    public static int parseId(Document doc) {
        return Integer.parseInt(doc.select(".circle").get(0).child(0).attr("href").replace("/space.php?uid=", ""));
    }

    public static int parseCredit(Document doc) {
        return Integer.parseInt(doc.select(".credit").get(0).child(0).text().replace("我的积分: ", ""));
    }
}
