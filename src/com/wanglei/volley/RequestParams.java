package com.shequtong.yishequ.volley;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import com.shequtong.yishequ.database.Common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * 
 * TODO  请求参数  Map-->key/value (包含 Multipart file) 亲！
 * @author  Wanglei 
 * @data:  2015年6月4日 下午3:48:41 
 * @version:  V1.0
 */
public class RequestParams {
	
	private int id;
	private String url;
	private Map<String,String> baseParam;
	private String param;
	private ArrayList<String> cookies;
    public Type resClass;
    
	public Type getResClass() {
		return resClass;
	}
	public void setResClass(Type resClass) {
		this.resClass = resClass;
	}
	public ArrayList<String> getCookies() {
		return cookies;
	}
	public void setCookies(ArrayList<String> cookies) {
		this.cookies = cookies;
	}
	
	/**
	 * SharedPreferences拿到数据 如果：city，session，latitude，longitude，communityId，userId，不等于null 存储在baseParam中
	 * 
	 * @param id 
	 * @param context 
	 */
	public RequestParams(int id,Context context)  {
		this.id = id;
		baseParam = new HashMap<String, String>();
		//获取到SharedPreferences
		SharedPreferences mPref= PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		cookies = Common.getCooieArrayList(mPref);
		String city = mPref.getString(Common.USER_CITY_STRING, null);
		if (city != null) {
			baseParam.put("city", city);
		}
		String session = mPref.getString(Common.SESSION_STRING, null);
		if (session != null) {
			baseParam.put("session", session);
		}
		String latitude = mPref.getString(Common.USER_LATITUDE, null);
		if (latitude != null) {
			baseParam.put("latitude", latitude);
		}
		String longitude = mPref.getString(Common.USER_LONGITUDE, null);
		if (longitude != null) {
			baseParam.put("longitude", longitude);
		}
		String communityId = mPref.getString(Common.USER_COMMUNITYID, null);
		if (communityId != null) {
			baseParam.put("communityId", communityId);
		}
		String userId = mPref.getString(Common.USERID, null);
		if (communityId != null) {
			baseParam.put("userId", userId);
		}
   	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Map<String, String> getBaseParam() {
		return baseParam;
	}
	public void setBaseParam(Map<String, String> baseParam) {
		this.baseParam = baseParam;
	}
	public String getParam() {
		return param;
	}

	public void setParam(String param){
		this.param = param;
	}
	
	/**
	 * 遍历baseParam ，得到param格式为 key+"="+value
	 */
	public void setParam() {
		Set<String> set = baseParam.keySet();

		for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			String value = (String) baseParam.get(key);
			 System.out.println(key+"===="+value); /*-----------------输出格式----------------------*/
			if (param != null) {
				param += "&" + key+"="+value;
			} else {
				param = key+"="+value;
			}
		}
	}
	
	
	/*------------------------------------- 区分不同格式的文件---------------------------------------------*/
	private static String ENCODING = "UTF-8";

	protected ConcurrentHashMap<String, String> urlParams;

	protected ConcurrentHashMap<String, FileWrapper> fileParams;

	private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	
	public RequestParams() {
		init();
	}

	public RequestParams(String key, String value) {
		init();
		put(key, value);
	}
	
	private void init() {
		urlParams = new ConcurrentHashMap<String, String>();
		fileParams = new ConcurrentHashMap<String, FileWrapper>();
	}

	/**
	 * @param key
	 * @param value
	 */
	public void put(String key, String value) {
		if (key != null && value != null) {
			urlParams.put(key, value);
		}
	}

	/**
	 * @param key
	 * @param file
	 */
	public void put(String key, File file) {
		try {
			put(key, new FileInputStream(file), file.getName());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @param key
	 * @param stream
	 * @param fileName
	 */
	public void put(String key, InputStream stream, String fileName) {
		put(key, stream, fileName, null);
	}

	/**
	 * @param key
	 * @param stream
	 * @param fileName
	 * @param contentType
	 */
	public void put(String key, InputStream stream, String fileName, String contentType) {
		if (key != null && stream != null) {
			fileParams.put(key, new FileWrapper(stream, fileName, contentType));
		}
	}
	
	/**
	 * 
	 * TODO 文件
	 * @return
	 * @throw 
	 * @return HttpEntity
	 */
	public HttpEntity getEntity() {
		HttpEntity entity = null;
		if (!fileParams.isEmpty()) {
			MultipartEntity multipartEntity = new MultipartEntity();
			for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {//添加 String类型 参数
				multipartEntity.addPart(entry.getKey(), entry.getValue());
			}
			int currentIndex = 0;
			int lastIndex = fileParams.entrySet().size() - 1;
			for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams.entrySet()) {//添加 File类型 参数
				FileWrapper file = entry.getValue();
				if (file.inputStream != null) {
					boolean isLast = currentIndex == lastIndex;
					if (file.contentType != null) {
						multipartEntity.addPart(entry.getKey(), file.getFileName(), file.inputStream, file.contentType,
								isLast);
					} else {
						multipartEntity.addPart(entry.getKey(), file.getFileName(), file.inputStream, isLast);
					}
				}
				currentIndex++;
			}
			entity = multipartEntity;
		} else {
			try {
				entity = new UrlEncodedFormEntity(getParamsList(), ENCODING);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return entity;
	}
	
	/**
	 * 
	 * TODO 遍历参数
	 * @return
	 * @throw 
	 * @return List<BasicNameValuePair>
	 */
	protected List<BasicNameValuePair> getParamsList() {
		List<BasicNameValuePair> lparams = new ArrayList<BasicNameValuePair>();
//		for (ConcurrentHashMap.Entry<String, String> entry : urlParams.entrySet()) {
//			lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
//		}
		
		Iterator<?> iter = urlParams.entrySet().iterator(); 
		while (iter.hasNext()) { 
			@SuppressWarnings("unchecked")
			ConcurrentHashMap.Entry<String, String> entry =  (Entry<String, String>) iter.next(); 
		    lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		} 
//		for(int i=0 ;i<lparams.size();i++){
//			Log.e("param", lparams.get(i).toString());
//		}
		return lparams;
	}

	private static class FileWrapper {
		public InputStream inputStream;
		public String fileName;
		public String contentType;

		public FileWrapper(InputStream inputStream, String fileName, String contentType) {
			this.inputStream = inputStream;
			this.fileName = fileName;
			this.contentType = contentType;
		}

		public String getFileName() {
			if (fileName != null) {
				return fileName;
			} else {
				return "nofilename";
			}
		}
	}

	class MultipartEntity implements HttpEntity {
		private String boundary = null;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		boolean isSetLast = false;

		boolean isSetFirst = false;

		public MultipartEntity() {
			final StringBuffer buf = new StringBuffer();
			final Random rand = new Random();
			for (int i = 0; i < 30; i++) {
				buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
			}
			this.boundary = buf.toString();
		}

		public void writeFirstBoundaryIfNeeds() {
			if (!isSetFirst) {
				try {
					out.write(("--" + boundary + "\r\n").getBytes());
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			isSetFirst = true;
		}

		public void writeLastBoundaryIfNeeds() {
			if (isSetLast) {
				return;
			}
			try {
				out.write(("\r\n--" + boundary + "--\r\n").getBytes());
			} catch (final IOException e) {
				e.printStackTrace();
			}
			isSetLast = true;
		}

		public void addPart(final String key, final String value) {
			writeFirstBoundaryIfNeeds();
			try {
				out.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n").getBytes());
				out.write(value.getBytes());
				out.write(("\r\n--" + boundary + "\r\n").getBytes());
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		public void addPart(final String key, final String fileName, final InputStream fin, final boolean isLast) {
			addPart(key, fileName, fin, "application/octet-stream", isLast);
		}

		public void addPart(final String key, final String fileName, final InputStream fin, String type, final boolean isLast) {
			writeFirstBoundaryIfNeeds();
			try {
				type = "Content-Type: " + type + "\r\n";
				out.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n")
						.getBytes());
				out.write(type.getBytes());
				out.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());

				final byte[] tmp = new byte[4096];
				
				int l = 0;
				while ((l = fin.read(tmp)) != -1) {
					out.write(tmp, 0, l);
				}
				if (!isLast)
					out.write(("\r\n--" + boundary + "\r\n").getBytes());
				else {
					writeLastBoundaryIfNeeds();
				}
				out.flush();
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fin.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void addPart(final String key, final File value, final boolean isLast) {
			try {
				addPart(key, value.getName(), new FileInputStream(value), isLast);
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		@Override
		public long getContentLength() {
			writeLastBoundaryIfNeeds();
			return out.toByteArray().length;
		}

		@Override
		public Header getContentType() {
			return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
		}

		@Override
		public boolean isChunked() {
			return false;
		}

		@Override
		public boolean isRepeatable() {
			return false;
		}

		@Override
		public boolean isStreaming() {
			return false;
		}

		@Override
		public void writeTo(final OutputStream outstream) throws IOException {
			outstream.write(out.toByteArray());
		}

		@Override
		public Header getContentEncoding() {
			return null;
		}

		@Override
		public void consumeContent() throws IOException, UnsupportedOperationException {
			if (isStreaming()) {
				throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
			}
		}

		@Override
		public InputStream getContent() throws IOException, UnsupportedOperationException {
			return new ByteArrayInputStream(out.toByteArray());
		}
	}

}