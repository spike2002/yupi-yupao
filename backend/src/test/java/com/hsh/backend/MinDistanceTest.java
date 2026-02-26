package com.hsh.backend;

import com.hsh.backend.utils.MinDistance;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class MinDistanceTest {
    @Test
    void test() {
        String str1 = "abc";
        String str2 = "abcde";
        String str3 = "abcdefg";
        //2
        System.out.println(MinDistance.getMinDistance(str1, str2));
        //4
        System.out.println(MinDistance.getMinDistance(str1, str3));
    }

    @Test
    void test1() {
        List<String> list1 = Arrays.asList("a");
        List<String> list2 = Arrays.asList("a", "b");
        List<String> list3 = Arrays.asList("a", "b", "c");
        //1
        System.out.println(MinDistance.getMinDistance(list1, list2));
        //2
        System.out.println(MinDistance.getMinDistance(list1, list3));
    }
}
