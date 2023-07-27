package lk.gov.health.phsp.bean;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javax.enterprise.context.SessionScoped;
import javax.imageio.ImageIO;
import javax.inject.Named;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

@Named
@SessionScoped
public class QRCodeController implements Serializable {

    private String qrData;

    public void scanQRCode(byte[] imageData) {
        System.out.println("scanQRCode");
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage bufferedImage = ImageIO.read(bais);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
            Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap);
            qrData = qrCodeResult.getText();
            System.out.println("qrData = " + qrData);
        } catch (IOException | NotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getQrData() {
        return qrData;
    }
}
