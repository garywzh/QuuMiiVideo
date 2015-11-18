package org.garywzh.quumiibox.parser;

import org.garywzh.quumiibox.model.Member;
import org.jsoup.nodes.Document;

/**
 * Created by WZH on 2015/11/10.
 */
public class MemberParser {
    public static Member.Builder parseDocForMemberNameAndAvatar(Document doc) {
        final Member.Builder builder = new Member.Builder();
        builder.setName(doc.select(".hey").get(0).text().replace(", wut's up?","").trim());
        builder.setAvatar(doc.select(".bigav").get(0).child(0).attr("src"));
        return builder;
    }
}
