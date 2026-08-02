package com.john.project.test.common.lumen;

import cn.hutool.core.util.ObjectUtil;
import com.john.project.model.LumenContextCoreModel;
import com.john.project.test.common.BaseTest.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LumenWithdrawalDehydrateCentralCoinTest extends BaseTest {

    private LumenContextCoreModel lumenContextDehydrateFirst;
    private BigDecimal obtainCcuBalanceFirst;

    @Test
    public void test() {
        var ratioFirst = this.lumenContextDehydrateFirst.getTotalCcuBalance().divide(this.lumenContextDehydrateFirst.getUsdCurrencyBalance(), 6, RoundingMode.FLOOR);
        var obtainJapanCurrencyBalanceFirst = this.lumenContextDehydrateFirst.withdrawal(this.lumenContextDehydrateFirst.getJapan(), obtainCcuBalanceFirst);
        var obtainCcuBalanceSecond = this.lumenContextDehydrateFirst.inject(this.lumenContextDehydrateFirst.getJapan(), obtainJapanCurrencyBalanceFirst);
        var ratioSecond = this.lumenContextDehydrateFirst.getTotalCcuBalance().divide(this.lumenContextDehydrateFirst.getUsdCurrencyBalance(), 6, RoundingMode.FLOOR);
        var obtainUsdCurrencyBalanceSecond = this.lumenContextDehydrateFirst.withdrawal(this.lumenContextDehydrateFirst.getUsd(), obtainCcuBalanceSecond);
        this.lumenContextDehydrateFirst.inject(this.lumenContextDehydrateFirst.getUsd(), obtainUsdCurrencyBalanceSecond);
        var ratioThird = this.lumenContextDehydrateFirst.getTotalCcuBalance().divide(this.lumenContextDehydrateFirst.getUsdCurrencyBalance(), 6, RoundingMode.FLOOR);
        assertTrue(ObjectUtil.equals(new BigDecimal("2.333333"), ratioFirst));
        assertTrue(ObjectUtil.equals(new BigDecimal("2.249999"), ratioSecond));
        assertTrue(ObjectUtil.equals(new BigDecimal("2.199999"), ratioThird));

    }

    @BeforeEach
    public void beforeEach() {
        this.lumenContextDehydrateFirst = new LumenContextCoreModel();
        this.lumenContextDehydrateFirst.injectPair(new BigDecimal(100), new BigDecimal(1000));
        this.obtainCcuBalanceFirst = this.lumenContextDehydrateFirst.inject(this.lumenContextDehydrateFirst.getJapan(), new BigDecimal(500));
    }

}
