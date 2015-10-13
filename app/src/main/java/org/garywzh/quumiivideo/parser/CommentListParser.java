package org.garywzh.quumiivideo.parser;

import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.garywzh.quumiivideo.util.LogUtils;

import java.util.List;

import org.garywzh.quumiivideo.model.Comment;
import org.garywzh.quumiivideo.model.Member;

/**
 * Created by garywzh on 2015/10/10.
 */
public class CommentListParser extends Parser {

    public static List<Comment> parseDocForCommentList(Document doc) {
        final Elements elements = doc.select(".thing.even.comment");
        LogUtils.d("elements count", String.valueOf(elements.size()));
        final List<Comment> result = Lists.newArrayListWithCapacity(elements.size());
        for (Element comment : elements) {
            result.add(parseComment(comment));
        }
        return result;
    }

    private static Comment parseComment(Element comment) {
        final Comment.Builder commentBuilder = new Comment.Builder();

        parseId(commentBuilder, comment);

        parseMember(commentBuilder, comment);

        parseContent(commentBuilder, comment);

        parseTime(commentBuilder, comment);

        parseThumbConut(commentBuilder, comment);

        return commentBuilder.createComment();
    }

    private static void parseId(Comment.Builder commentBuilder, Element ele){
        commentBuilder.setId(Integer.parseInt(ele.attr("id").replace("commentItem_", "")));
    }

    private static void parseMember(Comment.Builder commentBuilder, Element ele){
        final Member.Builder memberBuilder = new Member.Builder();
        memberBuilder.setAvatar(ele.select("img").get(0).attr("src"));
        ele = ele.select(".author").get(0);
        memberBuilder.setId(Integer.parseInt(ele.attr("href").replace("space-", "").replace(".html", "")));
        memberBuilder.setName(ele.text());
        commentBuilder.setMember(memberBuilder.createMember());
    }

    private static void parseContent(Comment.Builder commentBuilder, Element ele){
        commentBuilder.setContent(ele.select(".message").text());
    }

    private static void parseTime(Comment.Builder commentBuilder, Element ele){
        commentBuilder.setTime(ele.select("time").text());
    }

    private static void parseThumbConut(Comment.Builder commentBuilder, Element ele){
        Elements eles = ele.select(".smallnum");
        commentBuilder.setThumbUpCount(Integer.parseInt(eles.get(0).text()));
        commentBuilder.setThumbDownCount(Integer.parseInt(eles.get(1).text()));
    }
}

