package one.ddam;

import java.math.BigInteger;
import java.security.SignatureException;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.crypto.digests.SHA3Digest;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Signer {
    private static final Logger log = LoggerFactory.getLogger(Signer.class);

    static final int PRIVATE_KEY_SIZE = 32;
    static final int SIGNATURE_SIZE = 65;

    private static byte[] sha3Sum256Digest(byte[] input) {
        SHA3Digest sha3 = new SHA3Digest();
        byte[] out = new byte[PRIVATE_KEY_SIZE];

        sha3.update(input, 0, input.length);
        sha3.doFinal(out, 0);

        return out;
    }

    public static byte[] skToPk(String sk) {
        BigInteger privateKey = new BigInteger(Numeric.cleanHexPrefix(sk), 16);
        ECPoint point = Sign.publicPointFromPrivate(privateKey);

        byte[] x = Bytes.trimLeadingZeroes(point.normalize().getXCoord().getEncoded());
        byte[] y = Bytes.trimLeadingZeroes(point.normalize().getYCoord().getEncoded());

        byte[] result = new byte[x.length + y.length];
        System.arraycopy(x, 0, result, 0, x.length);
        System.arraycopy(y, 0, result, x.length, y.length);

        return result;
    }

    public static ECKeyPair skToKeyPair(String sk) {
        BigInteger privateKey   = new BigInteger(Numeric.cleanHexPrefix(sk), 16);
        BigInteger publicKey    = Sign.publicKeyFromPrivate(privateKey);

        return new ECKeyPair(privateKey, publicKey);
    }

    public static ECKeyPair genKeyPair() throws Exception {
        return Keys.createEcKeyPair();
    }

    public static String getAddress(String sk) {
        byte[] input = skToPk(sk);
        return getAddress(input);
    }

    public static String getAddress(byte[] input) {
        byte[] address = sha3Sum256Digest(input);
        return "DD" + Hex.toHexString(address);
    }

    public static String getAddress(ECKeyPair kp) {
        return getAddress(kp.getPrivateKey().toString(16));
    }

    public static String exportKey(ECKeyPair kp) {
        return Numeric.toHexStringWithPrefixZeroPadded(kp.getPrivateKey(), PRIVATE_KEY_SIZE * 2);
    }

    public static String sign(ECKeyPair keyPair, Transaction tx) {
        byte[] result = new byte[SIGNATURE_SIZE];

        byte[] txHash = tx.genHash();
        log.debug("tx hash: {}", Hex.toHexString(txHash));

        Sign.SignatureData sig = Sign.signMessage(txHash, keyPair, false);

        byte[] r = sig.getR();
        byte[] s = sig.getS();
        byte[] v = new byte[]{(byte) (new BigInteger(sig.getV()).intValue() - 27)};

        System.arraycopy(r, 0, result, 0, r.length);
        System.arraycopy(s, 0, result, r.length, s.length);
        System.arraycopy(v, 0, result, r.length + s.length, v.length);

        return Numeric.toHexString(result);
    }

    public static Sign.SignatureData sign(ECKeyPair keyPair, byte[] txHash) {
        return Sign.signMessage(txHash, keyPair, false);
    }

    public static Sign.SignatureData hexToSign(String hex) {
        byte[] bytes = Hex.decode(Numeric.cleanHexPrefix(hex));

        byte[] r = new byte[32];
        byte[] s = new byte[32];
        byte[] v = new byte[1];

        System.arraycopy(bytes,  0, r, 0, r.length);
        System.arraycopy(bytes, 32, s, 0, s.length);
        System.arraycopy(bytes, 64, v, 0, v.length);

        byte realV = (byte) (new BigInteger(v).intValue() + 27);
        return new Sign.SignatureData(new byte[]{ realV }, r, s);
    }

    public static String getSource(byte[] messageHash, Sign.SignatureData signatureData) throws SignatureException {
        BigInteger bi = Sign.signedMessageHashToKey(messageHash, signatureData);
        byte[] pk = Numeric.toBytesPadded(bi, 64);

        byte[] a = new byte[32];
        byte[] b = new byte[32];

        System.arraycopy(pk, pk.length - 32, b, 0, b.length);
        System.arraycopy(pk, pk.length - 64, a, 0, a.length);

        byte[] x = Bytes.trimLeadingZeroes(a);
        byte[] y = Bytes.trimLeadingZeroes(b);

        byte[] input = java.util.Arrays.copyOf(x, x.length + y.length);
        System.arraycopy(y, 0, input, x.length, y.length);
        return getAddress(input);
    }
}
