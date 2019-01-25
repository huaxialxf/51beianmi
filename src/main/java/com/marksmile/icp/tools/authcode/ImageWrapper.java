package com.marksmile.icp.tools.authcode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

public class ImageWrapper {
    private BufferedImage image;
    private int w;
    private int h;

    public BufferedImage getImage() {
        return image;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public ImageWrapper(BufferedImage image) {
        this.image = image;
        w = image.getWidth();
        h = image.getHeight();
    }

    public boolean isBlack(int x, int y) {
        return image.getRGB(x, y) == Color.BLACK.getRGB();
    }

    public boolean isWhite(int x, int y) {
        return image.getRGB(x, y) == Color.WHITE.getRGB();
    }

    public Color getColor(int x, int y) {
        int rgb = image.getRGB(x, y);
        return new Color((rgb & 0xff0000) >> 16, (rgb & 0xff00) >> 8, (rgb & 0xff));
    }

    public static Color getColorAvg(Color[] cs) {
        int r = 0, g = 0, b = 0;
        for (Color color : cs) {
            r = r + color.getRed();
            g = g + color.getGreen();
            b = b + color.getBlue();
        }
        int l = cs.length;
        return new Color(r / l, g / l, b / l);
    }

    public static Color getColorAvg(List<Color> cs) {
        int r = 0, g = 0, b = 0;
        for (Color color : cs) {
            r = r + color.getRed();
            g = g + color.getGreen();
            b = b + color.getBlue();
        }
        int l = cs.size();
        return new Color(r / l, g / l, b / l);
    }

    /**
     * 灰度处理
     * 
     * @param distance
     *            处理参数 ,和底色的差距
     * @Description :
     */
    public void greyProcess(int distance) {

        Color c1 = getColor(1, 1);
        Color c2 = getColor(1, h - 2);
        Color c3 = getColor(w - 2, 1);
        Color c4 = getColor(w - 2, h - 2);
        Color floorColor = getColorAvg(new Color[] { c1, c2, c3, c4 });

        this.iterator(new PointHandle() {
            @Override
            public void doHandle(int x, int y) {
                double d = ColorUtil.getColorDistance(getColor(x, y), floorColor);
                if (d < distance) {
                    image.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        });
    }

    public void greyProcess(Color floorColor, int distance) {

        this.iterator(new PointHandle() {
            @Override
            public void doHandle(int x, int y) {
                double d = ColorUtil.getColorDistance(getColor(x, y), floorColor);
                if (d < distance) {
                    image.setRGB(x, y, Color.WHITE.getRGB());
                } else {
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }
            }
        });
    }

    private BufferedImage copyData() {

        BufferedImage buffImg = new BufferedImage(this.w, this.h, BufferedImage.TYPE_INT_RGB);
        iterator(new PointHandle() {

            @Override
            public void doHandle(int x, int y) {
                buffImg.setRGB(x, y, ImageWrapper.this.getColor(x, y).getRGB());
            }
        });
        return buffImg;
    }

    public void testColors() {

        Color c1 = getColor(1, 1);
        Color c2 = getColor(1, h - 2);
        Color c3 = getColor(w - 2, 1);
        Color c4 = getColor(w - 2, h - 2);
        Color floorColor = getColorAvg(new Color[] { c1, c2, c3, c4 });
        Map<Double, Color> map = new HashMap<Double, Color>();
        Map<String, Double> typeMap = new HashMap<String, Double>();
        Set<Color> set = new HashSet<Color>();
        this.iterator(new PointHandle() {
            @Override
            public void doHandle(int x, int y) {
                double d = ColorUtil.getColorDistance(getColor(x, y), floorColor);
                if (!map.containsKey(d)) {
                    map.put(d, getColor(x, y));
                    boolean isOk = false;
                    for (String key : typeMap.keySet()) {
                        double value = typeMap.get(key);
                        if (Math.abs(d - value) < 100) {
                            isOk = true;
                            break;
                        }
                    }
                    if (!isOk) {
                        typeMap.put("k_" + typeMap.size(), d);
                    }
                }
            }
        });
//        System.out.println(map.size());
//        System.out.println(typeMap.size());

    }

    public void iterator(PointHandle handler) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                handler.doHandle(x, y);
            }
        }
    }

    // 去除周边的空白
    public BufferedImage trimming() {
        AtomicInteger x1 = new AtomicInteger(1000);
        AtomicInteger x2 = new AtomicInteger(0);

        AtomicInteger y1 = new AtomicInteger(1000);
        AtomicInteger y2 = new AtomicInteger(0);

        this.iterator(new PointHandle() {
            @Override
            public void doHandle(int x, int y) {
                if (isBlack(x, y)) {
                    x1.set(Math.min(x1.get(), x));
                    x2.set(Math.max(x2.get(), x));
                    y1.set(Math.min(y1.get(), y));
                    y2.set(Math.max(y2.get(), y));
                }
            }
        });
        image = image.getSubimage(x1.get(), y1.get(), (x2.get() - x1.get() + 1), (y2.get() - y1.get() + 1));
        return image;
    }

    public static BufferedImage spin(BufferedImage bi, int degree)  {
        int swidth = 0; // 旋转后的宽度
        int sheight = 0; // 旋转后的高度
        int x; // 原点横坐标
        int y; // 原点纵坐标

        // 处理角度--确定旋转弧度
        degree = degree % 360;
        if (degree < 0)
            degree = 360 + degree;// 将角度转换到0-360度之间
        double theta = Math.toRadians(degree);// 将角度转为弧度

        // 确定旋转后的宽和高
        if (degree == 180 || degree == 0 || degree == 360) {
            swidth = bi.getWidth();
            sheight = bi.getHeight();
        } else if (degree == 90 || degree == 270) {
            sheight = bi.getWidth();
            swidth = bi.getHeight();
        } else {
            swidth = (int) (Math.sqrt(bi.getWidth() * bi.getWidth() + bi.getHeight() * bi.getHeight()));
            sheight = (int) (Math.sqrt(bi.getWidth() * bi.getWidth() + bi.getHeight() * bi.getHeight()));
        }

        x = (swidth / 2) - (bi.getWidth() / 2);// 确定原点坐标
        y = (sheight / 2) - (bi.getHeight() / 2);

        BufferedImage spinImage = new BufferedImage(swidth, sheight, bi.getType());
        // 设置图片背景颜色
        Graphics2D gs = (Graphics2D) spinImage.getGraphics();
        gs.setColor(Color.white);
        gs.fillRect(0, 0, swidth, sheight);// 以给定颜色绘制旋转后图片的背景

        AffineTransform at = new AffineTransform();
        at.rotate(theta, swidth / 2, sheight / 2);// 旋转图象
        at.translate(x, y);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
        spinImage = op.filter(bi, spinImage);
        return spinImage;

    }

    public static void main(String[] args) throws Exception {
        BufferedImage buffer = ImageIO.read(new File("G:/data_pic/test_trim/0.bmp"));
        ImageIO.write(buffer, "bmp", new FileOutputStream(new File("G:/data_pic/test_trim/0_1.bmp")));
        buffer = spin(buffer,15);
//        ImageWrapper wrapper = new ImageWrapper(buffer);
//        wrapper.greyProcess(Color.WHITE, 100);
//        buffer = wrapper.getImage();
//        buffer = wrapper.trimming();
        
        ImageIO.write(buffer, "bmp", new FileOutputStream(new File("G:/data_pic/test_trim/0_4.bmp")));
    }

    /**
     * 去噪
     * 
     * @param range
     *            判断周围的方位
     * @param num
     *            最小黑点数量
     * @Description :
     */
    public void denoising(int range, int num) {

        List<ImagePoint> list = new ArrayList<ImagePoint>();
        this.iterator(new PointHandle() {
            @Override
            public void doHandle(int x, int y) {
                if (getNumBlackNeighbor(x, y, range) <= num) {
                    list.add(new ImagePoint(x, y));
                }
            }
        });

        for (ImagePoint ip : list) {
            image.setRGB(ip.x, ip.y, Color.WHITE.getRGB());
        }

    }

    // 坐标xy，周围距离为n单位的范围内，黑点数量
    public int getNumBlackNeighbor(int x, int y, int n) {
        int minX = x - n;
        int maxX = x + n;
        int minY = y - n;
        int maxY = y + n;

        int num = 0;
        for (int w = minX; w <= maxX; w++) {
            for (int h = minY; h <= maxY; h++) {
                if (w < 0 || w >= image.getWidth() || h < 0 || h >= image.getHeight()) {
                    continue;
                }
                if (isBlack(w, h)) {
                    num++;
                }
            }
        }
        return num;

    }

    // 获取cell的所有连接点。
    public static void addCell(Map<String, Cell> map, Cell cell) {
        String key = cell.getX() + "_" + cell.getY();
        map.put(key, cell);

        List<Cell> sublist = cell.getSublist();

        for (Cell subCell : sublist) {
            String _key = subCell.getX() + "_" + subCell.getY();
            if (!map.containsKey(_key)) {
                addCell(map, subCell);
            }
        }

    }

    // 渲染图片，把图片发到指定长度和宽度的图片中间。
    public static BufferedImage rendImage(int w, int h, BufferedImage subimage) {
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        ImageWrapper pWrapper = new ImageWrapper(bufferedImage);
        PointHandle hander2 = (int x, int y) -> {
            try {
                bufferedImage.setRGB(x, y, Color.WHITE.getRGB());
            } catch (Exception e) {
//                System.err.println("2 x: " + x + " y:" + y);
            }
        };
        pWrapper.iterator(hander2);

        int xLen = (w - subimage.getWidth()) / 2;
        int yLen = (h - subimage.getHeight()) / 2;

        ImageWrapper imageWrapper = new ImageWrapper(subimage);
        PointHandle hander = (int x, int y) -> {
            if (imageWrapper.isBlack(x, y)) {
                try {
                    bufferedImage.setRGB(xLen + x, yLen + y, Color.BLACK.getRGB());
                } catch (Exception e) {
//                    System.err.println("x:" + x + " y:" + y);
                }
            }
        };

        imageWrapper.iterator(hander);
        return bufferedImage;

    }

    public List<SubImage> split() throws IOException {
        ImageWrapper wrapper = this;
        Map<String, Cell> allCell = new HashMap<String, Cell>();
        PointHandle handler = (int x, int y) -> {
            if (wrapper.isBlack(x, y)) {
                allCell.put(x + "_" + y, new Cell(wrapper, x, y));
            }
        };
        wrapper.iterator(handler);
//        System.out.println("list.size == " + allCell.size());
        List<SubImage> list = new ArrayList<SubImage>();
        while (!allCell.isEmpty()) {
            Map<String, Cell> map = new HashMap<String, Cell>();// 连续点的集合
            String _key = allCell.keySet().iterator().next();
            addCell(map, allCell.get(_key));// 通过函数递归，第一个连续点切除.
//            System.out.println("map----" + map.size());
            int minX = wrapper.getW();
            int maxX = 0;
            int minY = wrapper.getH();
            int maxY = 0;

            for (String key : map.keySet()) {
                Cell cell = map.get(key);
                minX = Math.min(minX, cell.getX());
                maxX = Math.max(maxX, cell.getX());
                minY = Math.min(minY, cell.getY());
                maxY = Math.max(maxY, cell.getY());
                allCell.remove(cell.getX() + "_" + cell.getY());
            }
//            System.out.println("allCell.size == " + allCell.size());
            if (map.size() > 18) {
                // BufferedImage subimage = wrapper.getImage().getSubimage(minX,
                // minY, (maxX - minX) + 1, (maxY - minY) + 1);
                BufferedImage subimage = new BufferedImage((maxX - minX) + 1, wrapper.getH(), BufferedImage.TYPE_INT_RGB);
                for (int x = 0; x < subimage.getWidth() ; x++) {
                    for (int y = 0; y < subimage.getHeight() ; y++) {
                        String key = (x + minX) + "_" + y;
                        if (map.containsKey(key)) {
                            subimage.setRGB(x, y, Color.BLACK.getRGB());
                        } else {
                            subimage.setRGB(x, y, Color.WHITE.getRGB());
                        }
                    }
                }
//                System.out.println("width:" + (maxX - minX));
                list.add(new SubImage(minX, subimage, map.size()));
            }

        }
        Collections.sort(list, new Comparator<SubImage>() {
            public int compare(SubImage arg0, SubImage arg1) {
                int hits1 = arg0.getStartX();
                int hits0 = arg1.getStartX();
                if (hits1 > hits0) {
                    return 1;
                } else if (hits1 == hits0) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return list;
    }

    public static void test(List<SubImage> list) {
        int n = 1;
        for (SubImage bufferedImage : list) {
            try {
                ImageIO.write(bufferedImage.getImage(), "bmp", new FileOutputStream("G:/data_pic/test/1_" + (n++) + ".jpg"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public List<BufferedImage> splitFull() throws IOException {
        List<BufferedImage> listSubImages = new ArrayList<BufferedImage>();
        BufferedImage orgImage = this.copyData();
        greyProcess(100);
        denoising(2, 12);
        List<SubImage> list = this.split();
        for (SubImage subImage : list) {
            if (subImage.getImage().getWidth() > 31) {// 有连接在一起的，再拆分。
                BufferedImage srcImage = orgImage.getSubimage(subImage.getStartX(), 0, subImage.getImage().getWidth(), orgImage.getHeight());
                BufferedImage curImage = subImage.getImage();
                ImageWrapper wrapper2 = new ImageWrapper(curImage);
                wrapper2.iterator(new PointHandle() {
                    @Override
                    public void doHandle(int x, int y) {
                        if (wrapper2.isWhite(x, y)) {
                            srcImage.setRGB(x, y, Color.WHITE.getRGB());
                        }
                    }
                });
                listSubImages.addAll(ImageWrapper.splitByColor(srcImage));
                continue;
            }
            listSubImages.add(subImage.getImage());
        }

        return listSubImages;
    }

    public static List<BufferedImage> splitByColor(BufferedImage srcImage) throws IOException {
        ImageWrapper wrapper = new ImageWrapper(srcImage);
        Color c1 = wrapper.getColor(1, 1);
        Color c2 = wrapper.getColor(1, srcImage.getHeight() - 2);
        Color c3 = wrapper.getColor(srcImage.getWidth() - 2, 1);
        Color c4 = wrapper.getColor(srcImage.getWidth() - 2, srcImage.getHeight() - 2);
        Color floorColor = ImageWrapper.getColorAvg(new Color[] { c1, c2, c3, c4 });
        List<Color> list = new ArrayList<Color>();
        for (int x = 0; x < srcImage.getWidth() - 1; x++) {
            if (x > (srcImage.getWidth() / 4 - 3) && x < (srcImage.getWidth() / 4 + 3)) {
                for (int y = 0; y < srcImage.getHeight() - 1; y++) {
                    double d = ColorUtil.getColorDistance(wrapper.getColor(x, y), floorColor);
                    if (d > 100) {
                        list.add(wrapper.getColor(x, y));
                    }
                }
            }
        }
        Color leftColor = ImageWrapper.getColorAvg(list); // 左边的平均色
        list = new ArrayList<Color>();

        for (int x = 0; x < srcImage.getWidth() - 1; x++) {
            if (x > (srcImage.getWidth() * 3 / 4 - 3) && x < (srcImage.getWidth() * 3 / 4 + 3)) {
                for (int y = 0; y < srcImage.getHeight() - 1; y++) {
                    double d = ColorUtil.getColorDistance(wrapper.getColor(x, y), floorColor);
                    if (d > 100) {
                        list.add(wrapper.getColor(x, y));
                    }
                }
            }
        }
        Color rightColor = ImageWrapper.getColorAvg(list);// 右边的平均色

        List<String> listLeft = new ArrayList<String>();
        List<String> listRight = new ArrayList<String>();
        for (int x = 0; x < srcImage.getWidth() - 1; x++) {
            for (int y = 0; y < srcImage.getHeight() - 1; y++) {
                double d = ColorUtil.getColorDistance(wrapper.getColor(x, y), floorColor);
                if (d > 100) {
                    if (x < srcImage.getWidth() / 4) {
                        listLeft.add(x + "_" + y);
                    }
                    if (x > srcImage.getWidth() * 3 / 4) {
                        listRight.add(x + "_" + y);
                    } else {
                        double d1 = ColorUtil.getColorDistance(wrapper.getColor(x, y), leftColor);
                        double d2 = ColorUtil.getColorDistance(wrapper.getColor(x, y), rightColor);
                        if (d1 > d2) {
                            listRight.add(x + "_" + y);
                        } else {
                            listLeft.add(x + "_" + y);
                        }
                    }
                }
            }
        }

        List<BufferedImage> listSubImages = new ArrayList<BufferedImage>();
        listSubImages.add(subImagesGress(srcImage, listLeft).getImage());
        listSubImages.add(subImagesGress(srcImage, listRight).getImage());
        return listSubImages;

    }

    private static SubImage subImagesGress(BufferedImage srcImage, List<String> subPointSet) throws IOException {
        BufferedImage buffImg = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < buffImg.getWidth() - 1; x++) {
            for (int y = 0; y < buffImg.getHeight() - 1; y++) {
                String key = x + "_" + y;
                if (subPointSet.contains(key)) {
                    buffImg.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    buffImg.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        ImageWrapper wrapper = new ImageWrapper(buffImg);
        wrapper.greyProcess(100);
        wrapper.denoising(2, 12);
        List<SubImage> list = wrapper.split();
        return list.get(0);
    }

    @FunctionalInterface
    public interface PointHandle {
        public void doHandle(int x, int y);
    }

    class ImagePoint {
        int x;
        int y;

        public ImagePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
