import api from "@/api";
import LoadingOrErrorComponent from "@/common/MessageService/LoadingOrErrorComponent";
import { useMultipleQuery, useOnceSubmit } from "@/common/use-hook";
import { faFloppyDisk, faSpinner, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Fab, TextField } from "@mui/material";
import { observer, useMobxState } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { SystemRoleModel } from "@/model/SystemRoleModel";
import { timer } from "rxjs";

type Props = {
    id: string;
    searchByPagination: () => void;
    closeDialog: () => void;
}

export default observer((props: Props) => {

    const state = useMobxState(() => {
        const role = new SystemRoleModel();
        role.permissionList = [];
        return {
            role: role
        };
    });

    const { ready, error } = useMultipleQuery(async () => {
        if (props.id) {
            state.role = await api.Role.getRoleById(props.id);
        }
    });

    const { loading, resubmit } = useOnceSubmit(async () => {
        await timer(500).toPromise();
        if (props.id) {
            // await api.Role.deleteRoleById(props.id);
        } else {
            // await api.Role.deleteRoleById(props.id);
        }

        props.searchByPagination();
        props.closeDialog();
    })

    function closeDialog(event: {}, reason: "backdropClick" | "escapeKeyDown") {
        if (reason === "backdropClick") {
            return;
        }
        props.closeDialog();
    }

    return <>
        <Dialog
            open={true}
            onClose={closeDialog}
            disableRestoreFocus={true}
            fullWidth={true}
        >
            <DialogTitle className="justify-between items-center flex-row flex-auto flex">
                <div className="flex flex-row items-center" >
                    <FormattedMessage id="RoleDetail" defaultMessage="Role Detail" />
                </div>
                <Fab color="default" id="closeButton" onClick={props.closeDialog}>
                    <FontAwesomeIcon icon={faXmark} size="xl" />
                </Fab>
            </DialogTitle>
            <Divider />
            <DialogContent style={{ padding: "1em" }}>
                <LoadingOrErrorComponent ready={ready} error={error} >
                    <div className="flex flex-row">
                        <div className="flex flex-row" style={{ marginRight: "1em" }}>
                            <FormattedMessage id="RoleName" defaultMessage="Role Name" />
                            {":"}
                        </div>
                        <div className="flex flex-row">
                            <TextField
                                label={<FormattedMessage id="RoleName" defaultMessage="Role Name" />}
                                defaultValue={state.role.name}
                                onChange={(e) => state.role.name = e.target.value}
                            />
                        </div>
                    </div>
                    <div className="flex flex-row">
                        <div className="flex flex-row" style={{ marginRight: "1em" }}>
                            <FormattedMessage id="PermissionList" defaultMessage="Permission List" />
                            {":"}
                        </div>
                        {state.role.permissionList.length == 0 && <div className="flex flex-row">
                            <FormattedMessage id="RoleListIsEmpty" defaultMessage="Role list is empty" />
                        </div>}
                        {state.role.permissionList.length > 0 && <div className="flex flex-col">
                            {state.role.permissionList.map(permissionRelationModel =>
                                <div className="flex flex-row" key={permissionRelationModel.id}>
                                    {permissionRelationModel.permission}
                                </div>
                            )}
                        </div>}
                    </div>
                </LoadingOrErrorComponent>
            </DialogContent>
            <Divider />
            <DialogActions>
                <Button
                    variant="contained"
                    onClick={resubmit}
                    startIcon={<FontAwesomeIcon icon={loading ? faSpinner : faFloppyDisk} spin={loading} />}
                >
                    <FormattedMessage id="Save" defaultMessage="Save" />
                </Button>
            </DialogActions>
        </Dialog>
    </>
})