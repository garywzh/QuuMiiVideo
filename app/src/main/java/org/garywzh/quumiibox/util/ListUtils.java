package org.garywzh.quumiibox.util;

import java.util.List;

/**
 * Created by WZH on 2015/10/31.
 */
public class ListUtils {

    public static <T> void mergeListWithoutDuplicates(List<T> toList, List<T> fromList) {
        if (toList.size() != 0) {
            final T lastItemOfToList = toList.get(toList.size() - 1);
            for (int i = 0; i < fromList.size(); i++) {
                if (fromList.get(i).equals(lastItemOfToList)) {
                    final int lastIndex = toList.size() - 1;
                    for (int j = 0; j <= i; j++)
                        toList.remove(lastIndex - j);
                    break;
                }
            }
        }
        toList.addAll(fromList);
    }
}
