import { jsonMember, jsonObject } from 'typedjson'
import { makeAutoObservable } from 'mobx-react-use-autorun'
import { UserModel } from '@model/UserModel';
import { OrganizeModel } from '@model/OrganizeModel';

@jsonObject
export class IdentityCardModel {

    @jsonMember(String)
    id!: string;

    @jsonMember(String)
    identityType!: string;

    @jsonMember(String)
    address!: string;

    @jsonMember(Date)
    createDate!: Date;

    @jsonMember(Date)
    updateDate!: Date;

    @jsonMember(() => UserModel)
    user!: UserModel;

    @jsonMember(() => OrganizeModel)
    topOrganize!: OrganizeModel;

    @jsonMember(() => OrganizeModel)
    governanceRegion!: OrganizeModel;

    constructor() {
        makeAutoObservable(this);
    }

}
