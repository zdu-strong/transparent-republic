import api from "@/api";
import LoadingOrErrorComponent from "@/common/MessageService/LoadingOrErrorComponent";
import { useMultipleQuery, useOnceSubmitWhileTrue } from "@/common/use-hook";
import { faFloppyDisk, faSpinner, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Fab, FormControlLabel, FormGroup, TextField } from "@mui/material";
import { observer, useMobxState } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { timer } from "rxjs";
import { UserModel } from "@/model/UserModel";
import type { SystemRoleModel } from "@/model/SystemRoleModel";
import { SuperAdminRoleQueryPaginationModel } from "@/model/SuperAdminRoleQueryPaginationModel";

type Props = {
    id: string;
    searchByPagination: () => void;
    closeDialog: () => void;
}

export default observer((props: Props) => {

    const state = useMobxState(() => {
        const user = new UserModel();
        user.username = "";
        user.password = "";
        user.roleList = [];
        user.userEmailList = [];
        return {
            user: user,
            submit: false,
            systemRoleList: [] as SystemRoleModel[],
            errors: {
                name() {
                    return state.submit
                        && !state.user.username
                        && "Please fill in the username";
                },
                password() {
                    return state.submit
                        && !props.id
                        && !state.user.password
                        && "Please fill in the password";
                },
                hasError() {
                    return Object.keys(state.errors)
                        .filter(s => s !== "hasError")
                        .some(s => (state.errors as any)[s]());
                }
            }
        };
    });

    const { ready, error } = useMultipleQuery(async () => {
        if (props.id) {
            state.user = await api.User.getUserById(props.id);
        }
        const queryBody = new SuperAdminRoleQueryPaginationModel();
        queryBody.pageSize = 10;
        state.systemRoleList = (await api.SuperAdminSystemRoleQuery.searchByPagination(queryBody)).items;
    });

    const { loading, resubmit } = useOnceSubmitWhileTrue(async () => {
        state.submit = true;
        if (state.errors.hasError()) {
            return false;
        }
        await timer(500).toPromise();
        if (props.id) {
            await api.User.updateUser(state.user);
        } else {
            await api.User.createUser(state.user);
        }

        props.searchByPagination();
        props.closeDialog();
        return true;
    })

    function closeDialog(event: {}, reason: "backdropClick" | "escapeKeyDown") {
        if (reason === "backdropClick") {
            return;
        }
        props.closeDialog();
    }

    function switchCheckedOfRole(e: React.ChangeEvent<HTMLInputElement>, role: SystemRoleModel) {
        const index = state.user.roleList.findIndex(s => s.id === role.id);
        if (index >= 0) {
            state.user.roleList.splice(index, 1);
        } else {
            state.user.roleList.push(role);
        }
    }

    function isCheckedOfRole(role: SystemRoleModel) {
        const index = state.user.roleList.findIndex(s => s.id === role.id);
        const isChecked = index >= 0;
        return isChecked;
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
                    {props.id
                        ? <FormattedMessage id="Update" defaultMessage="Update" />
                        : <FormattedMessage id="Create" defaultMessage="Create" />}
                </div>
                <Fab color="default" id="closeButton" onClick={props.closeDialog}>
                    <FontAwesomeIcon icon={faXmark} size="xl" />
                </Fab>
            </DialogTitle>
            <Divider />
            <DialogContent style={{ padding: "1em" }}>
                <LoadingOrErrorComponent ready={ready} error={error} >
                    <div className="flex flex-row">
                        <TextField
                            label={<FormattedMessage id="Username" defaultMessage="Username" />}
                            defaultValue={state.user.username}
                            onChange={(e) => state.user.username = e.target.value}
                            error={!!state.errors.name()}
                            helperText={state.errors.name()}
                            autoFocus={true}
                        />
                    </div>
                    <div className="flex flex-row" style={{ marginTop: "1em" }}>
                        <TextField
                            label={<FormattedMessage id="Password" defaultMessage="Password" />}
                            defaultValue={state.user.password}
                            onChange={(e) => state.user.password = e.target.value}
                            error={!!state.errors.password()}
                            helperText={state.errors.password()}
                        />
                    </div>
                    <div className="flex flex-row" style={{ marginTop: "1em" }}>
                        <div className="flex flex-row" style={{ marginRight: "1em" }}>
                            <FormattedMessage id="SystemRole" defaultMessage="System Role" />
                            {":"}
                        </div>
                        <FormGroup>
                            {state.systemRoleList.map(role => <FormControlLabel
                                control={<Checkbox checked={isCheckedOfRole(role)} onChange={(e) => switchCheckedOfRole(e, role)} />}
                                label={role.name}
                                key={role.id}
                            />)}
                        </FormGroup>
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