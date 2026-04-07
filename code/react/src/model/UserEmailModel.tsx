import { makeAutoObservable } from 'mobx-react-use-autorun';
import { jsonMember, jsonObject, toJson } from 'typedjson'
import { VerificationCodeEmailModel } from '@model/VerificationCodeEmailModel';

@jsonObject
@toJson
export class UserEmailModel {

    @jsonMember(String)
    id?: string;

    @jsonMember(String)
    email!: string;

    @jsonMember(VerificationCodeEmailModel)
    verificationCodeEmail!: VerificationCodeEmailModel;

    constructor() {
        makeAutoObservable(this);
    }
}