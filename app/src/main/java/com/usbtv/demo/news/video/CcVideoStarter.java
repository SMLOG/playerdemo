package com.usbtv.demo.news.video;

import java.io.IOException;
import java.text.ParseException;


public class CcVideoStarter {


	private static String token;


	public static  void run() throws IOException, ParseException, InterruptedException {

		CcVideoRepository repository = new CcVideoRepository();

		token = repository.getToken();

		if (!token.equalsIgnoreCase("")) {
			VideoTask.getList(repository);
			System.out.println("DONE");

		}

	}




}
