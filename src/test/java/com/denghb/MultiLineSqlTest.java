package com.denghb;

import com.denghb.xxlibrary.domain.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MultiLineSqlTest {

    public static void main(String[] args) {

        Student xm = new Student();
        xm.setId(11);
        xm.setName("小明");
        xm.setGender(1);

        Student xh = new Student();
        xh.setId(12);
        xh.setName("小红");
        xh.setGender(2);

        Student xt = new Student();
        xt.setId(11);
        xt.setName("小天");
        xt.setGender(1);

        List<Student> list = new ArrayList<>();
        list.add(xm);
        list.add(xh);
        list.add(xt);

        Map<Integer, List<Student>> genderListMap = list.stream().collect(Collectors.groupingBy(Student::getId));
        Map<Integer, List<String>> genderListNameMap = list.stream().collect(Collectors.groupingBy(Student::getId,
                Collectors.mapping(Student::getName,Collectors.toList())));

        Map<Integer, Student> idMap = list.stream().collect(Collectors.toMap(Student::getId, Function.identity()));
    }

    private static void doRun(int start, String source, String key) {
        int i = source.indexOf(key);
        if (-1 < i) {

            String next = source.substring(i + key.length());
            System.out.println(next);
            doRun(start, next, key);
        }

    }
}
