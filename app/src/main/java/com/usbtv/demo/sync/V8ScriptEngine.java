/*
 *Copyright © 2022 SMLOG
 *SMLOG
 *https://smlog.github.io
 *All rights reserved.
 */
package com.usbtv.demo.sync;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import com.eclipsesource.v8.V8;

public class V8ScriptEngine {

	private V8 runtime;

	public String eval(String string) {
		return runtime.executeStringScript(string);

	}

	public V8ScriptEngine() {
		super();
		runtime = V8.createV8Runtime();
	}

	public void eval(FileReader fileReader) throws IOException {


		LineNumberReader reader = new LineNumberReader(fileReader);

		String temp = null;

		StringBuffer sb = new StringBuffer();

		while ((temp = reader.readLine()) != null) {

			sb.append(temp).append("\n");

		}

		runtime.executeVoidScript(sb.toString().trim());

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public void release(){
		if(runtime!=null)runtime.release();
	}
	public void  eval(InputStream inputStream) {
		runtime.executeVoidScript(getStringByInputStream(inputStream));
	}

	public static String getStringByInputStream(InputStream inputStream) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			byte[] b = new byte[10240];
			int n;
			while ((n = inputStream.read(b)) != -1) {
				outputStream.write(b, 0, n);
			}
		} catch (Exception e) {
			try {
				inputStream.close();
				outputStream.close();
			} catch (Exception e1) {
			}
		}
		return outputStream.toString();
	}
}
