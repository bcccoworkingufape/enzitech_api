package br.edu.ufape.enzitech.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Identifica o formato real de uma foto de perfil pelos bytes iniciais (ignorando o
 * Content-Type enviado pelo cliente) e lê as dimensões direto do cabeçalho do arquivo,
 * sem decodificar a imagem, então a validação custa praticamente nada de CPU/memória.
 */
public record AvatarImage(String contentType, int width, int height) {

    static final int MAX_BYTES = 256 * 1024;
    static final int MAX_DIMENSION = 1024;

    private static final String INVALID_IMAGE = "Imagem inválida. Envie uma foto JPEG, PNG ou WebP.";

    public static AvatarImage inspect(byte[] data) {
        if (data == null || data.length == 0) {
            throw badRequest(INVALID_IMAGE);
        }
        if (data.length > MAX_BYTES) {
            throw badRequest("A foto deve ter no máximo " + (MAX_BYTES / 1024) + " KB.");
        }

        AvatarImage image;
        try {
            image = parse(data);
        } catch (IndexOutOfBoundsException e) {
            image = null;
        }

        if (image == null || image.width() <= 0 || image.height() <= 0) {
            throw badRequest(INVALID_IMAGE);
        }
        if (image.width() > MAX_DIMENSION || image.height() > MAX_DIMENSION) {
            throw badRequest("A foto deve ter no máximo " + MAX_DIMENSION + "x" + MAX_DIMENSION + " pixels.");
        }
        return image;
    }

    private static AvatarImage parse(byte[] d) {
        if (startsWith(d, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A) && startsWith(d, 12, 'I', 'H', 'D', 'R')) {
            return new AvatarImage("image/png", int32be(d, 16), int32be(d, 20));
        }
        if (startsWith(d, 0, 0xFF, 0xD8, 0xFF)) {
            return parseJpeg(d);
        }
        if (startsWith(d, 0, 'R', 'I', 'F', 'F') && startsWith(d, 8, 'W', 'E', 'B', 'P')) {
            return parseWebp(d);
        }
        return null;
    }

    private static AvatarImage parseJpeg(byte[] d) {
        int i = 2;
        while (i + 3 < d.length) {
            if ((d[i] & 0xFF) != 0xFF) return null;
            int marker = d[i + 1] & 0xFF;
            if (marker == 0xFF) {
                i++;
                continue;
            }
            if (marker == 0x01 || (marker >= 0xD0 && marker <= 0xD8)) {
                i += 2;
                continue;
            }
            boolean startOfFrame = marker >= 0xC0 && marker <= 0xCF
                    && marker != 0xC4 && marker != 0xC8 && marker != 0xCC;
            if (startOfFrame) {
                return new AvatarImage("image/jpeg", int16be(d, i + 7), int16be(d, i + 5));
            }
            i += 2 + int16be(d, i + 2);
        }
        return null;
    }

    private static AvatarImage parseWebp(byte[] d) {
        if (startsWith(d, 12, 'V', 'P', '8', 'X')) {
            return new AvatarImage("image/webp", int24le(d, 24) + 1, int24le(d, 27) + 1);
        }
        if (startsWith(d, 12, 'V', 'P', '8', 'L') && (d[20] & 0xFF) == 0x2F) {
            int bits = int32le(d, 21);
            return new AvatarImage("image/webp", (bits & 0x3FFF) + 1, ((bits >>> 14) & 0x3FFF) + 1);
        }
        if (startsWith(d, 12, 'V', 'P', '8', ' ') && startsWith(d, 23, 0x9D, 0x01, 0x2A)) {
            return new AvatarImage("image/webp", int16le(d, 26) & 0x3FFF, int16le(d, 28) & 0x3FFF);
        }
        return null;
    }

    private static boolean startsWith(byte[] d, int offset, int... expected) {
        if (d.length < offset + expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if ((d[offset + i] & 0xFF) != expected[i]) return false;
        }
        return true;
    }

    private static int int16be(byte[] d, int i) {
        return ((d[i] & 0xFF) << 8) | (d[i + 1] & 0xFF);
    }

    private static int int32be(byte[] d, int i) {
        return (int16be(d, i) << 16) | int16be(d, i + 2);
    }

    private static int int16le(byte[] d, int i) {
        return (d[i] & 0xFF) | ((d[i + 1] & 0xFF) << 8);
    }

    private static int int24le(byte[] d, int i) {
        return int16le(d, i) | ((d[i + 2] & 0xFF) << 16);
    }

    private static int int32le(byte[] d, int i) {
        return int24le(d, i) | ((d[i + 3] & 0xFF) << 24);
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
