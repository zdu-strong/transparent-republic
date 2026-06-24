import CheckPermissionComponent from "@/common/Server/CheckPagePermissionComponent";
import CountryDashboard from "@/component/CountryDashboard/CountryDashboard";
import MainMenu from "@component/SystemMenu/MainMenu";

export default <CheckPermissionComponent
    isAutoLogin={true}
    checkIsSignIn={true}
>
    <MainMenu>
        <CountryDashboard />
    </MainMenu>
</CheckPermissionComponent>