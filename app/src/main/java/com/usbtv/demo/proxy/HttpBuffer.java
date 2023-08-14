package com.usbtv.demo.proxy;

import androidx.annotation.NonNull;

import com.usbtv.demo.comm.SSLSocketClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpBuffer extends ByteArrayOutputStream {
	public int id;
	private List<OutputStream> outs = new ArrayList<>();

	private static CurrentBufferHolder bufferHolder = new CurrentBufferHolder();

	public static HttpBuffer getBuffer(String bvid,int p,int id,int typeid) {
		synchronized (bufferHolder) {
			HttpBuffer bf;
			if (bufferHolder.bf!=null && bufferHolder.bf.id==id ) {
				bf = bufferHolder.bf;
			} else{
				bf = new HttpBuffer(bvid,p,id,typeid);
				bufferHolder.bf = bf;
			}
			return bf;
		}
	}

	private HttpBuffer(String bvid,int p,int id,int typeid) {

		super(8196);
		synchronized (bufferHolder) {

			this.id = id;
			bufferHolder.bf = this;
		}
		;
		new Thread(new Runnable() {

			@Override
			public void run() {
				HttpsURLConnection.setDefaultSSLSocketFactory(SSLSocketClient.getSSLSocketFactory());
				long timeoutMillisecond = 10000l;

				HttpURLConnection httpURLConnection;
				try {
					String url = getUrl(typeid, bvid, p);
					System.out.println(url);
					httpURLConnection = (HttpURLConnection) new URL(url).openConnection();

					httpURLConnection.setConnectTimeout((int) timeoutMillisecond);
					httpURLConnection.setUseCaches(false);
					httpURLConnection.setReadTimeout((int) timeoutMillisecond);
					httpURLConnection.setDoInput(true);
					InputStream in = httpURLConnection.getInputStream();

					int len = 0;
					byte[] bytes = new byte[8192];

						while ((len = in.read(bytes)) != -1) {
							//System.out.println(len);
							if(bufferHolder.bf==null || bufferHolder.bf.id!=id){
								break;
							}
							write(bytes, 0, len);
						}


					System.out.println("done");

					in.close();

				} catch (Throwable e) {
					e.printStackTrace();
				} finally {

					for (int i = 0; i < outs.size(); i++) {
						try {
							outs.remove(i).flush();
						} catch (Throwable e) {

						}
					}
					//synchronized (bufferHolder) {
					//	bufferHolder.bf=null;
					//}

				}

			}
		}).start();

	}

	@NonNull
	public static String getUrl(int typeid, String bvid, int p) throws IOException {
		String targetUrl = typeid >=800? "https://www.youtube.com/watch?v="+ bvid : ("https://www.bilibili.com/video/" +
				bvid + "?p=" + p + "&spm_id_from=pageDriver");
		String url = ULinkDownload.getLink(targetUrl);
		return url;
	}


	@Override
	public synchronized void write(byte[] b, int off, int len) {

		if(count<8196){
			super.write(b, off, len);
		}
		for (int i = 0; i < outs.size(); i++) {
			try {
				outs.get(i).write(b, off, len);
				outs.get(i).flush();
			} catch (Throwable e) {
				e.printStackTrace();
				outs.remove(i);
				i--;
			}
		}
	}

	public void addOuptput(OutputStream out) {

		try {
			synchronized (this) {

				out.write(this.toByteArray());
				outs.add(out);
				while (true) {
					if (!outs.contains(out))
						break;
					this.notifyAll();
					this.wait();

				}

			}

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HttpBuffer getBuffer(String bvid, int p, int id,int typeid, OutputStream outputStream) {

		HttpBuffer byos = HttpBuffer.getBuffer(bvid,p,id,typeid);
		byos.addOuptput(outputStream);
		return byos;
	}


}
