import CheckPermissionComponent from "@/common/Server/CheckPagePermissionComponent";
import SignUp from '@component/SignUp/SignUp';
import MainMenuForSignIn from "@component/SystemMenu/MainMenuForSignIn";

export default <CheckPermissionComponent
    checkIsNotSignIn={true}
>
    <MainMenuForSignIn>
        <SignUp />
    </MainMenuForSignIn>
</CheckPermissionComponent>