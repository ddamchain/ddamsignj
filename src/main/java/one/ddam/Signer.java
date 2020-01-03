package one.ddam;

import java.math.BigInteger;

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
}
