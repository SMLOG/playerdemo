/*
 *Copyright © 2022 SMLOG
 *SMLOG
 *https://smlog.github.io
 *All rights reserved.
 */
package com.usbtv.demo.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MemCacheManager {
	
	
	
	private static LinkedHashMap<String,IndexCache> indexCacheMap =new LinkedHashMap<String,IndexCache>();
	
	public static   CacheItem  curTsUrl(String url) {
		
		IndexCache cache;
		
		Iterator<String> it = indexCacheMap.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			cache = indexCacheMap.get(key);
			if(cache==null) continue;
			int index = cache.belongCache(url);
			if(index>-1) {
				
				if(cache.needStartCache(index)) {
					cache.startDownload();
					synchronized (cache) {
						cache.notifyAll();
					}
				}
		
				return cache.waitForReady(index);
				

			}else if(cache.canDel()) {
				indexCacheMap.remove(key);
				//indexCacheMap.put(key, null);
			}
			
			
		}

		return null;

	}
	
	public static void buildIndexFrom(String index,ArrayList<String> tsUrls) {
		
		synchronized (indexCacheMap) {
			if(indexCacheMap.get(index)==null)
				indexCacheMap.put(index, new IndexCache(tsUrls));
		}

		
	}
	
	

}
