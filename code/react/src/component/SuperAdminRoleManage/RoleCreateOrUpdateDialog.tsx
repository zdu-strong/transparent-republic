import api from "@/api";
import LoadingOrErrorComponent from "@/common/MessageService/LoadingOrErrorComponent";
import { useMultipleQuery, useOnceSubmitWhileTrue } from "@/common/use-hook";
import { faFloppyDisk, faSpinner, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Fab, FormControlLabel, FormGroup, TextField } from "@mui/material";
import { observer, useMobxState } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { SystemRoleModel } from "@/model/SystemRoleModel";
import { timer } from "rxjs";
import { isOrganizePermission, isSystemPermission, SystemPermissionEnum } from "@/enums/SystemPermissionEnum";
import { $enum } from "ts-enum-util";
import linq from 'linq';
import { SystemPermissionModel } from "@/model/SystemPermissionModel";
import { v7 } from "uuid";
import { OrganizeModel } from "@/model/OrganizeModel";

type Props = {
    id: string;
    searchByPagination: () => void;
    closeDialog: () => void;
}

export default observer((props: Props) => {

    const state = useMobxState(() => {
        const role = new SystemRoleModel();
        role.permissionList = [];
        const permissionListOfSystem = $enum(SystemPermissionEnum).getValues().filter(s => isSystemPermission(s));
        const permissionListOfOrganize = $enum(SystemPermissionEnum).getValues().filter(s => isOrganizePermission(s));
        return {
            role: role,
            submit: false,
            permissionListOfSystem: permissionListOfSystem,
            permissionListOfOrganize: permissionListOfOrganize,
            errors: {
                name() {
                    return state.submit &&
                        !state.role.name &&
                        "Please fill in the name";
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
            state.role = await api.Role.getRoleById(props.id);
        }
    });

    const { loading, resubmit } = useOnceSubmitWhileTrue(async () => {
        state.submit = true;
        if (state.errors.hasError()) {
            return false;
        }
        await timer(500).toPromise();
        if (props.id) {
            await api.Role.updateRole(state.role);
        } else {
            await api.Role.createRole(state.role);
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

    function switchCheckedOfPermission(e: React.ChangeEvent<HTMLInputElement>, permission: SystemPermissionEnum, organizeId?: string) {
        if (isSystemPermission(permission)) {
            const index = state.role.permissionList.findIndex(s => isSystemPermission($enum(SystemPermissionEnum).asValueOrThrow(s.permission)) && s.permission === permission);
            if (index >= 0) {
                state.role.permissionList.splice(index, 1);
            } else {
                const systemPermissionModel = new SystemPermissionModel();
                systemPermissionModel.id = v7();
                systemPermissionModel.permission = permission;
                state.role.permissionList.push(systemPermissionModel);
            }
        } else {
            const index = state.role.permissionList.findIndex(s => isOrganizePermission($enum(SystemPermissionEnum).asValueOrThrow(s.permission)) && s.organize.id === organizeId && s.permission === permission);
            if (index >= 0) {
                state.role.permissionList.splice(index, 1);
            } else {
                const systemPermissionModel = new SystemPermissionModel();
                systemPermissionModel.id = v7();
                systemPermissionModel.permission = permission;
                systemPermissionModel.organize = new OrganizeModel();
                systemPermissionModel.organize.id = organizeId!;
                state.role.permissionList.push(systemPermissionModel);
            }
        }
    }

    function isCheckedOfPermission(permission: SystemPermissionEnum, organizeId?: string) {
        const isChecked = linq.from(state.role.permissionList).any(s => isSystemPermission($enum(SystemPermissionEnum).asValueOrThrow(s.permission)) && s.permission === permission);
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
                            label={<FormattedMessage id="RoleName" defaultMessage="Role Name" />}
                            defaultValue={state.role.name}
                            onChange={(e) => state.role.name = e.target.value}
                            error={!!state.errors.name()}
                            helperText={state.errors.name()}
                            autoFocus={true}
                        />
                    </div>
                    <div className="flex flex-row">
                        <div className="flex flex-row" style={{ marginRight: "1em" }}>
                            <FormattedMessage id="SystemRole" defaultMessage="System Role" />
                            {":"}
                        </div>
                        {state.permissionListOfSystem.length == 0 && <div className="flex flex-row">
                            <FormattedMessage id="RoleListIsEmpty" defaultMessage="Role list is empty" />
                        </div>}
                        {state.permissionListOfSystem.length > 0 && <FormGroup>
                            {state.permissionListOfSystem.map(permission =>
                                <FormControlLabel
                                    key={permission}
                                    control={<Checkbox
                                        checked={isCheckedOfPermission(permission)}
                                        onChange={(e) => switchCheckedOfPermission(e, permission)}
                                    />}
                                    label={permission}
                                />
                            )}
                        </FormGroup>}
                    </div>
                    <div className="flex flex-row">
                        <div className="flex flex-row" style={{ marginRight: "1em" }}>
                            <FormattedMessage id="OrganizeRole" defaultMessage="Organize Role" />
                            {":"}
                        </div>
                        {/* {state.permissionListOfSystem.length == 0 && <div className="flex flex-row">
                            <FormattedMessage id="RoleListIsEmpty" defaultMessage="Role list is empty" />
                        </div>}
                        {state.permissionListOfSystem.length > 0 && <FormGroup>
                            {state.permissionListOfSystem.map(permission =>
                                <FormControlLabel
                                    key={permission}
                                    control={<Checkbox
                                        defaultChecked={linq.from(state.role.permissionList).any(s => !s.isOrganizePermission && s.permission === permission)}
                                        onChange={(e) => switchCheckedOfPermission(e, permission)}
                                    />}
                                    label={permission}
                                />
                            )}
                        </FormGroup>} */}
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