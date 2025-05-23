package com._project._project.Image;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.awt.Graphics2D;
import java.awt.Image;

@Service
public class ImageService {
    
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 800;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/jpg"};

    public String processImage(MultipartFile file) throws IOException {
        validateImage(file);
        
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        BufferedImage processedImage = resizeImage(originalImage);
        
        return convertToBase64(processedImage, getFileExtension(file.getContentType()));
    }

    private void validateImage(MultipartFile file) throws IOException {
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds 5MB limit");
        }

        // Check file type
        boolean isValidType = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equals(file.getContentType())) {
                isValidType = true;
                break;
            }
        }
        if (!isValidType) {
            throw new IOException("Invalid file type. Only JPG and PNG allowed");
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Check if resizing is needed
        if (originalWidth <= MAX_WIDTH && originalHeight <= MAX_HEIGHT) {
            return originalImage;
        }

        // Calculate new dimensions
        double scale = Math.min((double) MAX_WIDTH / originalWidth, 
                              (double) MAX_HEIGHT / originalHeight);
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        // Resize image
        Image resultingImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    private String convertToBase64(BufferedImage image, String formatName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, formatName, baos);
        byte[] imageBytes = baos.toByteArray();
        return "data:image/" + formatName + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    private String getFileExtension(String contentType) {
        return contentType.split("/")[1];
    }

} 