package com.john.project.model;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.john.project.common.uuid.UUIDUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.jinq.orm.stream.JinqStream;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Accessors(chain = true)
public class LumenContextCoreModel {

    private List<LumenCcuBalanceModel> ccuBalanceList;

    private LumenCurrencyModel usd;
    private LumenCurrencyModel japan;

    public BigDecimal injectPair(BigDecimal sourceUsdCurrencyBalance, BigDecimal sourceJapanCurrencyBalance) {
        checkSourceCurrencyBalanceGreaterOrEqualZero(sourceUsdCurrencyBalance);
        checkSourceCurrencyBalanceGreaterOrEqualZero(sourceJapanCurrencyBalance);

        if (NumberUtil.equals(sourceUsdCurrencyBalance, BigDecimal.ZERO) && NumberUtil.equals(sourceJapanCurrencyBalance, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        if (hasEqualToZero()) {
            return injectPairByZeroBalance(sourceUsdCurrencyBalance, sourceJapanCurrencyBalance);
        }

        var totalCCUOfNew = getTotalCcuBalance(getUsdCurrency().add(sourceUsdCurrencyBalance), getJapanCurrency().add(sourceJapanCurrencyBalance));
        var totalCCUOfOld = getTotalCcuBalance(getUsdCurrency(), getJapanCurrency());

        var obtainCcuBalanceEachSide = totalCCUOfNew.subtract(totalCCUOfOld).max(BigDecimal.ZERO).divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
        var obtainCcuBalance = obtainCcuBalanceEachSide.multiply(BigDecimal.TWO);
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(usd)
                .setCurrencyBalance(sourceUsdCurrencyBalance)
                .setCcuBalance(obtainCcuBalanceEachSide));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(japan)
                .setCurrencyBalance(sourceJapanCurrencyBalance)
                .setCcuBalance(obtainCcuBalanceEachSide));
        return obtainCcuBalance;
    }

    public BigDecimal inject(LumenCurrencyModel sourceCurrency, BigDecimal sourceBalance) {
        var sourceUsdCurrencyBalance = Optional.of(sourceBalance).filter(s -> ObjectUtil.equals(usd.getId(), sourceCurrency.getId())).orElse(BigDecimal.ZERO);
        var sourceJapanCurrencyBalance = Optional.of(sourceBalance).filter(s -> ObjectUtil.equals(japan.getId(), sourceCurrency.getId())).orElse(BigDecimal.ZERO);
        return injectPair(sourceUsdCurrencyBalance, sourceJapanCurrencyBalance);
    }

    public BigDecimal exchange(LumenCurrencyModel sourceCurrency, BigDecimal sourceBalance) {
        checkBalanceGreaterThanZero();
        var ccuBalanceOfInject = inject(sourceCurrency, sourceBalance);
        var targetCurrency = getTargetCurrency(sourceCurrency);
        var targetCurrencyBalance = withdrawal(targetCurrency, ccuBalanceOfInject);
        return targetCurrencyBalance;
    }

    @SneakyThrows
    public List<LumenCcuBalanceModel> withdrawalPair(BigDecimal ccuBalance) {
        checkBalanceGreaterThanOrEqualToZero();
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var usdCcuBalance = getUsdCcu();
        var japanCcuBalance = getJapanCcu();
        var usdCurrencyBalance = getUsdCurrency();
        var japanCurrencyBalance = getJapanCurrency();
        var obtainCcuBalanceEachSide = ccuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
        var obtainUsdCurrencyBalance = usdCurrencyBalance.multiply(obtainCcuBalanceEachSide).divide(usdCcuBalance, 6, RoundingMode.FLOOR);
        var obtainJapanCurrencyBalance = japanCurrencyBalance.multiply(obtainCcuBalanceEachSide).divide(japanCcuBalance, 6, RoundingMode.FLOOR);
        var obtainList = new ArrayList<LumenCcuBalanceModel>();
        obtainList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(usd)
                .setCurrencyBalance(obtainUsdCurrencyBalance)
                .setCcuBalance(obtainCcuBalanceEachSide.multiply(new BigDecimal(-1))));
        obtainList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(japan)
                .setCurrencyBalance(obtainJapanCurrencyBalance)
                .setCcuBalance(obtainCcuBalanceEachSide.multiply(new BigDecimal(-1))));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(usd)
                .setCurrencyBalance(obtainUsdCurrencyBalance.multiply(new BigDecimal(-1)))
                .setCcuBalance(obtainCcuBalanceEachSide.multiply(new BigDecimal(-1))));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(japan)
                .setCurrencyBalance(obtainJapanCurrencyBalance.multiply(new BigDecimal(-1)))
                .setCcuBalance(obtainCcuBalanceEachSide.multiply(new BigDecimal(-1))));
        checkBalanceGreaterThanOrEqualToZero();
        return obtainList;
    }

    public BigDecimal withdrawal(LumenCurrencyModel targetCurrency, BigDecimal ccuBalance) {
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        if (NumberUtil.equals(ccuBalance, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var sourceCurrency = getTargetCurrency(targetCurrency);
        var sourceCurrencyBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getJapanCurrency() : getUsdCurrency();
        var targetCurrencyBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCurrency() : getJapanCurrency();
        var targetCcuBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCcu() : getJapanCcu();


        var obtainCcuBalanceEachSide = ccuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
        var obtainTargetCurrencyBalanceOfFirst = targetCurrencyBalance.multiply(obtainCcuBalanceEachSide).divide(targetCcuBalance, 6, RoundingMode.FLOOR);

        var obtainTargetCurrencyBalanceOfSecond = BigDecimal.ZERO;
        if (NumberUtil.isLessOrEqual(sourceCurrencyBalance, targetCcuBalance.subtract(obtainCcuBalanceEachSide))) {
            var ccuBalanceOfSecond = targetCcuBalance.subtract(obtainCcuBalanceEachSide).multiply(BigDecimal.TWO).subtract(sourceCurrencyBalance);
            var maxTargetCurrencyBalance = ccuBalanceOfSecond.multiply(sourceCurrencyBalance).divide(ccuBalanceOfSecond.subtract(sourceCurrencyBalance.multiply(BigDecimal.TWO)), 6, RoundingMode.FLOOR);
            obtainTargetCurrencyBalanceOfSecond = targetCurrencyBalance.subtract(obtainTargetCurrencyBalanceOfFirst).subtract(maxTargetCurrencyBalance).max(BigDecimal.ZERO);
        } else {
            // minCurrencyBalance = totalCCuOfTwoSide * maxCurrencyBalance / (maxCurrencyBalance * 3 + minCurrencyBalance - totalCCuOfTwoSide)
            var totalCCuOfTwoSide = targetCcuBalance.subtract(obtainCcuBalanceEachSide).multiply(BigDecimal.TWO);
            var amountOfSourceCurrencyBalanceMultiplyTotalCCuOfTwoSide = sourceCurrencyBalance.multiply(new BigDecimal(3)).subtract(totalCCuOfTwoSide);
            var amountOfTotalCCuOfTwoSideMultiplySourceCurrencyBalance = totalCCuOfTwoSide.multiply(sourceCurrencyBalance);
            var discriminant = amountOfSourceCurrencyBalanceMultiplyTotalCCuOfTwoSide.multiply(amountOfSourceCurrencyBalanceMultiplyTotalCCuOfTwoSide).add(amountOfTotalCCuOfTwoSideMultiplySourceCurrencyBalance.multiply(new BigDecimal(4)));

            if (NumberUtil.isLess(discriminant, BigDecimal.ZERO)) {
                throw new IllegalArgumentException("No real solution");
            }

            var totalTargetCurrencyBalanceOfNew = amountOfSourceCurrencyBalanceMultiplyTotalCCuOfTwoSide.multiply(new BigDecimal(-1)).add(discriminant.sqrt(MathContext.DECIMAL128)).divide(BigDecimal.TWO, 6, RoundingMode.FLOOR).max(BigDecimal.ZERO);
            obtainTargetCurrencyBalanceOfSecond = targetCurrencyBalance.subtract(obtainTargetCurrencyBalanceOfFirst).subtract(totalTargetCurrencyBalanceOfNew).max(BigDecimal.ZERO);
        }

        var obtainTargetCurrencyBalance = obtainTargetCurrencyBalanceOfFirst.add(obtainTargetCurrencyBalanceOfSecond);

        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(sourceCurrency)
                .setCurrencyBalance(BigDecimal.ZERO)
                .setCcuBalance(obtainCcuBalanceEachSide.multiply(new BigDecimal(-1))));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(targetCurrency)
                .setCurrencyBalance(obtainTargetCurrencyBalance.multiply(new BigDecimal(-1)))
                .setCcuBalance(obtainCcuBalanceEachSide.multiply(new BigDecimal(-1))));
        return obtainTargetCurrencyBalance;
    }

    private BigDecimal getTotalCcuBalance(BigDecimal sourceUsdCurrencyBalance, BigDecimal sourceJapanCurrencyBalance) {
        var minCurrencyBalance = sourceUsdCurrencyBalance.min(sourceJapanCurrencyBalance);
        var maxCurrencyBalance = sourceUsdCurrencyBalance.max(sourceJapanCurrencyBalance);
        var maxCCUBalance = minCurrencyBalance.multiply(BigDecimal.TWO);
        var ccuBalanceOfFirst = minCurrencyBalance;
        var ccuBalanceOfSecond = maxCCUBalance.multiply(maxCurrencyBalance).divide(maxCurrencyBalance.add(minCurrencyBalance), 6, RoundingMode.FLOOR);
        var totalCcuBalance = ccuBalanceOfFirst.add(ccuBalanceOfSecond);
        return totalCcuBalance;
    }

    private BigDecimal injectPairByZeroBalance(BigDecimal sourceUsdCurrencyBalance, BigDecimal sourceJapanCurrencyBalance) {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var totalCcuBalance = getTotalCcuBalance(sourceUsdCurrencyBalance, sourceJapanCurrencyBalance);
        var obtainCcuBalanceEachSide = totalCcuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
        var obtainCcuBalance = obtainCcuBalanceEachSide.multiply(BigDecimal.TWO);
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(usd)
                .setCurrencyBalance(sourceUsdCurrencyBalance)
                .setCcuBalance(obtainCcuBalanceEachSide));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(japan)
                .setCurrencyBalance(sourceJapanCurrencyBalance)
                .setCcuBalance(obtainCcuBalanceEachSide));
        return obtainCcuBalance;
    }


    private LumenCurrencyModel getTargetCurrency(LumenCurrencyModel sourceCurrency) {
        var targetCurrency = JinqStream.from(
                        List.of(
                                usd,
                                japan
                        )
                )
                .where(s -> ObjectUtil.notEqual(sourceCurrency.getId(), s.getId()))
                .getOnlyValue();
        return targetCurrency;
    }

    public BigDecimal getUsdCurrency() {
        var balance = this.combineBalance(usd);
        return balance.getCurrencyBalance();
    }

    public BigDecimal getUsdCcu() {
        var balance = this.combineBalance(usd);
        return balance.getCcuBalance();
    }

    public BigDecimal getJapanCurrency() {
        var balance = this.combineBalance(japan);
        return balance.getCurrencyBalance();
    }

    public BigDecimal getJapanCcu() {
        var balance = this.combineBalance(japan);
        return balance.getCcuBalance();
    }

    public LumenCcuBalanceModel combineBalance(LumenCurrencyModel currency) {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var list = JinqStream.from(ccuBalanceList)
                .where(s -> s.getCurrency().getId().equals(currency.getId()))
                .toList();
        var balance = new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(currency)
                .setCurrencyBalance(Optional.ofNullable(JinqStream.from(list).sumBigDecimal(s -> s.getCurrencyBalance())).orElse(BigDecimal.ZERO))
                .setCcuBalance(Optional.ofNullable(JinqStream.from(list).sumBigDecimal(s -> s.getCcuBalance())).orElse(BigDecimal.ZERO));
        return balance;
    }

    public LumenContextCoreModel() {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        usd = new LumenCurrencyModel()
                .setId(uuidUtil.v4())
                .setName("USD");
        japan = new LumenCurrencyModel()
                .setId(uuidUtil.v4())
                .setName("JAPAN");
        this.ccuBalanceList = new ArrayList<>();
    }

    public void checkCcuBalanceGreaterThanOrEqualZero(BigDecimal withdrawalCcuBalance) {
        if (NumberUtil.isLess(getUsdCcu().add(getJapanCcu()), withdrawalCcuBalance)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
    }

    public void checkSourceCurrencyBalanceGreaterZero(BigDecimal sourceCurrencyBalance) {
        if (NumberUtil.isLessOrEqual(sourceCurrencyBalance, BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
    }

    public void checkSourceCurrencyBalanceGreaterOrEqualZero(BigDecimal sourceCurrencyBalance) {
        if (NumberUtil.isLess(sourceCurrencyBalance, BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
    }

    public void checkBalanceGreaterThanZero() {
        if (NumberUtil.isLessOrEqual(getUsdCurrency(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getUsdCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getJapanCurrency(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getJapanCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
    }

    public void checkBalanceGreaterThanOrEqualToZero() {
        if (NumberUtil.isLess(getUsdCurrency(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getUsdCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getJapanCurrency(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getJapanCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
    }

    public boolean hasEqualToZero() {
        this.checkBalanceGreaterThanOrEqualToZero();
        if (NumberUtil.isGreater(getUsdCurrency().add(getUsdCcu()).add(getJapanCurrency()).add(getJapanCcu()), BigDecimal.ZERO)) {
            this.checkBalanceGreaterThanZero();
            return false;
        } else {
            return true;
        }
    }

}
