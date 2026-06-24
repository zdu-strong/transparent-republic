import CheckPermissionComponent from "@/common/Server/CheckPagePermissionComponent";
import MainMenu from "@component/SystemMenu/MainMenu";
import SuperAdminUserManage from "@component/SuperAdminUserManage/SuperAdminUserManage";

export default <CheckPermissionComponent
    isAutoLogin={true}
    checkIsSignIn={true}
>
    <MainMenu>
        <SuperAdminUserManage />
    </MainMenu>
</CheckPermissionComponent>