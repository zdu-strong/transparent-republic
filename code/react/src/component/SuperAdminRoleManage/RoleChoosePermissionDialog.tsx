import api from "@/api";
import LoadingOrErrorComponent from "@/common/MessageService/LoadingOrErrorComponent";
import { useMultipleQuery } from "@/common/use-hook";
import { faSearch, faSpinner, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Checkbox, Dialog, DialogContent, DialogTitle, Divider, Fab } from "@mui/material";
import { observer, toJS, useMobxEffect, useMobxState } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { DataGrid, useGridApiRef, type GridColDef } from "@mui/x-data-grid";
import { PaginationModel } from "@/model/PaginationModel";
import type { SystemPermissionModel } from "@/model/SystemPermissionModel";
import type { OrganizeModel } from "@/model/OrganizeModel";
import { SuperAdminOrganizeQueryPaginationModel } from "@/model/SuperAdminOrganizeQueryPaginationModel";
import { format } from "date-fns";
import SuperAdminOrganizeDetailButton from "../SuperAdminOrganizeManage/SuperAdminOrganizeDetailButton";
import linq from "linq";

type Props = {
    switchCheckedOfPermission: (systemPermissionModel: SystemPermissionModel) => void;
    isCheckedOfPermission: (systemPermissionModel: SystemPermissionModel) => boolean;
    closeDialog: () => void;
}

export default observer((props: Props) => {

    const state = useMobxState(() => {
        const query = new SuperAdminOrganizeQueryPaginationModel();
        return {
            query: query,
            paginationModel: new PaginationModel<OrganizeModel>(),
            organizeOfCheckedList: [] as OrganizeModel[],
        };
    });

    const organizeQueryState = useMultipleQuery(async () => {
        state.paginationModel = await api.SuperAdminOrganizeQuery.searchByPagination(state.query);
    });

    function isCheckedOfOrganize(organize: OrganizeModel) {
        return linq.from(state.organizeOfCheckedList)
            .any(s => s.id === organize.id);
    }

    function switchCheckedOfOrganize(organize: OrganizeModel) {
        const index = state.organizeOfCheckedList.findIndex(s => s.id === organize.id);
        if (index >= 0) {
            state.organizeOfCheckedList.splice(index, 1);
        } else {
            state.organizeOfCheckedList.push(organize);
        }
    }

    const columns: GridColDef<OrganizeModel>[] = [
        {
            renderHeader: () => "",
            field: 'checkbox',
            renderCell: (row) => <Checkbox
                checked={isCheckedOfOrganize(row.row)}
                onChange={(e) => switchCheckedOfOrganize(row.row)}
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
            renderHeader: () => <FormattedMessage id="CreateDate" defaultMessage="Create Date" />,
            field: 'createDate',
            renderCell: (row) => {
                return <div>
                    {format(row.row.createDate, "yyyy-MM-dd HH:mm:ss")}
                </div>
            },
            width: 150,
        },
        {
            renderHeader: () => <FormattedMessage id="Operation" defaultMessage="Operation" />,
            field: '',
            renderCell: (row) => <SuperAdminOrganizeDetailButton
                id={row.row.id}
                searchByPagination={organizeQueryState.requery}
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

    useMobxEffect(() => {
        organizeQueryState.requery();
    }, [state.query])

    toJS(state.organizeOfCheckedList)

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
                <LoadingOrErrorComponent ready={organizeQueryState.ready} error={organizeQueryState.error} >
                    <div className="flex flex-row" style={{ marginTop: "1em", marginBottom: "1em" }}>
                        <Button
                            variant="contained"
                            onClick={organizeQueryState.requery}
                            startIcon={<FontAwesomeIcon icon={organizeQueryState.loading ? faSpinner : faSearch} spin={organizeQueryState.loading} />}
                        >
                            <FormattedMessage id="Refresh" defaultMessage="Refresh" />
                        </Button>
                    </div>
                    <div className="flex flex-auto" style={{ paddingBottom: "1px" }}>
                        <DataGrid
                            rows={state.paginationModel.items}
                            rowCount={state.paginationModel.totalRecords}
                            onPaginationModelChange={(s) => {
                                state.query.pageNum = Math.max(s.page + 1, 1);
                                state.query.pageSize = Math.max(s.pageSize, 1);
                                organizeQueryState.requery();
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
        </Dialog>
    </>
})