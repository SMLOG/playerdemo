package com.usbtv.demo;


public class VideoItem {
    public static final class Table {
        public static final String NAME = "data";

        public static final class Cols {
            public static final String ID = "id";
            public static final String URL = "url";
            public static final String TITLE="title";
            public static final String CAT = "cat";
            public static final String CREATE_DATE = "create_date";
            public static final String SEQ = "seq";
            public static final String TIMES = "times";
            public static final String LAST_UPATE = "last_update";
            public static final String STATUS="status";
        }
    }
    public Integer id;
    public String url;
    public String title;
    public Long createDate;
    public String cat;
    public Long lastUpdate;
    public Integer seq=0;
    public Integer times=0;
    public Integer status=0;
    public String thumb;

}