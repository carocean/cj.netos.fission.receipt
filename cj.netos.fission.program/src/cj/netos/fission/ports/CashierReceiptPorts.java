package cj.netos.fission.ports;

import cj.netos.fission.ICashierService;
import cj.netos.rabbitmq.IRabbitMQProducer;
import cj.studio.ecm.CJSystem;
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
    @CjServiceRef(refByName = "@.rabbitmq.producer.fission-account")
    IRabbitMQProducer rabbitMQProducerAccount;
    @CjServiceRef
    ICashierService cashierService;
    @Override
    public void withdraw(ISecuritySession session, long amount) throws CircuitException {
//        throw new CircuitException("800",String.format("最近由于系统升级，暂时停止提现服务。暂定于本月22号开通服务，当听到手机地微app有他人抢红包的提示音后，表示该服务已开通。"));
//        CJSystem.logging().info(getClass(),String.format("%s %s欲提现%s分",session.property("nickName"),session.principal(),amount));
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

    @Override
    public void cashierOutBusiness(ISecuritySession session, long amount) throws CircuitException {
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .type("/cashier/account.mhub")
                .headers(new HashMap<String, Object>() {{
                    put("command", "cashierOutBusiness");
                    put("person", session.principal());
                    put("nick-name", session.property("nickName"));
                    put("amount",amount);
                }})
                .build();
        rabbitMQProducerAccount.publish("fission.mf", properties, new byte[0]);
    }

    @Override
    public void cashierOutIncome(ISecuritySession session, long amount) throws CircuitException {
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder()
                .type("/cashier/account.mhub")
                .headers(new HashMap<String, Object>() {{
                    put("command", "cashierOutIncome");
                    put("person", session.principal());
                    put("nick-name", session.property("nickName"));
                    put("amount",amount);
                }})
                .build();
        rabbitMQProducerAccount.publish("fission.mf", properties, new byte[0]);
    }
}
