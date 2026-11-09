package com.app.povserver.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class PovMatrixConverterService {

  public enum ColorOrder {
    RGB,
    GRB // Стандартный порядок байтов WS2812B
  }


  public byte[] convertToPolarMatrix(
          byte[] imageBytes,
          int ledCount,
          int sectorsCount,
          ColorOrder colorOrder
  ) throws IOException {
    BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
    if (originalImage == null) {
      throw new IllegalArgumentException("Файл не является поддерживаемым изображением");
    }

    int targetSize = Math.max(ledCount * 4, 256);
    BufferedImage squareImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = squareImage.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.drawImage(originalImage, 0, 0, targetSize, targetSize, null);
    g2d.dispose();

    double centerX = (targetSize - 1) / 2.0;
    double centerY = (targetSize - 1) / 2.0;
    double maxRadius = targetSize / 2.0;

    byte[] outputMatrix = new byte[sectorsCount * ledCount * 3];
    int writeIndex = 0;

    double angleStep = (2.0 * Math.PI) / sectorsCount;

    for (int sector = 0; sector < sectorsCount; sector++) {
      double angle = sector * angleStep;
      double cos = Math.cos(angle);
      double sin = Math.sin(angle);

      for (int led = 0; led < ledCount; led++) {
        double r = ((double) led / (ledCount - 1)) * maxRadius;
        int x = (int) Math.round(centerX + r * cos);
        int y = (int) Math.round(centerY + r * sin);

        // Защита границ
        x = Math.clamp(x, 0, targetSize - 1);
        y = Math.clamp(y, 0, targetSize - 1);

        int rgb = squareImage.getRGB(x, y);
        int red   = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue  = rgb & 0xFF;

        if (colorOrder == ColorOrder.GRB) {
          outputMatrix[writeIndex++] = (byte) green;
          outputMatrix[writeIndex++] = (byte) red;
          outputMatrix[writeIndex++] = (byte) blue;
        } else {
          outputMatrix[writeIndex++] = (byte) red;
          outputMatrix[writeIndex++] = (byte) green;
          outputMatrix[writeIndex++] = (byte) blue;
        }
      }
    }

    return outputMatrix;
  }
}