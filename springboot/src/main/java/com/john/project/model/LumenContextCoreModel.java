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

        var totalCCUOfNew = getTotalCcuBalance(getUsdCurrencyBalance().add(sourceUsdCurrencyBalance), getJapanCurrencyBalance().add(sourceJapanCurrencyBalance));
        var totalCCUOfOld = getUsdCcu().add(getJapanCcu());
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

    @SneakyThrows
    public List<LumenCcuBalanceModel> withdrawalPair(BigDecimal ccuBalance) {
        checkBalanceGreaterThanOrEqualToZero();
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var totalCcuBalanceAsDenominator = getTotalCcuBalanceAsDenominator();

        var usdCurrencyBalance = getUsdCurrencyBalance();
        var japanCurrencyBalance = getJapanCurrencyBalance();
        var obtainCcuBalanceEachSide = ccuBalance.divide(BigDecimal.TWO, 6, RoundingMode.FLOOR);
        var obtainUsdCurrencyBalance = usdCurrencyBalance.multiply(obtainCcuBalanceEachSide).multiply(BigDecimal.TWO).divide(totalCcuBalanceAsDenominator, 6, RoundingMode.FLOOR);
        var obtainJapanCurrencyBalance = japanCurrencyBalance.multiply(obtainCcuBalanceEachSide).multiply(BigDecimal.TWO).divide(totalCcuBalanceAsDenominator, 6, RoundingMode.FLOOR);
        var obtainUsdCcuBalance = getUsdCcu().multiply(ccuBalance).divide(getUsdCcu().add(getJapanCcu()), 6, RoundingMode.FLOOR).multiply(new BigDecimal(-1));
        var obtainJapanCcuBalance = getJapanCcu().multiply(ccuBalance).divide(getUsdCcu().add(getJapanCcu()), 6, RoundingMode.FLOOR).multiply(new BigDecimal(-1));

        var obtainLumenCcuBalanceList = List.of(new LumenCcuBalanceModel()
                        .setId(uuidUtil.v4())
                        .setCurrency(usd)
                        .setCurrencyBalance(obtainUsdCurrencyBalance)
                        .setCcuBalance(obtainUsdCcuBalance),
                new LumenCcuBalanceModel()
                        .setId(uuidUtil.v4())
                        .setCurrency(japan)
                        .setCurrencyBalance(obtainJapanCurrencyBalance)
                        .setCcuBalance(obtainJapanCcuBalance)
        );
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
        return obtainLumenCcuBalanceList;
    }

    public BigDecimal exchange(LumenCurrencyModel sourceCurrency, BigDecimal sourceBalance) {
        checkBalanceGreaterThanZero();
        var ccuBalanceOfInject = inject(sourceCurrency, sourceBalance);
        var targetCurrency = getTargetCurrency(sourceCurrency);
        var targetCurrencyBalance = withdrawal(targetCurrency, ccuBalanceOfInject);
        return targetCurrencyBalance;
    }

    public BigDecimal inject(LumenCurrencyModel sourceCurrency, BigDecimal sourceBalance) {
        var sourceUsdCurrencyBalance = Optional.of(sourceBalance).filter(s -> ObjectUtil.equals(usd.getId(), sourceCurrency.getId())).orElse(BigDecimal.ZERO);
        var sourceJapanCurrencyBalance = Optional.of(sourceBalance).filter(s -> ObjectUtil.equals(japan.getId(), sourceCurrency.getId())).orElse(BigDecimal.ZERO);
        return injectPair(sourceUsdCurrencyBalance, sourceJapanCurrencyBalance);
    }

    public BigDecimal withdrawal(LumenCurrencyModel targetCurrency, BigDecimal ccuBalance) {
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        if (NumberUtil.equals(ccuBalance, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var sourceCcuBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getJapanCcu() : getUsdCcu();
        var targetCcuBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCcu() : getJapanCcu();
        var sourceCurrency = getTargetCurrency(targetCurrency);
        var targetCurrencyBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCurrencyBalance() : getJapanCurrencyBalance();
        var obtainSourceCcuBalance = sourceCcuBalance.multiply(ccuBalance).divide(getUsdCcu().add(getJapanCcu()), 6, RoundingMode.FLOOR).multiply(new BigDecimal(-1));
        var obtainTargetCcuBalance = targetCcuBalance.multiply(ccuBalance).divide(getUsdCcu().add(getJapanCcu()), 6, RoundingMode.FLOOR).multiply(new BigDecimal(-1));

        var obtainTargetCurrencyBalance = targetCurrencyBalance.multiply(ccuBalance).multiply(new BigDecimal("0.625")).divide(targetCcuBalance.add(obtainTargetCcuBalance).add(ccuBalance), 6, RoundingMode.FLOOR);
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(sourceCurrency)
                .setCurrencyBalance(BigDecimal.ZERO)
                .setCcuBalance(obtainSourceCcuBalance));

        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(targetCurrency)
                .setCurrencyBalance(obtainTargetCurrencyBalance.multiply(new BigDecimal(-1)))
                .setCcuBalance(obtainTargetCcuBalance));

        checkBalanceGreaterThanOrEqualToZero();

        return obtainTargetCurrencyBalance;
    }

    private BigDecimal getTotalCcuBalance(BigDecimal sourceUsdCurrencyBalance, BigDecimal sourceJapanCurrencyBalance) {
        var minCurrencyBalance = sourceUsdCurrencyBalance.min(sourceJapanCurrencyBalance);
        var maxCurrencyBalance = sourceUsdCurrencyBalance.max(sourceJapanCurrencyBalance);
        var ccuBalanceOfThird = minCurrencyBalance.multiply(new BigDecimal(4)).multiply(maxCurrencyBalance.subtract(minCurrencyBalance)).divide(maxCurrencyBalance.add(minCurrencyBalance.multiply(BigDecimal.TWO)), 6, RoundingMode.FLOOR);
        var totalCcuBalance = minCurrencyBalance.multiply(BigDecimal.TWO).add(ccuBalanceOfThird);
        return totalCcuBalance;
    }

    private BigDecimal getTotalCcuBalanceAsDenominator() {
        var sourceUsdCurrencyBalance = getUsdCurrencyBalance();
        var sourceJapanCurrencyBalance = getJapanCurrencyBalance();
        return getTotalCcuBalance(sourceUsdCurrencyBalance, sourceJapanCurrencyBalance);
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

    public BigDecimal getUsdCurrencyBalance() {
        var balance = this.combineBalance(usd);
        return balance.getCurrencyBalance();
    }

    public BigDecimal getUsdCcu() {
        var balance = this.combineBalance(usd);
        return balance.getCcuBalance();
    }

    public BigDecimal getJapanCurrencyBalance() {
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
        if (NumberUtil.isLessOrEqual(getUsdCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getUsdCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getJapanCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
        if (NumberUtil.isLessOrEqual(getJapanCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance must greater than 0");
        }
    }

    public void checkBalanceGreaterThanOrEqualToZero() {
        if (NumberUtil.isLess(getUsdCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getUsdCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getJapanCurrencyBalance(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
        if (NumberUtil.isLess(getJapanCcu(), BigDecimal.ZERO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance cannot less than 0");
        }
    }

    public boolean hasEqualToZero() {
        this.checkBalanceGreaterThanOrEqualToZero();
        if (NumberUtil.isGreater(getUsdCurrencyBalance().add(getUsdCcu()).add(getJapanCurrencyBalance()).add(getJapanCcu()), BigDecimal.ZERO)) {
            this.checkBalanceGreaterThanZero();
            return false;
        } else {
            return true;
        }
    }

}
