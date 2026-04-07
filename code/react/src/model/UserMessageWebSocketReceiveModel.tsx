import { makeAutoObservable } from 'mobx-react-use-autorun';
import { jsonArrayMember, jsonMember, jsonObject, toJson } from 'typedjson'
import { UserMessageModel } from '@model/UserMessageModel';

@jsonObject
@toJson
export class UserMessageWebSocketReceiveModel {

  @jsonMember(Number)
  totalPages!: number;

  @jsonArrayMember(UserMessageModel)
  items!: UserMessageModel[];

  constructor() {
    makeAutoObservable(this);
  }
}