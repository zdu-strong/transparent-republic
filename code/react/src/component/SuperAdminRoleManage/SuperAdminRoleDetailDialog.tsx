import api from "@/api";
import LoadingOrErrorComponent from "@/common/MessageService/LoadingOrErrorComponent";
import { useMultipleQuery, useOnceSubmit } from "@/common/use-hook";
import { faSpinner, faTrashCan, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Fab } from "@mui/material";
import { observer, useMobxState } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { SystemRoleModel } from "@/model/SystemRoleModel";
import RoleDetail from "./RoleDetail";
import { MessageService } from "@/common/MessageService";

type Props = {
    id: string;
    searchByPagination: () => void;
    closeDialog: () => void;
}

export default observer((props: Props) => {

    const state = useMobxState({
        role: new SystemRoleModel(),
    });

    const { ready, error } = useMultipleQuery(async () => {
        state.role = await api.Role.getRoleById(props.id);
    });

    const { loading, resubmit } = useOnceSubmit(async () => {
        await api.Role.deleteRoleById(props.id);
        props.searchByPagination();
        props.closeDialog();
    })

    function confirmDeleteRole() {
        MessageService.confirm(<FormattedMessage
            id="AreYouSureDeleteRole"
            defaultMessage={`Are you sure you want to delete the role "{roleName}"?`}
            values={{
                roleName: state.role.name
            }}
        />, resubmit);
    }

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
                    <RoleDetail
                        role={state.role}
                    />
                </LoadingOrErrorComponent>
            </DialogContent>
            <Divider />
            <DialogActions>
                <Button
                    variant="contained"
                    onClick={confirmDeleteRole}
                    startIcon={<FontAwesomeIcon icon={loading ? faSpinner : faTrashCan} spin={loading} />}
                >
                    <FormattedMessage id="Delete" defaultMessage="Delete" />
                </Button>
            </DialogActions>
        </Dialog>
    </>
})