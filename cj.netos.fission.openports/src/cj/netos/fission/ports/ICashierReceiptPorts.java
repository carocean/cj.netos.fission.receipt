package cj.netos.fission.ports;

import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

@CjOpenports(usage = "出纳柜台交易指令")
public interface ICashierReceiptPorts extends IOpenportService {

    @CjOpenport(usage = "从出纳柜台提现到零钱")
    void withdraw(ISecuritySession session,
                  @CjOpenportParameter(usage = "提现金额", name = "amount") long amount
    ) throws CircuitException;

    @CjOpenport(usage = "抢包,返回抢单号")
    String snatchEnvelope(ISecuritySession session,
                        @CjOpenportParameter(usage = "被抢对象", name = "person") String person
    ) throws CircuitException;
}
