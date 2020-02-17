package one.ddam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

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
    @Getter @Setter private byte[] data = null;
    @Getter @Setter private Sign.SignatureData sign = null;

    public static String serializeTx(Transaction tx, String sign) {
        return "\"{"
                    + "\\\"target\\\": \\\"" + tx.getTarget() + "\\\","
                    + "\\\"value\\\": " + tx.getValue().longValue() + ","
                    + "\\\"nonce\\\": " + tx.getNonce().longValue() + ","
                    + "\\\"gas\\\":" + tx.getGasLimit().longValue() + ","
                    + "\\\"gasprice\\\":" + tx.getGasPrice().longValue() + ","
                    + "\\\"tx_type\\\":" + tx.getType().intValue() + ","
                    + "\\\"data\\\": " + Arrays.toString(tx.getData()) + ","
                    + "\\\"sign\\\": \\\"" + sign + "\\\""
                + "}\"";
    }

    public static Transaction unSerializeTx(String info) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Transaction tx = new Transaction();

        JsonNode json = objectMapper.readTree(info);
        json = objectMapper.readTree(json.asText());

        String target = json.findValue("target").asText();
        BigInteger value = BigInteger.valueOf(json.findValue("value").asLong());
        BigInteger nonce = BigInteger.valueOf(json.findValue("nonce").asLong());
        BigInteger gasLimit = BigInteger.valueOf(json.findValue("gas").asLong());
        BigInteger gasPrice = BigInteger.valueOf(json.findValue("gasprice").asLong());
        byte type = (byte) json.findValue("tx_type").asInt();
        String sign = json.findValue("sign").asText();

        tx.setTarget(target);
        tx.setValue(value);
        tx.setNonce(nonce);
        tx.setGasLimit(gasLimit);
        tx.setGasPrice(gasPrice);
        tx.setType(type);
        tx.setSign(Signer.hexToSign(sign));

        return tx;
    }

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
        result = concat(result, encode(Bytes.trimLeadingZeroes(value.toByteArray())));
        result = concat(result, encode(Bytes.trimLeadingZeroes(nonce.toByteArray())));
        result = concat(result, encode(Bytes.trimLeadingZeroes(gasLimit.toByteArray())));
        result = concat(result, encode(Bytes.trimLeadingZeroes(gasPrice.toByteArray())));
        result = concat(result, new byte[]{type});
        if (data != null) {
            result = concat(result, encode(data));
        }
        else {
            result = concat(result, encode(new byte[0]));
        }

        return Hash.sha256(result);
    }
}
