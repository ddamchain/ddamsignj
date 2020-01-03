package one.ddam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.ECKeyPair;

import java.math.BigInteger;

public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) {
        // 生成账户
        try {
            ECKeyPair random = Signer.genKeyPair();
            log.info("Random privateKey: {}", Signer.exportKey(random));
            log.info("Random address: {}", Signer.getAddress(random));
        } catch (Exception ex) {
            log.error("GenKeyPair Exception", ex);
        }

        // 待签名交易
        Transaction tx = new Transaction();
        tx.setTarget("DD108b77529ec1de053b77898c27bcaa213d294638eea7a1990ef18a8516c8b27a");
        tx.setValue(new BigInteger("10000000000"));

        // 账户密钥对
        ECKeyPair kp1 = Signer.skToKeyPair("0x01");
        ECKeyPair kp2 = Signer.skToKeyPair("0x02");
        ECKeyPair kp3 = Signer.skToKeyPair("0x03");
        ECKeyPair kp4 = Signer.skToKeyPair("0x04");

        log.info("PrivateKey1: {}, Address1: {}, Sign1: {}", Signer.exportKey(kp1), Signer.getAddress(kp1), Signer.sign(kp1, tx));
        log.info("PrivateKey2: {}, Address2: {}, Sign2: {}", Signer.exportKey(kp2), Signer.getAddress(kp2), Signer.sign(kp2, tx));
        log.info("PrivateKey3: {}, Address3: {}, Sign3: {}", Signer.exportKey(kp3), Signer.getAddress(kp3), Signer.sign(kp3, tx));
        log.info("PrivateKey4: {}, Address4: {}, Sign4: {}", Signer.exportKey(kp4), Signer.getAddress(kp4), Signer.sign(kp4, tx));
    }
}
