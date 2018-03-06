package com.huofu.module.i5wei.fixed;

import com.huofu.module.i5wei.promotion.dao.StorePromotionRebateDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionReduceDAO;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;
import huofucore.facade.i5wei.fixed.I5weiFixedFacade;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@ThriftServlet(name = "i5weiFixedFacadeServlet", serviceClass = I5weiFixedFacade.class)
public class I5weiFixedFacadeImpl implements I5weiFixedFacade.Iface {

    @Resource
    private StorePromotionRebateDAO storePromotionRebateDAO;

    @Resource
    private StorePromotionReduceDAO storePromotionReduceDAO;

    @Override
    public void fixedPromotionRebateAndReduce() throws TException {
        List<StorePromotionRebate> rebateList = this.storePromotionRebateDAO.getAll();
        List<StorePromotionReduce> reduceList = this.storePromotionReduceDAO.getAll();

        for (StorePromotionRebate rebate : rebateList) {
            rebate.setPreOrderSupport(true);
            rebate.update();
        }

        for (StorePromotionReduce reduce : reduceList) {
            reduce.setPreOrderSupport(true);
            reduce.update();
        }
    }
}