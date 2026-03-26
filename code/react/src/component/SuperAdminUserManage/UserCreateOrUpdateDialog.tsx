import api from "@/api";
import LoadingOrErrorComponent from "@/common/MessageService/LoadingOrErrorComponent";
import { useMultipleQuery, useOnceSubmitWhileTrue } from "@/common/use-hook";
import { faFloppyDisk, faSpinner, faSquarePlus, faTrashCan, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Fab, TextField } from "@mui/material";
import { observer, useMobxState } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { timer } from "rxjs";
import { UserModel } from "@/model/UserModel";
import type { SystemRoleModel } from "@/model/SystemRoleModel";
import { SuperAdminRoleQueryPaginationModel } from "@/model/SuperAdminRoleQueryPaginationModel";
import { DataGrid, useGridApiRef, type GridColDef } from "@mui/x-data-grid";
import SuperAdminRoleDetailButton from "@component/SuperAdminRoleManage/SuperAdminRoleDetailButton";
import { v4 } from "uuid";
import UserChooseRoleDialog from "@component/SuperAdminUserManage/UserChooseRoleDialog";

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
            addDialog: {
                id: v4(),
                open: false,
            },
        };
    });

    const errors = {
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
            return Object.keys(errors)
                .filter(s => s !== "hasError")
                .some(s => (errors as any)[s]());
        }
    };

    const columns: GridColDef<SystemRoleModel>[] = [
        {
            headerName: 'ID',
            field: 'id',
            width: 290
        },
        {
            renderHeader: () => <FormattedMessage id="Name" defaultMessage="Name" />,
            field: 'name',
            width: 150,
            flex: 1,
        },
        {
            renderHeader: () => <Button
                variant="contained"
                onClick={openAddDialog}
                startIcon={<FontAwesomeIcon icon={faSquarePlus} />}
                style={{ marginLeft: "1em" }}
                size="small"
            >
                <FormattedMessage id="Add" defaultMessage="Add" />
            </Button>,
            field: '',
            renderCell: (row) => <div className="flex flex-row items-center justify-between h-full">
                <SuperAdminRoleDetailButton
                    id={row.row.id}
                    searchByPagination={() => { }}
                    isOnlyView={true}
                />
                <Button
                    variant="contained"
                    onClick={() => removeCheckedOfRole(row.row)}
                    startIcon={<FontAwesomeIcon icon={faTrashCan} />}
                    style={{ marginLeft: "1em" }}
                >
                    <FormattedMessage id="Delete" defaultMessage="Delete" />
                </Button>
            </div>,
            width: 230,
        },
    ];

    const dataGridRef = useGridApiRef();

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
        if (errors.hasError()) {
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

    function removeCheckedOfRole(role: SystemRoleModel) {
        const index = state.user.roleList.findIndex(s => s.id === role.id);
        if (index >= 0) {
            state.user.roleList.splice(index, 1);
        }
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

    function openAddDialog() {
        state.addDialog.id = v4();
        state.addDialog.open = true;
    }

    function closeAddDialog() {
        state.addDialog.open = false;
    }

    return <>
        <Dialog
            open={true}
            onClose={closeDialog}
            disableRestoreFocus={true}
            fullWidth={true}
            maxWidth="xl"
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
                            error={!!errors.name()}
                            helperText={errors.name()}
                            autoFocus={true}
                        />
                    </div>
                    <div className="flex flex-row" style={{ marginTop: "1em" }}>
                        <TextField
                            label={<FormattedMessage id="Password" defaultMessage="Password" />}
                            defaultValue={state.user.password}
                            onChange={(e) => state.user.password = e.target.value}
                            error={!!errors.password()}
                            helperText={errors.password()}
                        />
                    </div>
                    <div className="flex flex-row">
                        <FormattedMessage id="RoleList" defaultMessage="Role List" />
                        {":"}
                    </div>
                    <div className="flex flex-row" style={{ paddingBottom: "1px" }}>
                        <DataGrid
                            rows={state.user.roleList}
                            rowCount={state.user.roleList.length}
                            apiRef={dataGridRef}
                            sortingMode="server"
                            paginationMode="server"
                            getRowId={(s) => s.id}
                            columns={columns}
                            hideFooter
                            disableRowSelectionOnClick
                            disableColumnMenu
                            disableColumnResize
                            disableColumnSorting
                        />
                    </div>
                </LoadingOrErrorComponent>
            </DialogContent>
            {ready && <>
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
            </>}
        </Dialog>
        {state.addDialog.open && <UserChooseRoleDialog
            switchCheckedOfRole={switchCheckedOfRole}
            isCheckedOfRole={isCheckedOfRole}
            closeDialog={closeAddDialog}
        />}
    </>
})