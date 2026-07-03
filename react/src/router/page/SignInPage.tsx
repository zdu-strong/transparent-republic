import CheckPermissionComponent from "@/common/Server/CheckPagePermissionComponent";
import SignIn from '@component/SignIn/SignIn';
import MainMenuForSignIn from "@component/SystemMenu/MainMenuForSignIn";


export default <CheckPermissionComponent
    checkIsNotSignIn={true}
>
    <MainMenuForSignIn>
        <SignIn />
    </MainMenuForSignIn>
</CheckPermissionComponent>