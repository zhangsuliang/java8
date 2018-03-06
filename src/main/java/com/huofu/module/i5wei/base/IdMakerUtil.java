package com.huofu.module.i5wei.base;

import com.google.common.collect.Lists;
import huofucore.facade.idmaker.IdMakerFacade;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftInvoker;
import huofuhelper.util.thrift.ThriftParam;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by akwei on 3/9/15.
 */
@Component
public class IdMakerUtil {

    @ThriftClient
    private IdMakerFacade.Iface idMakerFacadeIface;

    public long nextId(String key) {
        try {
            return this.idMakerFacadeIface.getNextId(key);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> nextIds(String key, int size) {
        try {
            return this.idMakerFacadeIface.getNextIds(key, size);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public long nextId2(String key) {
        try {
            return this.idMakerFacadeIface.getNextId2(key);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> nextIds2(String key, int size) {
        try {
            return this.idMakerFacadeIface.getNextIds2(key, size);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> nextIdsV2(String key, int size) {
        ThriftParam thriftParam = new ThriftParam();
        thriftParam.setSoTimeout(15000);
        try {
            List<Long> idList = Lists.newArrayList();
            int fixedSize = 500;
            if (size > fixedSize) {
                int sizea = size / fixedSize;
                int sizeb = size % fixedSize;
                for (int i = 0; i < sizea; i++) {
                    ThriftInvoker.invoke(thriftParam, () -> {
                        idList.addAll(this.nextIds(key, fixedSize));
                        return null;
                    });
                }
                ThriftInvoker.invoke(thriftParam, () -> {
                    idList.addAll(this.nextIds(key, sizeb));
                    return null;
                });
            } else {
                ThriftInvoker.invoke(thriftParam, () -> {
                    idList.addAll(this.nextIds(key, size));
                    return null;
                });
            }
            return idList;
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> nextIds2V2(String key, int size) {
        ThriftParam thriftParam = new ThriftParam();
        thriftParam.setSoTimeout(15000);
        try {
            List<Long> idList = Lists.newArrayList();
            int fixedSize = 500;
            if (size > fixedSize) {
                int sizea = size / fixedSize;
                int sizeb = size % fixedSize;
                for (int i = 0; i < sizea; i++) {
                    ThriftInvoker.invoke(thriftParam, () -> {
                        idList.addAll(this.nextIds2(key, fixedSize));
                        return null;
                    });
                }
                ThriftInvoker.invoke(thriftParam, () -> {
                    idList.addAll(this.nextIds2(key, sizeb));
                    return null;
                });
            } else {
                ThriftInvoker.invoke(thriftParam, () -> {
                    idList.addAll(this.nextIds2(key, size));
                    return null;
                });
            }
            return idList;
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }
}
