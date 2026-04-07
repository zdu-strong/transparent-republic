import { makeAutoObservable } from "mobx-react-use-autorun";
import { jsonMember, jsonObject, toJson } from "typedjson";

@jsonObject
@toJson
export class GitPropertiesModel {

    @jsonMember(String)
    commitId!: string;

    @jsonMember(Date)
    commitDate!: Date;

    constructor() {
        makeAutoObservable(this);
    }
}

