package com.togglecorp.paiso;

public class Utils {
    public static int getLength(Iterable iterable) {
        int i = 0;
        for (Object ignore : iterable) {
            i++;
        }
        return i;
    }
}
