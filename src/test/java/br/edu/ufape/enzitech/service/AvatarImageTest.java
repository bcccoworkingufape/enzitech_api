package br.edu.ufape.enzitech.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarImageTest {

    @Test
    void detectsPngDimensions() {
        AvatarImage image = AvatarImage.inspect(png(320, 200));

        assertThat(image).isEqualTo(new AvatarImage("image/png", 320, 200));
    }

    @Test
    void detectsJpegDimensions() {
        AvatarImage image = AvatarImage.inspect(encode("jpg", 256, 128));

        assertThat(image).isEqualTo(new AvatarImage("image/jpeg", 256, 128));
    }

    @Test
    void detectsLossyWebpDimensions() {
        byte[] webp = webp("VP8 ", 30);
        webp[23] = (byte) 0x9D;
        webp[24] = 0x01;
        webp[25] = 0x2A;
        webp[26] = 0x00; // largura 256 (LE)
        webp[27] = 0x01;
        webp[28] = (byte) 0x80; // altura 128 (LE)
        webp[29] = 0x00;

        assertThat(AvatarImage.inspect(webp)).isEqualTo(new AvatarImage("image/webp", 256, 128));
    }

    @Test
    void detectsLosslessWebpDimensions() {
        byte[] webp = webp("VP8L", 25);
        webp[20] = 0x2F;
        int bits = (256 - 1) | ((100 - 1) << 14);
        webp[21] = (byte) bits;
        webp[22] = (byte) (bits >>> 8);
        webp[23] = (byte) (bits >>> 16);
        webp[24] = (byte) (bits >>> 24);

        assertThat(AvatarImage.inspect(webp)).isEqualTo(new AvatarImage("image/webp", 256, 100));
    }

    @Test
    void detectsExtendedWebpDimensions() {
        byte[] webp = webp("VP8X", 30);
        webp[24] = (byte) 255; // largura - 1 = 255 (LE, 24 bits)
        webp[27] = (byte) 63;  // altura - 1 = 63

        assertThat(AvatarImage.inspect(webp)).isEqualTo(new AvatarImage("image/webp", 256, 64));
    }

    @Test
    void rejectsImagesLargerThanAllowedDimension() {
        assertThatThrownBy(() -> AvatarImage.inspect(png(AvatarImage.MAX_DIMENSION + 1, 10)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("pixels");
    }

    @Test
    void rejectsFilesAboveSizeLimit() {
        byte[] big = Arrays.copyOf(png(10, 10), AvatarImage.MAX_BYTES + 1);

        assertThatThrownBy(() -> AvatarImage.inspect(big))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("KB");
    }

    @Test
    void rejectsUnknownOrTruncatedContent() {
        assertThatThrownBy(() -> AvatarImage.inspect("<svg onload=alert(1)>".getBytes()))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> AvatarImage.inspect(Arrays.copyOf(png(10, 10), 14)))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> AvatarImage.inspect(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00}))
                .isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> AvatarImage.inspect(new byte[0]))
                .isInstanceOf(ResponseStatusException.class);
    }

    static byte[] png(int width, int height) {
        return encode("png", width, height);
    }

    private static byte[] encode(String format, int width, int height) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] webp(String chunk, int length) {
        byte[] d = new byte[length];
        System.arraycopy("RIFF".getBytes(), 0, d, 0, 4);
        System.arraycopy("WEBP".getBytes(), 0, d, 8, 4);
        System.arraycopy(chunk.getBytes(), 0, d, 12, 4);
        return d;
    }
}
