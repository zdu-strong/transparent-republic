package com.john.project.test.common.lumen;

import cn.hutool.core.util.ObjectUtil;
import com.john.project.model.LumenContextCoreModel;
import com.john.project.test.common.BaseTest.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LumenExchangeAndInjectTest extends BaseTest {

    private LumenContextCoreModel lumenContext;

    @Test
    public void test() {
        var obtainJapanCurrencyBalance = this.lumenContext.exchange(lumenContext.getUsd(), new BigDecimal(100));
        var result = this.lumenContext.inject(lumenContext.getJapan(), new BigDecimal(100).add(obtainJapanCurrencyBalance));
        var usdCurrencyBalance = this.lumenContext.getUsdCurrencyBalance();
        var japanCurrencyBalance = this.lumenContext.getJapanCurrencyBalance();
        var totalCcuBalance = this.lumenContext.getTotalCcuBalance();
        assertTrue(ObjectUtil.equals(new BigDecimal("133.333332"), result));
        assertTrue(ObjectUtil.equals(new BigDecimal("200"), usdCurrencyBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("200"), japanCurrencyBalance));
        assertTrue(ObjectUtil.equals(new BigDecimal("333.333332"), totalCcuBalance));
    }

    @BeforeEach
    public void beforeEach() {
        this.lumenContext = new LumenContextCoreModel();
        this.lumenContext.injectPair(new BigDecimal(100), new BigDecimal(100));
    }

}
