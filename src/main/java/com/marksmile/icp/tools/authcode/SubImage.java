package com.marksmile.icp.tools.authcode;

import java.awt.image.BufferedImage;

public class SubImage {
    private int startX;
    private BufferedImage image;
    private int numBlack;

    public SubImage(int startX, BufferedImage image, int numBlack) {
        super();
        this.startX = startX;
        this.image = image;
        this.numBlack = numBlack;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public int getNumBlack() {
        return numBlack;
    }

    public void setNumBlack(int numBlack) {
        this.numBlack = numBlack;
    }

}
