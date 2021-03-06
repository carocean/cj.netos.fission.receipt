package cj.netos.fission.service;

import cj.netos.fission.ICashierService;
import cj.netos.fission.mapper.WithdrawRecordMapper;
import cj.netos.fission.model.WithdrawRecordExample;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjBridge;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.CircuitException;
import cj.studio.orm.mybatis.annotation.CjTransaction;
import cj.ultimate.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

@CjBridge(aspects = "@transaction")
@CjService(name = "cashierService")
public class CashierService implements ICashierService {
    @CjServiceRef(refByName = "mybatis.cj.netos.fission.mapper.WithdrawRecordMapper")
    WithdrawRecordMapper withdrawRecordMapper;
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
        Date current = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String dayText = format.format(current);
        String begin = String.format("%s000000000", dayText);
        String end = String.format("%s235959999", dayText);
        WithdrawRecordExample example = new WithdrawRecordExample();
        example.createCriteria().andWithdrawerEqualTo(person).andCtimeBetween(begin, end);
        long count = withdrawRecordMapper.countByExample(example);
        String limitTimers = site.getProperty("withdraw.limit.timers");
        if (StringUtil.isEmpty(limitTimers)) {
            limitTimers = "1";
        }
        if (count > Integer.valueOf(limitTimers)) {
            String tip = String.format("今天已提现%s次，明天再提吧。快去抢更多，以待明天提更多!", count);
//            CJSystem.logging().error(getClass(), tip);
            throw new CircuitException("800", tip);
        }
    }
}
