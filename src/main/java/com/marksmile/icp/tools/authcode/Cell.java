package com.marksmile.icp.tools.authcode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Cell {
    private int x;
    private int y;
    private ImageWrapper wrapper;
    private List<Cell> sublist = null;
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ImageWrapper getWrapper() {
        return wrapper;
    }

    public Cell(ImageWrapper wrapper, int x, int y) {
        this.wrapper = wrapper;
        this.x = x;
        this.y = y;

    }

    private void init2() {

        List<Cell> tempList = new ArrayList<Cell>();
        sublist = new ArrayList<Cell>();

        tempList.add(new Cell(wrapper, x - 1, y - 1));
        tempList.add(new Cell(wrapper, x, y - 1));
        tempList.add(new Cell(wrapper, x + 1, y - 1));
        tempList.add(new Cell(wrapper, x - 1, y));
        // sublist.add(new Cell(wrapper, x, y));
        tempList.add(new Cell(wrapper, x + 1, y));

        tempList.add(new Cell(wrapper, x - 1, y + 1));
        tempList.add(new Cell(wrapper, x, y + 1));
        tempList.add(new Cell(wrapper, x + 1, y + 1));
        for (Cell cell : tempList) {
            if (cell.x < wrapper.getW() && cell.x > -1 && cell.y < wrapper.getH() && cell.y > -1) {
                if (wrapper.isBlack(cell.x, cell.y)) {
                    sublist.add(cell);
                }
            }
        }
    }

    private void init() {

        List<Cell> tempList = new ArrayList<Cell>();
        sublist = new ArrayList<Cell>();
        int r  =1 ;
        for (int i = x - r; i <= (x + r); i++) {
            for (int j = y - r; j <= (y + r); j++) {
                if(x==i && j==y){
                    continue ;
                }
                
                tempList.add(new Cell(wrapper, i, j));
            }
        }

        
        for (Cell cell : tempList) {
            if (cell.x < wrapper.getW() && cell.x > -1 && cell.y < wrapper.getH() && cell.y > -1) {
                if (wrapper.isBlack(cell.x, cell.y)) {
                    sublist.add(cell);
                }
            }
        }
    }

    public List<Cell> getSublist() {
        if (sublist == null) {
            init();
        }
        return sublist;
    }

    public boolean isBlack() {
        return wrapper.isBlack(x, y);
    }

    public static void main(String[] args) throws IOException {

        FileInputStream is = new FileInputStream(new File("2_01.jpg"));
        BufferedImage image = javax.imageio.ImageIO.read(is);

        ImageWrapper wrapper = new ImageWrapper(image);
        Cell cell = new Cell(wrapper, 48, 39);
        List<Cell> list = cell.getSublist();
        for (Cell cell2 : list) {
            System.out.println("key:" + cell2.x + "_" + cell2.y);
        }
    }

}
