package one.ddam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    OkHttpClient rpc = new OkHttpClient();

    // Testnet RPC node
    String host = "http://47.56.171.245";
    String port = "8101";

    String post(String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(host + ":" + port)
                .post(body)
                .build();

        try (Response response = rpc.newCall(request).execute()) {
            return Objects.requireNonNull(response.body()).string();
        }
    }

    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();
        ObjectMapper objectMapper = new ObjectMapper();

        // 校验地址
        log.info("DDcec506e893f59e1e9a0f82fd20f8e54f47b5fc826800fdca93df217fe21f7fb6 is valid: {}", Utils.isAddress("DDcec506e893f59e1e9a0f82fd20f8e54f47b5fc826800fdca93df217fe21f7fb6"));
        log.info("DDfb6916095ca1df60bb79ce92ce3ea74c37c5d359 is valid: {}", Utils.isAddress("DDfb6916095ca1df60bb79ce92ce3ea74c37c5d359"));
        log.info("0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359 is valid: {}", Utils.isAddress("0xfB6916095ca1df60bB79Ce92cE3Ea74c37c5d359"));

        // 生成目标账户
        ECKeyPair keyPair = Signer.genKeyPair();
        String target = Signer.getAddress(keyPair);

        log.info("[转账目标账户] privateKey: {}, address: {}", Signer.exportKey(keyPair), target);

        // 签名账户
        ECKeyPair src = Signer.skToKeyPair("0xf241759f4e85b2188314c4636e88b8b8076606b53ebe9817bbe1b2379b930ca6");

        // 查询签名账户Nonce
        String req = "{"
                + "\"method\": \"Gx_nonce\","
                + "\"jsonrpc\": \"2.0\","
                + "\"id\": 1,"
                + "\"params\": [\""
                    + Signer.getAddress(src)
                + "\"]"
                + "}";

        JsonNode res = objectMapper.readTree(demo.post(req));
        int nonce = res.get("result").findValue("data").asInt();

        // 待签名交易
        Transaction tx = new Transaction();
        tx.setTarget(target);
        tx.setValue(new BigInteger("1000000000"));
        tx.setGasLimit(BigInteger.valueOf(3000));
        tx.setGasPrice(BigInteger.valueOf(500));
        tx.setNonce(BigInteger.valueOf(nonce));
        tx.setType((byte) 0);
        tx.setSign(Signer.sign(src, tx.genHash()));

        // 生成签名
        String sign = Signer.sign(src, tx);
        log.info("transaction:\ntarget: {}\nvalue: {}\nnonce: {}\ntype: {}\ndata: {}\nsign: {}",
                tx.getTarget(),
                tx.getValue(),
                tx.getNonce(),
                tx.getType(),
                tx.getData(),
                sign);

        // 反序列化签名
        Sign.SignatureData signData = Signer.hexToSign(sign);
        log.info("hex to Sign.SignatureData: {}", signData.equals(tx.getSign()));

        // 恢复公钥
        log.info("recover address: get={}, want={}", Signer.getSource(tx.genHash(), signData), Signer.getAddress(src));

        // 序列化交易
        String sendTx = Transaction.serializeTx(tx, sign);

        // 反序列化交易
        Transaction rawTx = Transaction.unSerializeTx(sendTx);
        log.info("Unserialize transaction:\ntarget: {}\nvalue: {}\nnonce: {}\ntype: {}\ndata: {}\nsource: {}",
                rawTx.getTarget(),
                rawTx.getValue(),
                rawTx.getNonce(),
                rawTx.getType(),
                rawTx.getData(),
                Signer.getSource(rawTx.genHash(), rawTx.getSign()));

        // 发送交易
        String jsonrpc = "{"
                + "\"method\": \"Gx_tx\","
                + "\"jsonrpc\": \"2.0\","
                + "\"id\": 1,"
                + "\"params\": [" + sendTx + "]"
                + "}";

        String result = demo.post(jsonrpc);
        JsonNode info = objectMapper.readTree(result);

        if (info.get("result").isObject() && !info.get("result").findValue("data").isNull()) {
            log.info("tx hash: {}", info.get("result").findValue("data").asText());
        }
        else {
            log.error("tx result: {}", result);
        }
    }
}
