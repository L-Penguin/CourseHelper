package com.tiancai.app.lib;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class CourseParser {

    public static final String NO_DESCRIPTION = "no description";
    public static final String NO_PREREQUISITE = "no prerequisite";
    public static final String NEED_ALEKS = "need ALEKS";
    public static final String COURSE_NOT_FOUND = "course not found";

    public static Course courseFactory(String year, String semester, String cat_num) {
        try {
            String cat = cat_num.trim().toUpperCase().substring(0,cat_num.length() - 3);
            String num = cat_num.trim().toUpperCase().substring(cat_num.length() - 3, cat_num.length());
            String urlStr = "http:/courses.illinois.edu/cisapp/explorer/schedule/"
                    + year.toUpperCase().trim() + "/" + semester.toUpperCase().trim() + "/"
                    + cat.toUpperCase().trim() + "/" + num.toUpperCase().trim() + ".xml";
            String prerequisite = getPrerequisite(urlStr);
            Course course = new Course(cat.trim().toUpperCase(), num.trim().toUpperCase());
            getPrerequisiteForAll(year.toUpperCase().trim(), semester.toUpperCase().trim(), course);
            return course;
        } catch (Exception e) {
            e.printStackTrace();
            return new Course(CourseParser.COURSE_NOT_FOUND, "0");
        }
    }

    private static void getPrerequisiteForAll(String year, String semester, Course current) {
        try {
            if (current.getCourseName().equals(CourseParser.COURSE_NOT_FOUND)) {
                return;
            }
            if (current.getCourseName().equals(CourseParser.NEED_ALEKS)) {
                return;
            }
            String cat = current.getCourseName();
            String num = current.getCourseNum();
            String urlStr = "http:/courses.illinois.edu/cisapp/explorer/schedule/"
                    + year.toUpperCase().trim() + "/" + semester.toUpperCase().trim() + "/"
                    + cat.toUpperCase().trim() + "/" + num.toUpperCase().trim() + ".xml";
            current.setPrerequisites(parsePrerequisiteFactory(prerequisiteSeperator(getPrerequisite(urlStr))));
            for (List<Course> each : current.getPrerequisites()) {
                for (Course each_c : each) {
                    getPrerequisiteForAll(year, semester, each_c);
                }
            }
        } catch (Exception e) {
            List<Course> toAdd = new ArrayList<>();
            toAdd.add(new Course(CourseParser.NO_PREREQUISITE, "0"));
            current.appendPrerequisite(toAdd);
            return;
        }
    }

    private static String getPrerequisite(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.connect();

        try {
            Document doc = parseXML(connection.getInputStream());
            System.out.println(doc);
            NodeList descNode = doc.getElementsByTagName("description");
            return parsePrerequisite(descNode);
        } catch (FileNotFoundException e) {
            return CourseParser.COURSE_NOT_FOUND;
        } catch (Exception e) {
            e.printStackTrace();
            return CourseParser.NO_PREREQUISITE;
        }
    }

    private static Document parseXML(InputStream stream) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = null;
        DocumentBuilder documentBuilder = null;
        Document document = null;
        try {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();

            document = documentBuilder.parse(stream);
        } catch (Exception e) {
            throw e;
        }

        return document;
    }

    private static String parsePrerequisite(NodeList docNodeList) throws Exception {
        if (docNodeList.getLength() < 1) {
            return CourseParser.NO_DESCRIPTION;
        } else {
            String description = docNodeList.item(0).getTextContent();
            if (!description.contains("Prerequisite: ")) {
                return CourseParser.NO_PREREQUISITE;
            } else {
                return description.split("Prerequisite: ")[1];
            }
        }
    }

    private static List<String> prerequisiteSeperator(String toSeparate) {
        List<String> list = new ArrayList<>();

        if (toSeparate.contains("ALEKS")) { //some math classes happened to require ALEKS score
            list.add(CourseParser.NEED_ALEKS);
            return list;
        }

        if (toSeparate.contains(";")) {
            toSeparate = toSeparate.replace(";", "and");
        }

        if (toSeparate.contains(",")) {
            toSeparate = toSeparate.replace(",", "and");
        }

        list = Arrays.asList(toSeparate.split("and"));

        return list;
    }

    private static List<Course> parseStringForCourse(String str) {

        if (str.equals(CourseParser.NEED_ALEKS)) {
            Course course = new Course(CourseParser.NEED_ALEKS, "0");
            List<Course> result = new ArrayList<>();
            result.add(course);
            return result;
        }

        String input = str;
        input = input.replace("Three", "");
        String cat_regex = "[A-Z]+";
        String num_regex = "[0-9]+";
        Matcher cat_m = Pattern.compile(cat_regex).matcher(input);
        Matcher num_m = Pattern.compile(num_regex).matcher(input);
        final List<String> cat_matches = new ArrayList<>();
        final List<String> num_matches = new ArrayList<>();
        while(cat_m.find()) {
            cat_matches.add(cat_m.group(0));
        }
        while(num_m.find()) {
            num_matches.add(num_m.group(0));
        }
        List<Course> result = new ArrayList<Course>();

        for (int i = 0; i < cat_matches.size(); i++) {
            Course course = new Course(cat_matches.get(i), num_matches.get(i));
            result.add(course);
        }

        return result;
    }

    private static List<List<Course>> parsePrerequisiteFactory(List<String> input) {
        List<List<Course>> result = new ArrayList<>();

        for (String each : input) {
            result.add(parseStringForCourse(each));
        }

        return result;
    }
}
