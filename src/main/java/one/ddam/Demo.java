package one.ddam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.ECKeyPair;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
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

    public static void main(String[] args) throws IOException {
        Demo demo = new Demo();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
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
            tx.setData("offline signer");

            // 生成签名
            String sign = Signer.sign(src, tx);
            log.info("transaction:\ntarget: {}\nvalue: {}\nnonce: {}\ntype: {}\ndata: {}\nsign: {}",
                    tx.getTarget(),
                    tx.getValue(),
                    tx.getNonce(),
                    tx.getType(),
                    tx.getData(),
                    sign);

            // 发送交易
            String sendTx = "{"
                    + "\"method\": \"Gx_tx\","
                    + "\"jsonrpc\": \"2.0\","
                    + "\"id\": 1,"
                    + "\"params\": [\"{"
                        + "\\\"target\\\": \\\"" + target + "\\\","
                        + "\\\"value\\\": " + tx.getValue().longValue() + ","
                        + "\\\"nonce\\\": " + tx.getNonce().longValue() + ","
                        + "\\\"gas\\\":" + tx.getGasLimit().longValue() + ","
                        + "\\\"gasprice\\\":" + tx.getGasPrice().longValue() + ","
                        + "\\\"tx_type\\\":" + tx.getType().intValue() + ","
                        + "\\\"data\\\": " + Arrays.toString(tx.getData().getBytes()) + ","
                        + "\\\"sign\\\": \\\"" + sign + "\\\""
                    + "}\"]"
                    + "}";

            String result = demo.post(sendTx);
            JsonNode info = objectMapper.readTree(result);

            if (info.get("result").isObject()) {
                log.info("tx hash: {}", info.get("result").findValue("data").asText());
            }
            else {
                log.error("send tx error: {}", result);
            }

        } catch (Exception ex) {
            log.error("Exception", ex);
        }
    }
}
