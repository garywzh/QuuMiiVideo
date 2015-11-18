package org.garywzh.quumiibox.parser;

import com.google.common.collect.Lists;

import org.garywzh.quumiibox.model.Item;
import org.garywzh.quumiibox.model.Tag;
import org.garywzh.quumiibox.util.LogUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class ItemListParser extends Parser {

    public static List<Item> parseDocForItemList(Document doc, boolean isByTag, boolean isByFav) {
        final Elements elements = doc.select(".link.show.linkhover");
//        非Tag列表去掉第一个没有用的元素
        if (!isByTag) {
            elements.remove(0);
        }
        LogUtils.d("elements count", String.valueOf(elements.size()));
        final List<Item> result = Lists.newArrayListWithCapacity(elements.size());
        for (Element item : elements) {
            result.add(parseItem(item, isByFav));
        }
        return result;
    }

    private static Item parseItem(Element item, boolean isByFav) {
        final Item.Builder itemBuilder = new Item.Builder();

        parseId(itemBuilder, item);

        parseThumbConut(itemBuilder, item);

        final Element entry = item.child(3);

        final Element picNext = entry.select(".picnext").get(0);

        parseTitle(itemBuilder, picNext.child(0));
        parseType(itemBuilder, picNext.child(0));

        final Element tagsAndTime = picNext.select(".tagline.dateline").get(0);

        parseTime(itemBuilder, tagsAndTime);
        parseTags(itemBuilder, tagsAndTime, isByFav);

        parseReplyCount(itemBuilder, entry.select(".tagline.rightalign").get(0));

        return itemBuilder.createItem();
    }

    private static void parseId(Item.Builder itemBuilder, Element ele) {
        final int id = Integer.parseInt(ele.attr("id").replace("link_", ""));
        itemBuilder.setId(id);
    }

    private static void parseThumbConut(Item.Builder itemBuilder, Element ele) {
        ele = ele.child(1).child(1);
        final int count = Integer.parseInt(ele.text().replace(" ", ""));
        itemBuilder.setThumbUpCount(count);
    }

    private static void parseTitle(Item.Builder itemBuilder, Element ele) {
        itemBuilder.setTitle(ele.child(0).text());
    }

    private static void parseType(Item.Builder itemBuilder, Element ele) {
        if (ele.children().size() == 2) {
            itemBuilder.setType(Item.Type.IMAGE);
            itemBuilder.serInfoBasedType(ele.child(1).attr("href"));
        } else {
            final String titleHref = ele.child(0).attr("href");

            if (titleHref.contains("itemlist")) {
                itemBuilder.setType(Item.Type.TOPIC);
                itemBuilder.serInfoBasedType(titleHref);
            } else if (titleHref.contains("videolist")) {
                itemBuilder.setType(Item.Type.VIDEO);
                String blogId = titleHref
                        .replace("http://www.quumii.com/videolist-id-", "")
                        .replace(".html", "");
                itemBuilder.serInfoBasedType(blogId);
            } else {
                itemBuilder.setType(Item.Type.NEWS);
                itemBuilder.serInfoBasedType(titleHref);
            }
        }
    }

    private static void parseTime(Item.Builder itemBuilder, Element ele) {
        final Elements elements = ele.select("span");
        String time = elements.get(0).text();
        if (time.length() <= 1) {
            time = elements.get(1).text();
        }
        itemBuilder.setTime(time);
    }

    private static void parseTags(Item.Builder itemBuilder, Element ele, boolean isByFav) {
        final Elements elements = ele.select(".item");
        /*最后一个不能转成Tag对象*/
        if (isByFav) {
            elements.remove(elements.last());
        }
        final List<Tag> result = Lists.newArrayListWithCapacity(elements.size());
        for (Element tag : elements) {
            result.add(parseTag(tag));
        }

        itemBuilder.setTags(result);
    }

    private static Tag parseTag(Element ele) {
        final Tag.Builder builder = new Tag.Builder();
        builder.setId(Integer.parseInt(ele.attr("href").replace("itemlist-do-tag-id-", "").replace(".html", "")));
        builder.setName(ele.select(".txt").get(0).text());

        return builder.createTag();
    }

    private static void parseReplyCount(Item.Builder itemBuilder, Element ele) {
        itemBuilder.setReplyCount(Integer.parseInt(ele.select(".comm").get(0).text().replace("\u00A0", "").trim()));
    }
}