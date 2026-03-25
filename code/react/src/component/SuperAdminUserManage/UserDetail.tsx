import { UserModel } from "@/model/UserModel";
import { observer, useMobxState } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { format } from "date-fns";
import { DataGrid, useGridApiRef, type GridColDef } from "@mui/x-data-grid";
import type { SystemRoleModel } from "@/model/SystemRoleModel";
import SuperAdminRoleDetailButton from "@component/SuperAdminRoleManage/SuperAdminRoleDetailButton";

type Props = {
    user: UserModel;
}

export default observer((props: Props) => {

    const dataGridRef = useGridApiRef();

    const state = useMobxState({
        columns: [
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
        ] as GridColDef<SystemRoleModel>[],
    });

    return <>
        <div className="flex flex-row">
            <div className="flex flex-row" style={{ marginRight: "1em" }}>
                <FormattedMessage id="Username" defaultMessage="Username" />
                {":"}
            </div>
            <div className="flex flex-row">
                {props.user.username}
            </div>
        </div>
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
            <FormattedMessage id="RoleList" defaultMessage="Role List" />
            {":"}
        </div>
        <div className="flex flex-row" style={{ paddingBottom: "1px" }}>
            <DataGrid
                rows={props.user.roleList}
                rowCount={props.user.roleList.length}
                apiRef={dataGridRef}
                sortingMode="server"
                paginationMode="server"
                getRowId={(s) => s.id}
                columns={state.columns}
                hideFooter
                disableRowSelectionOnClick
                disableColumnMenu
                disableColumnResize
                disableColumnSorting
            />
        </div>
    </>
})