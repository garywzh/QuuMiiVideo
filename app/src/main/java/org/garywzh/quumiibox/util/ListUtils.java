package org.garywzh.quumiibox.util;

import java.util.List;

/**
 * Created by WZH on 2015/10/31.
 */
public class ListUtils {

    public static <T> void mergeListWithoutDuplicates(List<T> toList, List<T> fromList) {
        final T firtItemOfFromList = fromList.get(0);
        for (int i = toList.size() - 1; i >= 0; i--) {
            if (toList.get(i).equals(firtItemOfFromList)) {
                for (int j = toList.size() - 1; j >= i; j--)
                    toList.remove(j);
                break;
            }
        }
        toList.addAll(fromList);
    }
}
