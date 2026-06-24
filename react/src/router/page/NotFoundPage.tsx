import CheckPermissionComponent from "@/common/Server/CheckPagePermissionComponent";
import NotFound from "@component/NotFound/NotFound";
import MainMenu from "@component/SystemMenu/MainMenu";

export default <CheckPermissionComponent
    isAutoLogin={true}
    checkIsSignIn={true}
>
    <MainMenu>
        {NotFound}
    </MainMenu>
</CheckPermissionComponent>