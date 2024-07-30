package com.jiangjing.im.app.bussiness.config;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import com.alibaba.dashscope.aigc.generation.Generation;

public class PooledDashScopeObjectFactory extends BasePooledObjectFactory<Generation> {

    @Override
    public Generation create() throws Exception {
        return new Generation();
    }

    @Override
    public PooledObject<Generation> wrap(Generation obj) {
        return new DefaultPooledObject<>(obj);
    }
}
