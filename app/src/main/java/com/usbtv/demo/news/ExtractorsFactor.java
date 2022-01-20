package com.usbtv.demo.news;

import java.util.ArrayList;
import java.util.List;

public class ExtractorsFactor {

	public static List<ListExtractor> getListExtractors() {
		List<ListExtractor> list = new ArrayList<ListExtractor>();
		list.add(new CCTVExtractor());
		list.add(new CNNExtractor());
		list.add(new GTExtractor());
		return list;
	}

}
