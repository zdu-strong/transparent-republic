import { makeAutoObservable } from "mobx-react-use-autorun";
import { jsonMember, jsonObject, toJson } from "typedjson";
import { LumenCurrencyModel } from "@model/LumenCurrencyModel";
import { BigDecimal } from "bigdecimal.js";

@jsonObject
@toJson
export class LumenCcuBalanceModel {

    @jsonMember(String)
    id!: string;

    @jsonMember(LumenCurrencyModel)
    currency!: LumenCurrencyModel;

    @jsonMember(BigDecimal)
    currencyBalance!: BigDecimal;

    @jsonMember(BigDecimal)
    ccuBalance!: BigDecimal;

    constructor() {
        makeAutoObservable(this);
    }
}

