package in.okcredit.merchant.collection.utils;

import com.google.common.base.Converter;

import java.util.ArrayList;
import java.util.List;

public final class CommonUtils {
    public static <A, B> List<B> mapList(List<A> aList, Converter<A, B> mapper) {
        List<B> bList = new ArrayList<>(aList.size());
        for (A a : aList) {
            bList.add(mapper.convert(a));
        }
        return bList;
    }
}
