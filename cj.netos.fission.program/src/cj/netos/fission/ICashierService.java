package cj.netos.fission;

import cj.studio.ecm.net.CircuitException;

public interface ICashierService {
    void checkWithdrawCondition(String principal, long amount) throws CircuitException;

}
