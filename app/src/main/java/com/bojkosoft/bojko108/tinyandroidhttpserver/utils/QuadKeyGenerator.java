package com.bojkosoft.bojko108.tinyandroidhttpserver.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class QuadKeyGenerator {

    public QuadKeyGenerator() {

    }

    public String TileXYToQuadKey(int z, int x, int y) {
        List<Integer> quadKey = new ArrayList<>();

        for (int i = z; i > 0; i--) {
            int digit = 0;
            int mask = 1 << (i - 1);
            if ((x & mask) != 0) {
                digit++;
            }
            if ((y & mask) != 0) {
                digit++;
                digit++;
            }
            quadKey.add(digit);
        }

        return TextUtils.join("", quadKey);
    }
}
