package cj.netos.fission.ports;

import cj.netos.fission.ICashierService;
import cj.netos.rabbitmq.IRabbitMQProducer;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.util.Encript;
import com.rabbitmq.client.AMQP;

import java.util.HashMap;
import java.util.UUID;

@CjService(name = "/cashier.ports")
public class CashierReceiptPorts implements ICashierReceiptPorts {
    @CjServiceRef(refByName = "@.rabbitmq.producer.fission-mf")
    IRabbitMQProducer rabbitMQProducer;
    @CjServiceRef
    ICashierService cashierService;
    @Override
    public void withdraw(ISecuritySession session, long amount) throws CircuitException {
        cashierService.checkWithdrawCondition(session.principal(),amount);
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .type("/cashier/receipt.mhub")
                .headers(new HashMap<String, Object>() {{
                    put("command", "withdraw");
                    put("person", session.principal());
                    put("nick-name", session.property("nickName"));
                    put("amount",amount);
                }})
                .build();
        rabbitMQProducer.publish("fission.mf", properties, new byte[0]);
    }

    @Override
    public String snatchEnvelope(ISecuritySession session, String person) throws CircuitException {
        String sn= Encript.md5(UUID.randomUUID().toString());
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .type("/cashier/receipt.mhub")
                .headers(new HashMap<String, Object>() {{
                    put("command", "snatchEnvelope");
                    put("payee", session.principal());
                    put("payee-name", session.property("nickName"));
                    put("payer", person);
                    put("record_sn",sn);
                }})
                .build();
        rabbitMQProducer.publish("fission.mf", properties, new byte[0]);
        return sn;
    }
}
