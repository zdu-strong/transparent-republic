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
        var usdCcuBalance = this.lumenContext.getUsdCcu();
        var japanCcuBalance = this.lumenContext.getJapanCcu();
        assertTrue(ObjectUtil.equals(new BigDecimal("266.666666"), result));
        assertTrue(ObjectUtil.equals(new BigDecimal("299.999999"), usdCcuBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("299.999999"), japanCcuBalance));
    }

    @BeforeEach
    public void beforeEach() {
        this.lumenContext = new LumenContextCoreModel();
        this.lumenContext.injectPair(new BigDecimal(100), new BigDecimal(200));
        var usdCcuBalance = this.lumenContext.getUsdCcu();
        var japanCcuBalance = this.lumenContext.getJapanCcu();
        assertTrue(ObjectUtil.equals(new BigDecimal("166.666666"), usdCcuBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("166.666666"), japanCcuBalance));
    }

}
