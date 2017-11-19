package classes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import rolez.checked.lang.CheckedArray;

public class ImageReader {
    
    public static CheckedArray<CheckedArray<int[]>[]> read(String file) throws IOException {
        return fromBufferedImage(ImageIO.read(new File(file)));
    }
    
    public static CheckedArray<CheckedArray<int[]>[]> fromBufferedImage(BufferedImage image) {
        @SuppressWarnings("unchecked")
        CheckedArray<CheckedArray<int[]>[]> result = 
                new CheckedArray<CheckedArray<int[]>[]>(new CheckedArray[image.getHeight()]);
        for(int y = 0; y < image.getHeight(); y++) {
        	CheckedArray<int[]> row = new CheckedArray<int[]>(new int[image.getWidth()]);
            for(int x = 0; x < image.getWidth(); x++)
                row.setInt(x, image.getRGB(x, y));
            result.set(y, row);
        }
        return result;
    }
}