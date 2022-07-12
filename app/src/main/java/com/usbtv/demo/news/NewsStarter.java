package com.usbtv.demo.news;

import java.io.IOException;
import java.text.ParseException;


public class NewsStarter {


    private static String token;


    public static  void run() throws IOException, ParseException, InterruptedException {

         UploadItemRepository uploadItemRepository = new UploadItemRepository();

        new Thread(new Runnable() {
                @Override
                public void run() {



                    token = uploadItemRepository.getToken();

                    while (true) {

                        if (!token.equalsIgnoreCase("")) {
                            try {

                                NewsFullText.getList(uploadItemRepository);
                                System.out.println("DONE");

                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            try {
                                Thread.sleep(3 * 3600 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        } else {
                            return;
                        }
                    }
                }
            }).start();


    }




}
