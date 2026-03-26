import { observer, useMobxState } from "mobx-react-use-autorun";
import { FormattedMessage } from "react-intl";
import { format } from "date-fns";
import type { SystemRoleModel } from "@/model/SystemRoleModel";
import { v4 } from "uuid";
import SuperAdminRoleDetailButton from "@component/SuperAdminRoleManage/SuperAdminRoleDetailButton";
import { SystemPermissionModel } from "@/model/SystemPermissionModel";
import { DataGrid, useGridApiRef, type GridColDef } from "@mui/x-data-grid";
import { isOrganizePermission, isSystemPermission, SystemPermissionEnum } from "@/enums/SystemPermissionEnum";
import { $enum } from "ts-enum-util";
import linq from 'linq';

type Props = {
    role: SystemRoleModel;
}

export default observer((props: Props) => {

    const state = useMobxState(() => {
        const allPermissionList = $enum(SystemPermissionEnum).getValues()
            .map(s => {
                const systemPermissionModel = new SystemPermissionModel();
                systemPermissionModel.id = v4();
                systemPermissionModel.permission = s;
                return systemPermissionModel;
            });
        return {
            allPermissionList,
        };
    });

    const permissionListOfSystem = linq.from(state.allPermissionList)
        .where(s => isSystemPermission(s.permission))
        .where(s => isCheckedOfPermission(s))
        .toArray();

    const permissionListOfOrganize = linq.from(props.role.permissionList)
        .where(s => isOrganizePermission(s.permission))
        .orderBy(s => s.organize.id)
        .orderBy(s => s.organize.createDate)
        .toArray();

    const columnsOfSystemPermission: GridColDef<SystemPermissionModel>[] = [
        {
            headerName: 'ID',
            field: 'id',
            width: 290
        },
        {
            renderHeader: () => <FormattedMessage id="Permission" defaultMessage="Permission" />,
            field: 'permission',
            width: 150,
            flex: 1,
        },
        {
            renderHeader: () => <FormattedMessage id="Operation" defaultMessage="Operation" />,
            field: '',
            renderCell: (row) => <div className="flex flex-row items-center justify-between h-full">
                <SuperAdminRoleDetailButton
                    id={row.row.id}
                    searchByPagination={() => { }}
                    isOnlyView={true}
                />
            </div>,
            width: 230,
        },
    ];

    const columnsOfOrganizePermission: GridColDef<SystemPermissionModel>[] = [
        {
            headerName: 'ID',
            field: 'id',
            width: 290
        },
        {
            renderHeader: () => <FormattedMessage id="OrganizeName" defaultMessage="Organize Name" />,
            field: 'organize',
            renderCell: (row) => {
                return <div>
                    {row.row.organize ? row.row.organize.name : ""}
                </div>
            },
            width: 150,
            flex: 1,
        },
        {
            renderHeader: () => <FormattedMessage id="Permission" defaultMessage="Permission" />,
            field: 'permission',
            width: 150,
        },
        {
            renderHeader: () => <FormattedMessage id="Operation" defaultMessage="Operation" />,
            field: '',
            renderCell: (row) => <div className="flex flex-row items-center justify-between h-full">
                <SuperAdminRoleDetailButton
                    id={row.row.id}
                    searchByPagination={() => { }}
                    isOnlyView={true}
                />
            </div>,
            width: 230,
        },
    ];

    const dataGridOfOrganizePermissionRef = useGridApiRef();

    const dataGridOfSystemPermissionRef = useGridApiRef();

    function isCheckedOfPermission(systemPermissionModel: SystemPermissionModel) {
        const permission = systemPermissionModel.permission;
        const organizeId: string = systemPermissionModel.organize ? systemPermissionModel.organize.id : "";
        const isChecked = linq.from(props.role.permissionList)
            .where(s => s.permission === permission)
            .any(s => isSystemPermission(permission) || s.organize.id === organizeId);
        return isChecked;
    }


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
            <FormattedMessage id="SystemPermission" defaultMessage="System Permission" />
            {":"}
        </div>
        <div className="flex flex-row" style={{ paddingBottom: "1px" }}>
            <DataGrid
                rows={permissionListOfSystem}
                rowCount={permissionListOfSystem.length}
                apiRef={dataGridOfSystemPermissionRef}
                sortingMode="server"
                paginationMode="server"
                getRowId={(s) => s.id}
                columns={columnsOfSystemPermission}
                hideFooter
                disableRowSelectionOnClick
                disableColumnMenu
                disableColumnResize
                disableColumnSorting
            />
        </div>
        <div className="flex flex-row">
            <FormattedMessage id="OrganizePermission" defaultMessage="Organize Permission" />
            {":"}
        </div>
        <div className="flex flex-row" style={{ paddingBottom: "1px" }}>
            <DataGrid
                rows={permissionListOfOrganize}
                rowCount={permissionListOfOrganize.length}
                apiRef={dataGridOfOrganizePermissionRef}
                sortingMode="server"
                paginationMode="server"
                getRowId={(s) => s.id}
                columns={columnsOfOrganizePermission}
                hideFooter
                disableRowSelectionOnClick
                disableColumnMenu
                disableColumnResize
                disableColumnSorting
            />
        </div>
    </>
})