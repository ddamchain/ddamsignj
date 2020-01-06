package one.ddam;

import lombok.Getter;
import lombok.Setter;

import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;
import org.web3j.utils.Strings;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Transaction {
    @Getter @Setter private String target = "";
    @Getter @Setter private BigInteger value = BigInteger.ZERO;
    @Getter @Setter private BigInteger nonce = BigInteger.ONE;
    @Getter @Setter private BigInteger gasLimit = new BigInteger("3000");
    @Getter @Setter private BigInteger gasPrice = new BigInteger("500");
    @Getter @Setter private Byte type = 0;
    @Getter @Setter private byte[] data = new byte[0];

    private static byte[] encode(byte[] bytesValue) {
        byte[] result = new byte[bytesValue.length + 4];
        byte[] length = ByteBuffer.allocate(4).putInt(bytesValue.length).order(ByteOrder.BIG_ENDIAN).array();

        System.arraycopy(length, 0, result, 0, length.length);
        if (bytesValue.length > 0) {
            System.arraycopy(bytesValue, 0, result, 4, bytesValue.length);
        }

        return result;
    }

    private static byte[] concat(byte[] b1, byte[] b2) {
        byte[] result = Arrays.copyOf(b1, b1.length + b2.length);
        System.arraycopy(b2, 0, result, b1.length, b2.length);
        return result;
    }

    public byte[] genHash() {
        byte[] result = new byte[0];
        BigInteger to = new BigInteger(target.substring(2), 16);

        result = concat(result, encode(Numeric.toBytesPadded(to, 32)));
        result = concat(result, encode(value.toByteArray()));
        result = concat(result, encode(nonce.toByteArray()));
        result = concat(result, encode(gasLimit.toByteArray()));
        result = concat(result, encode(gasPrice.toByteArray()));
        result = concat(result, new byte[]{type});
        result = concat(result, encode(data));

        return Hash.sha256(result);
    }
}
