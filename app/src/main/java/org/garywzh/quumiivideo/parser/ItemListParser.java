package org.garywzh.quumiivideo.parser;

import org.garywzh.quumiivideo.model.Item;
import org.garywzh.quumiivideo.util.LogUtils;

import com.google.common.collect.Lists;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class ItemListParser extends Parser {

    public static List<Item> parseDocForItemList(Document doc) {
        final Elements elements = doc.select(".item");
        LogUtils.d("elements count", String.valueOf(elements.size()));
        final List<Item> result = Lists.newArrayListWithCapacity(elements.size());
        for (Element item : elements) {
            result.add(parseItem(item));
        }
        return result;
    }

    private static Item parseItem(Element item) {
        final Elements list = item.children();

        final Item.Builder itemBuilder = new Item.Builder();

        parseId(itemBuilder, item);

        parseThumbConut(itemBuilder, list.get(1));

        final Elements detailList = list.get(2).children();

        parseCoverPic(itemBuilder, detailList.get(0));

        parseTimeLength(itemBuilder, detailList.get(2));
        parseTitle(itemBuilder, detailList.get(3));
        parseTime(itemBuilder, detailList.get(5));

        return itemBuilder.createItem();
    }

    private static void parseId(Item.Builder itemBuilder, Element ele){
        final int id = Integer.parseInt(ele.attr("id"));
        itemBuilder.setId(id);
    }

    private static void parseThumbConut(Item.Builder itemBuilder, Element ele){
        itemBuilder.setThumbUpCount(0);
        itemBuilder.setThumbDownCount(0);
    }

    private static void parseCoverPic(Item.Builder itemBuilder, Element ele){
        ele = ele.select(".videoimg").get(0);

        itemBuilder.setCoverPic(ele.attr("src"));
    }

    private static void parseTimeLength(Item.Builder itemBuilder, Element ele){
        itemBuilder.setTimeLength(ele.child(0).text());
    }

    private static void parseTitle(Item.Builder itemBuilder, Element ele){
        itemBuilder.setTitle(ele.child(0).child(0).text());
    }

    private static void parseTime(Item.Builder itemBuilder, Element ele){
        itemBuilder.setTime(ele.select(".time").get(0).text());
    }
}
