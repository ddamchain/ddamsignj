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
        ECKeyPair kp1 = Signer.skToKeyPair("0x97ddae0f3a25b92268175400149d65d6887b9cefaf28ea2c078e05cdc15a3c0a");
        ECKeyPair kp2 = Signer.skToKeyPair("0x39bde506f23bffbab83e4b7187f6f90ee1fe5d990b80af636633febc1463f85d");
        ECKeyPair kp3 = Signer.skToKeyPair("0x6c16b3d2eff3932cb9ffdb4a750ff040afa5a2a031d61538bb503e9f2c3dc82d");
        ECKeyPair kp4 = Signer.skToKeyPair("0x91b2db997e91ebc64f45fb70df9b09cd808aac520934b1a12dc1b52241745f5b");

        log.info("PrivateKey1: {}, Address1: {}, Sign1: {}", Signer.exportKey(kp1), Signer.getAddress(kp1), Signer.sign(kp1, tx));
        log.info("PrivateKey2: {}, Address2: {}, Sign2: {}", Signer.exportKey(kp2), Signer.getAddress(kp2), Signer.sign(kp2, tx));
        log.info("PrivateKey3: {}, Address3: {}, Sign3: {}", Signer.exportKey(kp3), Signer.getAddress(kp3), Signer.sign(kp3, tx));
        log.info("PrivateKey4: {}, Address4: {}, Sign4: {}", Signer.exportKey(kp4), Signer.getAddress(kp4), Signer.sign(kp4, tx));
    }
}
