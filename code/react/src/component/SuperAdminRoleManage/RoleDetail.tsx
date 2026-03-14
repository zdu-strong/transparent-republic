import { observer } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { format } from "date-fns";
import type { SystemRoleModel } from "@/model/SystemRoleModel";

type Props = {
    role: SystemRoleModel;
}

export default observer((props: Props) => {

    return <>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="RoleName" defaultMessage="Role Name" />
                {":"}
            </div>
            <div className="flex flex-row">
                {props.role.name}
            </div>
        </div>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="ID" defaultMessage="ID" />
                {":"}
            </div>
            <div className="flex flex-row">
                {props.role.id}
            </div>
        </div>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="CreateDate" defaultMessage="Create Date" />
                {":"}
            </div>
            <div className="flex flex-row">
                {format(props.role.createDate, "yyyy-MM-dd HH:mm:ss")}
            </div>
        </div>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="UpdateDate" defaultMessage="Update Date" />
                {":"}
            </div>
            <div className="flex flex-row">
                {format(props.role.updateDate, "yyyy-MM-dd HH:mm:ss")}
            </div>
        </div>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="PermissionList" defaultMessage="Permission List" />
                {":"}
            </div>
            {props.role.permissionList.length == 0 && <div className="flex flex-row">
                <FormattedMessage id="RoleListIsEmpty" defaultMessage="Role list is empty" />
            </div>}
            {props.role.permissionList.length > 0 && <div className="flex flex-col">
                {props.role.permissionList.map(permissionRelationModel =>
                    <div className="flex flex-row" key={permissionRelationModel.id}>
                        {permissionRelationModel.permission}
                    </div>
                )}
            </div>}
        </div>
    </>
})