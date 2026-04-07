import { makeAutoObservable } from "mobx-react-use-autorun";
import { jsonMember, jsonObject, toJson } from "typedjson";

@jsonObject
@toJson
export class LumenCurrencyModel {

    @jsonMember(String)
    id!: string;

    @jsonMember(String)
    name!: string;

    constructor() {
        makeAutoObservable(this);
    }
}

