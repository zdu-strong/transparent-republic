package com.john.project.test.common.lumen;

import cn.hutool.core.util.ObjectUtil;
import com.john.project.model.LumenContextCoreModel;
import com.john.project.test.common.BaseTest.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LumenWithdrawalRehydrateCentralCoinTest extends BaseTest {

    private LumenContextCoreModel lumenContextRehydrateFirst;

    @Test
    public void test() {
        var ratioFirst = this.lumenContextRehydrateFirst.getTotalCcuBalance().divide(this.lumenContextRehydrateFirst.getUsdCurrencyBalance(), 6, RoundingMode.FLOOR);
        var obtainJapanCurrencyBalanceFirst = this.lumenContextRehydrateFirst.withdrawal(this.lumenContextRehydrateFirst.getJapan(), new BigDecimal(100));
        var obtainCcuBalanceSecond = this.lumenContextRehydrateFirst.inject(this.lumenContextRehydrateFirst.getJapan(), obtainJapanCurrencyBalanceFirst);
        var ratioSecond = this.lumenContextRehydrateFirst.getTotalCcuBalance().divide(this.lumenContextRehydrateFirst.getUsdCurrencyBalance(), 6, RoundingMode.FLOOR);
        this.lumenContextRehydrateFirst.withdrawal(this.lumenContextRehydrateFirst.getUsd(), obtainCcuBalanceSecond);
        var ratioThird = this.lumenContextRehydrateFirst.getTotalCcuBalance().divide(this.lumenContextRehydrateFirst.getUsdCurrencyBalance(), 6, RoundingMode.FLOOR);
        assertTrue(ObjectUtil.equals(new BigDecimal("1.900000"), ratioFirst));
        assertTrue(ObjectUtil.equals(new BigDecimal("1.911111"), ratioSecond));
        assertTrue(ObjectUtil.equals(new BigDecimal("2.022222"), ratioThird));
    }

    @BeforeEach
    public void beforeEach() {
        this.lumenContextRehydrateFirst = new LumenContextCoreModel();
        this.lumenContextRehydrateFirst.injectPair(new BigDecimal(1000), new BigDecimal(5000));
        this.lumenContextRehydrateFirst.withdrawal(this.lumenContextRehydrateFirst.getJapan(), new BigDecimal(100));
    }

}

