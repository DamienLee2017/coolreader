package com.dotcool.util;

import java.util.List;

public class ArrayUtil
{
    /**
     * 将List转换成相应的String数组
     * @param list 指定的list
     * @return 返回结果
     */
    public static String[] getStringArray(List<String> list)
    {
        String[] str = new String[list.size()];
        for(int i=0;i<list.size();i++)
        {
            str[i] = list.get(i);
        }
        return str;
    }
}
