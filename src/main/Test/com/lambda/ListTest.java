package com.lambda;

import org.junit.Test;

import java.awt.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ListTest {
    /**
     * 遍历List
     */
    public static List<Point> pointList = Arrays.asList(new Point(1,2),new Point(2,3));



    /**
     * 引入JDK5之前
     */
    @Test
    public void test01(){
        Iterator<Point> iterator = pointList.iterator();
        iterator.hasNext();
    }

}
