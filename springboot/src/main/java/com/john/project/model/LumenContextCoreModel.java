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
        return this.injectPair(this.usd, sourceUsdCurrencyBalance, this.japan, sourceJapanCurrencyBalance);
    }

    @SneakyThrows
    public List<LumenCcuBalanceModel> withdrawalPair(BigDecimal ccuBalance) {
        checkBalanceGreaterThanOrEqualToZero();
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);
        var usdCurrencyBalance = getUsdCurrencyBalance();
        var japanCurrencyBalance = getJapanCurrencyBalance();
        var usdCcuBalance = getUsdCcu();
        var japanCcuBalance = getJapanCcu();
        var totalCcuBalance = usdCcuBalance.add(japanCcuBalance);
        var obtainUsdCurrencyBalance = usdCurrencyBalance.multiply(ccuBalance).multiply(usdCcuBalance).divide(totalCcuBalance.multiply(usdCcuBalance), 6, RoundingMode.FLOOR);
        var obtainJapanCurrencyBalance = japanCurrencyBalance.multiply(ccuBalance).multiply(japanCcuBalance).divide(totalCcuBalance.multiply(japanCcuBalance), 6, RoundingMode.FLOOR);

        var obtainUsdCcuBalance = BigDecimal.ZERO;
        var obtainJapanCcuBalance = BigDecimal.ZERO;

        if (NumberUtil.isLess(usdCcuBalance, japanCcuBalance)) {
            obtainUsdCcuBalance = usdCcuBalance.multiply(ccuBalance).divide(totalCcuBalance, 6, RoundingMode.FLOOR);
            obtainJapanCcuBalance = ccuBalance.subtract(obtainUsdCcuBalance);
        } else {
            obtainJapanCcuBalance = japanCcuBalance.multiply(ccuBalance).divide(totalCcuBalance, 6, RoundingMode.FLOOR);
            obtainUsdCcuBalance = ccuBalance.subtract(obtainJapanCcuBalance);
        }

        var obtainLumenCcuBalanceList = List.of(new LumenCcuBalanceModel()
                        .setId(uuidUtil.v4())
                        .setCurrency(usd)
                        .setCurrencyBalance(obtainUsdCurrencyBalance)
                        .setCcuBalance(obtainUsdCcuBalance.negate()),
                new LumenCcuBalanceModel()
                        .setId(uuidUtil.v4())
                        .setCurrency(japan)
                        .setCurrencyBalance(obtainJapanCurrencyBalance)
                        .setCcuBalance(obtainJapanCcuBalance.negate())
        );
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(usd)
                .setCurrencyBalance(obtainUsdCurrencyBalance.negate())
                .setCcuBalance(obtainUsdCcuBalance.negate()));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(japan)
                .setCurrencyBalance(obtainJapanCurrencyBalance.negate())
                .setCcuBalance(obtainJapanCcuBalance.negate()));
        checkBalanceGreaterThanOrEqualToZero();
        return obtainLumenCcuBalanceList;
    }

    public BigDecimal exchange(LumenCurrencyModel sourceCurrency, BigDecimal sourceCurrencyBalanceForExchange) {
        var obtainSourceCcuBalance = this.inject(sourceCurrency, sourceCurrencyBalanceForExchange);
        var targetCurrency = getTargetCurrency(sourceCurrency);
        return this.withdrawal(targetCurrency, obtainSourceCcuBalance);
    }

    public BigDecimal inject(LumenCurrencyModel sourceCurrency, BigDecimal sourceBalanceForInject) {
        var sourceCurrencyBalance = getCurrencyBalance(sourceCurrency);
        var sourceCcuBalance = getCcuBalance(sourceCurrency);
        var targetCurrency = getTargetCurrency(sourceCurrency);
        var targetCurrencyBalance = getCurrencyBalance(targetCurrency);
        var targetCcuBalance = getCcuBalance(targetCurrency);
        var totalCcuBalance = sourceCcuBalance.add(targetCcuBalance);

        if (NumberUtil.isLessOrEqual(sourceCcuBalance, targetCcuBalance)) {
            var obtainSourceCcuBalanceFirst = sourceBalanceForInject.multiply(targetCcuBalance).divide(sourceCurrencyBalance, 6, RoundingMode.FLOOR);
            var obtainSourceCcuBalanceSecond = sourceCcuBalance.multiply(BigDecimal.TWO).multiply(obtainSourceCcuBalanceFirst.add(targetCcuBalance)).divide(obtainSourceCcuBalanceFirst.add(totalCcuBalance), 6, RoundingMode.FLOOR);

            var obtainSourceCcuBalance = obtainSourceCcuBalanceSecond.subtract(targetCcuBalance).max(BigDecimal.ZERO);
            this.addCcuToList(sourceCurrency, sourceBalanceForInject, obtainSourceCcuBalance, targetCurrency, BigDecimal.ZERO, BigDecimal.ZERO);
            return obtainSourceCcuBalance;
        } else {

        }

        return BigDecimal.ZERO;
    }

    public BigDecimal withdrawal(LumenCurrencyModel targetCurrency, BigDecimal ccuBalance) {
        checkBalanceGreaterThanZero();
        checkCcuBalanceGreaterThanOrEqualZero(ccuBalance);

        if (NumberUtil.equals(ccuBalance, BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        var sourceCurrency = getTargetCurrency(targetCurrency);
        var sourceCcuBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getJapanCcu() : getUsdCcu();
        var targetCcuBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCcu() : getJapanCcu();
        var sourceCurrencyBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getJapanCurrencyBalance() : getUsdCurrencyBalance();
        var targetCurrencyBalance = ObjectUtil.equals(usd.getId(), targetCurrency.getId()) ? getUsdCurrencyBalance() : getJapanCurrencyBalance();
        var totalCcuBalance = sourceCcuBalance.add(targetCcuBalance);

        var obtainTargetCurrencyBalanceFirst = targetCurrencyBalance.multiply(ccuBalance.add(targetCcuBalance)).divide(ccuBalance.add(totalCcuBalance), 6, RoundingMode.FLOOR);

        var lumenCcuBalanceList = this.withdrawalPair(ccuBalance);
        var obtainSourceCurrencyBalanceFirst = JinqStream.from(lumenCcuBalanceList)
                .where(s -> ObjectUtil.equals(sourceCurrency.getId(), s.getCurrency().getId()))
                .sumBigDecimal(s -> s.getCurrencyBalance());
        var obtainTargetCurrencyBalanceSecond = JinqStream.from(lumenCcuBalanceList)
                .where(s -> ObjectUtil.equals(targetCurrency.getId(), s.getCurrency().getId()))
                .sumBigDecimal(s -> s.getCurrencyBalance());
        var obtainCcuBalanceFirst = this.inject(sourceCurrency, obtainSourceCurrencyBalanceFirst);

        var obtainTargetCurrencyBalance = obtainTargetCurrencyBalanceFirst.max(obtainTargetCurrencyBalanceSecond);

        this.addCcuToList(sourceCurrency, BigDecimal.ZERO, BigDecimal.ZERO, targetCurrency, obtainTargetCurrencyBalanceSecond.subtract(obtainTargetCurrencyBalance), obtainCcuBalanceFirst.negate());

        checkBalanceGreaterThanOrEqualToZero();

        return obtainTargetCurrencyBalance;
    }

    private BigDecimal injectPair(LumenCurrencyModel sourceFirstCurrency, BigDecimal sourceFirstCurrencyBalance, LumenCurrencyModel sourceSecondCurrency, BigDecimal sourceSecondCurrencyBalance) {
        var firstCurrencyBalance = getCurrencyBalance(sourceFirstCurrency);
        var secondCurrencyBalance = getCurrencyBalance(sourceSecondCurrency);
        var firstCcuBalance = getCcuBalance(sourceFirstCurrency);
        var secondCcuBalance = getCcuBalance(sourceSecondCurrency);
        var totalCcuBalance = firstCcuBalance.add(secondCcuBalance);

        checkSourceCurrencyBalanceGreaterOrEqualZero(sourceFirstCurrencyBalance);
        checkSourceCurrencyBalanceGreaterOrEqualZero(sourceSecondCurrencyBalance);

        if (hasEqualToZero()) {
            return injectPairByZeroBalance(sourceFirstCurrency, sourceFirstCurrencyBalance, sourceSecondCurrency, sourceSecondCurrencyBalance);
        }

        var obtainFirstCcuBalanceOne = sourceFirstCurrencyBalance.multiply(totalCcuBalance).divide(firstCurrencyBalance.multiply(BigDecimal.TWO), 6, RoundingMode.FLOOR);
        var obtainSecondCcuBalanceTwo = sourceSecondCurrencyBalance.multiply(totalCcuBalance).divide(secondCurrencyBalance.multiply(BigDecimal.TWO), 6, RoundingMode.FLOOR);

        if (NumberUtil.isLess(obtainFirstCcuBalanceOne, obtainSecondCcuBalanceTwo)) {
            return this.injectPair(sourceSecondCurrency, sourceSecondCurrencyBalance, sourceFirstCurrency, sourceFirstCurrencyBalance);
        }

        var obtainSourceFirstCurrencyBalance = obtainSecondCcuBalanceTwo.multiply(firstCurrencyBalance).multiply(BigDecimal.TWO).divide(totalCcuBalance, 6, RoundingMode.FLOOR);
        var surplusFirstCurrencyBalance = sourceFirstCurrencyBalance.subtract(obtainSourceFirstCurrencyBalance);
        this.addCcuToList(sourceFirstCurrency, obtainSourceFirstCurrencyBalance, obtainSecondCcuBalanceTwo, sourceSecondCurrency, obtainSecondCcuBalanceTwo, obtainSecondCcuBalanceTwo);

        var obtainTotalCcuBalance = obtainSecondCcuBalanceTwo
                .multiply(BigDecimal.TWO)
                .add(inject(sourceFirstCurrency, surplusFirstCurrencyBalance));
        return obtainTotalCcuBalance;
    }

    private void addCcuToList(LumenCurrencyModel sourceFirstCurrency, BigDecimal sourceFirstCurrencyBalance, BigDecimal sourceFirstCcuBalance, LumenCurrencyModel sourceSecondCurrency, BigDecimal sourceSecondCurrencyBalance, BigDecimal sourceSecondCcuBalance) {
        var uuidUtil = SpringUtil.getBean(UUIDUtil.class);

        if (!hasEqualToZero() && NumberUtil.isGreater(sourceFirstCcuBalance, BigDecimal.ZERO) && NumberUtil.isGreater(sourceSecondCcuBalance, BigDecimal.ZERO)) {
            var totalCcuBalance = sourceFirstCcuBalance.add(sourceSecondCcuBalance);
            var firstCcuBalance = getCcuBalance(sourceFirstCurrency);
            var secondCcuBalance = getCcuBalance(sourceSecondCurrency);
            var hypotenuseFirstCcuBalance = totalCcuBalance.multiply(secondCcuBalance.add(sourceSecondCcuBalance)).divide(firstCcuBalance.add(secondCcuBalance).add(totalCcuBalance), 6, RoundingMode.FLOOR);
            var hypotenuseSecondCcuBalance = totalCcuBalance.subtract(hypotenuseFirstCcuBalance);
            sourceFirstCcuBalance = hypotenuseFirstCcuBalance;
            sourceSecondCcuBalance = hypotenuseSecondCcuBalance;
        } else if (!hasEqualToZero() && NumberUtil.equals(sourceSecondCcuBalance, BigDecimal.ZERO)) {
            var reverseFirstCcuBalance = sourceSecondCcuBalance;
            var reverseSecondCcuBalance = sourceFirstCcuBalance;
            sourceFirstCcuBalance = reverseFirstCcuBalance;
            sourceSecondCcuBalance = reverseSecondCcuBalance;
        } else if (!hasEqualToZero() && NumberUtil.equals(sourceFirstCcuBalance, BigDecimal.ZERO)) {
            var reverseFirstCcuBalance = sourceSecondCcuBalance;
            var reverseSecondCcuBalance = sourceFirstCcuBalance;
            sourceFirstCcuBalance = reverseFirstCcuBalance;
            sourceSecondCcuBalance = reverseSecondCcuBalance;
        }

        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(sourceFirstCurrency)
                .setCurrencyBalance(sourceFirstCurrencyBalance)
                .setCcuBalance(sourceFirstCcuBalance));
        ccuBalanceList.add(new LumenCcuBalanceModel()
                .setId(uuidUtil.v4())
                .setCurrency(sourceSecondCurrency)
                .setCurrencyBalance(sourceSecondCurrencyBalance)
                .setCcuBalance(sourceSecondCcuBalance));
    }

    private BigDecimal injectPairByZeroBalance(LumenCurrencyModel sourceFirstCurrency, BigDecimal sourceFirstCurrencyBalance, LumenCurrencyModel sourceSecondCurrency, BigDecimal sourceSecondCurrencyBalance) {
        var obtainCcuBalanceEachSide = sourceFirstCurrencyBalance.max(sourceSecondCurrencyBalance);
        var obtainCcuBalance = obtainCcuBalanceEachSide.multiply(BigDecimal.TWO);
        this.addCcuToList(sourceFirstCurrency, sourceFirstCurrencyBalance, obtainCcuBalanceEachSide, sourceSecondCurrency, sourceSecondCurrencyBalance, obtainCcuBalanceEachSide);
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

    public BigDecimal getCcuBalance(LumenCurrencyModel lumenCurrencyModel) {
        if (ObjectUtil.equals(usd.getId(), lumenCurrencyModel.getId())) {
            return getUsdCcu();
        } else {
            return getJapanCcu();
        }
    }

    public BigDecimal getCurrencyBalance(LumenCurrencyModel lumenCurrencyModel) {
        if (ObjectUtil.equals(usd.getId(), lumenCurrencyModel.getId())) {
            return getUsdCurrencyBalance();
        } else {
            return getJapanCurrencyBalance();
        }
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
