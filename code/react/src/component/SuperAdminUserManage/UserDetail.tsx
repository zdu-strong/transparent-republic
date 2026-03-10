import { UserModel } from "@/model/UserModel";
import { observer } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { format } from "date-fns";
import { Button } from "@mui/material";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSpinner, faTrashCan } from "@fortawesome/free-solid-svg-icons";
import { useOnceSubmit } from "@/common/use-hook";
import api from "@/api";

type Props = {
    user: UserModel;
    closeDialog: () => void;
    searchByPagination: () => void;
}

export default observer((props: Props) => {

    const { loading, resubmit } = useOnceSubmit(async () => {
        await api.User.deleteUserById(props.user.id);
        props.searchByPagination();
        props.closeDialog();
    })

    return <>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="ID" defaultMessage="ID" />
                {":"}
            </div>
            <div className="flex flex-row">
                {props.user.id}
            </div>
        </div>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="UserName" defaultMessage="Username" />
                {":"}
            </div>
            <div className="flex flex-row">
                {props.user.username}
            </div>
        </div>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="CreateDate" defaultMessage="Create Date" />
                {":"}
            </div>
            <div className="flex flex-row">
                {format(props.user.createDate, "yyyy-MM-dd HH:mm:ss")}
            </div>
        </div>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="UpdateDate" defaultMessage="Update Date" />
                {":"}
            </div>
            <div className="flex flex-row">
                {format(props.user.updateDate, "yyyy-MM-dd HH:mm:ss")}
            </div>
        </div>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="Role" defaultMessage="Role" />
                {":"}
            </div>
            {props.user.roleList.length == 0 && <div className="flex flex-row">
                <FormattedMessage id="RoleListIsEmpty" defaultMessage="Role list is empty" />
            </div>}
            {props.user.roleList.length > 0 && <div className="flex flex-col">
                {props.user.roleList.map(role => <div className="flex flex-row" key={role.id}>
                    {role.name}
                </div>)}
            </div>}
        </div>
        <div className="flex flex-row">
            <Button
                variant="contained"
                onClick={resubmit}
                startIcon={<FontAwesomeIcon icon={loading ? faSpinner : faTrashCan} spin={loading} />}
            >
                <FormattedMessage id="Delete" defaultMessage="Delete" />
            </Button>
        </div>
    </>
})