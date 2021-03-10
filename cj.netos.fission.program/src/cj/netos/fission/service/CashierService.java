package cj.netos.fission.service;

import cj.netos.fission.ICashierService;
import cj.netos.fission.mapper.CashierBalanceMapper;
import cj.netos.fission.mapper.CashierMapper;
import cj.netos.fission.mapper.MfSettingsMapper;
import cj.netos.fission.mapper.WithdrawRecordMapper;
import cj.netos.fission.model.Cashier;
import cj.netos.fission.model.CashierBalance;
import cj.netos.fission.model.MfSettings;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjBridge;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.orm.mybatis.annotation.CjTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

@CjBridge(aspects = "@transaction")
@CjService(name = "cashierService")
public class CashierService implements ICashierService {
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.WithdrawRecordMapper")
    WithdrawRecordMapper withdrawRecordMapper;
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.CashierMapper")
    CashierMapper cashierMapper;
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.CashierBalanceMapper")
    CashierBalanceMapper cashierBalanceMapper;
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.MfSettingsMapper")
    MfSettingsMapper mfSettingsMapper;
    @CjServiceSite
    IServiceSite site;

    @CjTransaction
    @Override
    public void checkWithdrawCondition(String principal, long amount) throws CircuitException {
        int pos=principal.lastIndexOf("@");
        String person =null;
        if (pos <0) {
            person=principal;
        }else{
            person = principal.substring(0, pos);
        }

        String mfSettingsId = site.getProperty("fission.mf.settings.id");
        MfSettings settings = mfSettingsMapper.selectByPrimaryKey(mfSettingsId);
        Cashier cashier = cashierMapper.selectByPrimaryKey(person);
        CashierBalance balance = cashierBalanceMapper.selectByPrimaryKey(person);

        Long stayBalance = null;
        if (cashier != null) {
            stayBalance = cashier.getStayBalance();
        }
        if (stayBalance == null && settings != null) {
            stayBalance = settings.getStayBalance();
        }
        if (stayBalance == null) {
            stayBalance = 0L;
        }
        Long balanceAmount = balance == null ? 0L : balance.getBalance();
        if (amount > balanceAmount - stayBalance) {
            throw new CircuitException("801", String.format("不符合提现条件，您要提取的的金额%s大于可提余额:%s元。",
                    new BigDecimal(amount).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN),
                    new BigDecimal(balanceAmount - stayBalance).divide(new BigDecimal("100.00"), 2, RoundingMode.DOWN)));
        }
    }
}
