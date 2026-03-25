import api from "@/api";
import LoadingOrErrorComponent from "@/common/MessageService/LoadingOrErrorComponent";
import { useMultipleQuery } from "@/common/use-hook";
import { faCircleCheck, faCircleXmark, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Fab } from "@mui/material";
import { observer, useMobxState } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { SystemRoleModel } from "@/model/SystemRoleModel";
import SuperAdminRoleDetailButton from "@component/SuperAdminRoleManage/SuperAdminRoleDetailButton";
import { DataGrid, useGridApiRef, type GridColDef } from "@mui/x-data-grid";
import { SuperAdminRoleQueryPaginationModel } from "@/model/SuperAdminRoleQueryPaginationModel";
import { PaginationModel } from "@/model/PaginationModel";

type Props = {
    switchCheckedOfRole: (e: React.ChangeEvent<HTMLInputElement>, role: SystemRoleModel) => void;
    isCheckedOfRole: (role: SystemRoleModel) => boolean;
    closeDialog: () => void;
}

export default observer((props: Props) => {

    const state = useMobxState(() => {
        const query = new SuperAdminRoleQueryPaginationModel();
        query.isOnlySystemRole = true;
        return {
            query: query,
            paginationModel: new PaginationModel<SystemRoleModel>(),
        };
    });

    const roleQueryState = useMultipleQuery(async () => {
        state.paginationModel = await api.SuperAdminSystemRoleQuery.searchByPagination(state.query);
    });

    const columns: GridColDef<SystemRoleModel>[] = [
        {
            renderHeader: () => "",
            field: 'createDate',
            renderCell: (row) => <Checkbox
                checked={props.isCheckedOfRole(row.row)}
                onChange={(e) => props.switchCheckedOfRole(e, row.row)}
            />,
            width: 70,
        },
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
            renderHeader: () => <FormattedMessage id="Operation" defaultMessage="Operation" />,
            field: '',
            renderCell: (row) => <SuperAdminRoleDetailButton
                id={row.row.id}
                searchByPagination={() => { }}
                isOnlyView={true}
            />,
            width: 150,
        },
    ];

    const dataGridRef = useGridApiRef();

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
            maxWidth="xl"
            fullScreen={true}
        >
            <DialogTitle className="justify-between items-center flex-row flex-auto flex">
                <div className="flex flex-row items-center" >
                    <FormattedMessage id="Add" defaultMessage="Add" />
                </div>
                <Fab color="default" id="closeButton" onClick={props.closeDialog}>
                    <FontAwesomeIcon icon={faXmark} size="xl" />
                </Fab>
            </DialogTitle>
            <Divider />
            <DialogContent style={{ padding: "1em" }}>
                <LoadingOrErrorComponent ready={roleQueryState.ready} error={roleQueryState.error} >
                    <div className="flex flex-auto" style={{ paddingBottom: "1px" }}>
                        <DataGrid
                            rows={state.paginationModel.items}
                            rowCount={state.paginationModel.totalRecords}
                            onPaginationModelChange={(s) => {
                                state.query.pageNum = Math.max(s.page + 1, 1);
                                state.query.pageSize = Math.max(s.pageSize, 1);
                                roleQueryState.requery();
                            }}
                            apiRef={dataGridRef}
                            sortingMode="server"
                            paginationMode="server"
                            getRowId={(s) => s.id}
                            columns={columns}
                            autoPageSize
                            disableRowSelectionOnClick
                            disableColumnMenu
                            disableColumnResize
                            disableColumnSorting
                            autoHeight
                        />
                    </div>
                </LoadingOrErrorComponent>
            </DialogContent>
            <Divider />
            <DialogActions>
                <Button
                    variant="contained"
                    startIcon={<FontAwesomeIcon icon={faCircleXmark} size="xl" />}
                    onClick={props.closeDialog}
                >
                    <FormattedMessage id="Cancel" defaultMessage="Cancel" />
                </Button>
                <Button
                    variant="contained"
                    startIcon={<FontAwesomeIcon icon={faCircleCheck} size="xl" />}
                    onClick={props.closeDialog}
                >
                    <FormattedMessage id="Confirm" defaultMessage="Confirm" />
                </Button>
            </DialogActions>
        </Dialog>
    </>
})