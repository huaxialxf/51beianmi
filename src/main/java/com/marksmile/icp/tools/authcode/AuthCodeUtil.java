package com.marksmile.icp.tools.authcode;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.marksmile.icp.tools.authcode.ImageWrapper.PointHandle;
import com.marksmile.utils.HttpClientKit;

public class AuthCodeUtil {

	private static Logger logger = LoggerFactory.getLogger(AuthCodeUtil.class);

	public static Map<BufferedImage, String> getTemplate() throws IOException {
		File file = new File("tempalte");
		File[] files = file.listFiles();
		Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
		for (File file2 : files) {
			String c = file2.getName().split("\\.", 2)[0].toUpperCase();
			BufferedImage image = ImageIO.read(file2);
			ImageWrapper wrapper = new ImageWrapper(image);
			wrapper.greyProcess(Color.WHITE, 100);
			wrapper.trimming();
			map.put(ImageWrapper.rendImage(48, 48, wrapper.getImage()), c);
		}
		return map;
	}

	public static void test(List<BufferedImage> list) {
		int n = 1;
		for (BufferedImage bufferedImage : list) {
			try {
				ImageWrapper imageWrapper = new ImageWrapper(bufferedImage);

				ImageIO.write(imageWrapper.trimming(), "bmp", new FileOutputStream("pics/" + (n++) + ".jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static List<BufferedImage> filter(List<BufferedImage> list) {
		List<BufferedImage> list1 = new ArrayList<>();
		if (list.size() > 6) {
			for (BufferedImage bufferedImage : list) {
				if (bufferedImage.getWidth() < 15) {
					continue;
				}
				list1.add(bufferedImage);
			}
		} else {
			for (BufferedImage bufferedImage : list) {
				list1.add(bufferedImage);
			}
		}
		return list1;
	}

	public static String getCode(BufferedImage bufferedImage) throws IOException {

		ImageWrapper wrapper = new ImageWrapper(bufferedImage);
		try {
			List<BufferedImage> list = wrapper.splitFull();
			list = filter(list);

			if (list.size() != 6) {
				return null;
			}
//			test(list);
			StringBuffer buffer = new StringBuffer();
			for (BufferedImage subImage : list) {
				buffer.append(getStr(subImage));
			}
			return buffer.toString();

		} catch (java.lang.StackOverflowError error) {
			logger.error("error pic--->err.bmp");
			ImageIO.write(bufferedImage, "bmp", new File("err.bmp"));
			return null;

		} catch (Throwable e) {
			return null;
		}

	}

	private static String getStr(BufferedImage bufferedImage) throws Exception {
		ImageWrapper imageWrapper = new ImageWrapper(bufferedImage);
		imageWrapper.greyProcess(Color.WHITE, 100);
		BufferedImage image2 = ImageWrapper.rendImage(45, 45, imageWrapper.trimming());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(image2, "bmp", bos);
		byte[] bytes = bos.toByteArray();
		String baseStr = Base64.getEncoder().encodeToString(bytes);
		HttpClientKit clientKit = new HttpClientKit();
		Map<String, String> map = new HashMap<String, String>();
		map.put("base", baseStr);
		String url = "http://127.0.0.1:5000/predict";

//		String url = "http://47.110.135.99:5000/predict";
//		String url = "http://192.168.1.65:5000/predict";
		String ret = clientKit.exePostMethodForString(url, null, map);
		JSONObject json = JSON.parseObject(ret);
		return json.getString("predict");

	}

	public static double getSimilarity(BufferedImage image1, BufferedImage image2)
			throws FileNotFoundException, IOException {
		ImageWrapper imageWrapper1 = new ImageWrapper(image1);
		ImageWrapper imageWrapper2 = new ImageWrapper(image2);
		imageWrapper1.greyProcess(Color.WHITE, 100);
		imageWrapper2.greyProcess(Color.WHITE, 100);

		AtomicInteger bAll = new AtomicInteger(0);
		AtomicInteger bJj = new AtomicInteger(0);
		AtomicInteger b1 = new AtomicInteger(0);
		AtomicInteger b2 = new AtomicInteger(0);

		imageWrapper1.iterator(new PointHandle() {

			@Override
			public void doHandle(int x, int y) {
				if (imageWrapper1.isBlack(x, y) && imageWrapper2.isBlack(x, y)) {
					bJj.incrementAndGet();
				}
				if (imageWrapper1.isBlack(x, y) && !imageWrapper2.isBlack(x, y)) {
					b1.incrementAndGet();
				}
				if (!imageWrapper1.isBlack(x, y) && imageWrapper2.isBlack(x, y)) {
					b2.incrementAndGet();
				}
				if (imageWrapper1.isBlack(x, y) || imageWrapper2.isBlack(x, y)) {
					bAll.incrementAndGet();
				}
			}
		});

		return (1.2 * bJj.get()) / bAll.get();
	}

	public static void main(String[] args) throws IOException {

		File file = new File("pics/test.jpg");
		String code = AuthCodeUtil.getCode(ImageIO.read(file));
		System.out.println(code);
	}

}
