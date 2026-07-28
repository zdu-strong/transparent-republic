package com.john.project.test.common.lumen;

import cn.hutool.core.util.ObjectUtil;
import com.john.project.model.LumenContextCoreModel;
import com.john.project.test.common.BaseTest.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LumenInjectPairDifferentRateTest extends BaseTest {

    private LumenContextCoreModel lumenContext;

    @Test
    public void test() {
        var result = this.lumenContext.injectPair(new BigDecimal(200), new BigDecimal(100));
        var usdCcuBalance = this.lumenContext.getUsdCcuBalance();
        var japanCcuBalance = this.lumenContext.getJapanCcuBalance();
        assertTrue(ObjectUtil.equals(new BigDecimal("300.000000"), result));
        assertTrue(ObjectUtil.equals(new BigDecimal("300.000000"), usdCcuBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("400.000000"), japanCcuBalance));
    }

    @BeforeEach
    public void beforeEach() {
        this.lumenContext = new LumenContextCoreModel();
        this.lumenContext.injectPair(new BigDecimal(100), new BigDecimal(200));
        var usdCcuBalance = this.lumenContext.getUsdCcuBalance();
        var japanCcuBalance = this.lumenContext.getJapanCcuBalance();
        assertTrue(ObjectUtil.equals(new BigDecimal("200"), usdCcuBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("200"), japanCcuBalance));
    }

}
