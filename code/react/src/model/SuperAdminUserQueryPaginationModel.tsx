import { jsonMember, jsonObject, toJson } from 'typedjson'
import { makeAutoObservable } from 'mobx-react-use-autorun'

@jsonObject
@toJson
export class SuperAdminUserQueryPaginationModel {

    @jsonMember(Number)
    pageNum: number = 1;

    @jsonMember(Number)
    pageSize: number = 1;

    constructor() {
        makeAutoObservable(this);
    }

}
