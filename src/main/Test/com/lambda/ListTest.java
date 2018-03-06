package com.lambda;

import org.junit.Test;

import java.awt.*;
import java.util.*;
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
        for(Iterator<Point> pointItr = pointList.iterator(); pointItr.hasNext();)
            pointItr.next().translate(1, 1);
    }

    @Test
    public void test02(){
        Iterator<Point> pointItr = pointList.iterator();
        while (pointItr.hasNext()){
            pointItr.next().translate(1, 1);
        }
    }


    @Test
    public void setTest(){
        Set  set=new HashSet();
         set.add("aaa");
    }
}
