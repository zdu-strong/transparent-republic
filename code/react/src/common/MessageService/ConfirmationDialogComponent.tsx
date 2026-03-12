import { faCircleCheck, faCircleXmark, faXmark } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Button, Dialog, DialogActions, DialogTitle, Divider, Fab } from "@mui/material";
import { observer } from "mobx-react-use-autorun";
import type { ReactNode } from "react";
import { FormattedMessage } from "react-intl";

type Props = {
    title: ReactNode;
    closeDialog: () => void;
    confirmCallback: () => void;
}

export default observer((props: Props) => {

    function closeDialog(event: {}, reason: "backdropClick" | "escapeKeyDown") {
        if (reason === "backdropClick") {
            return;
        }
        props.closeDialog();
    }

    function confirmCallback() {
        props.closeDialog();
        props.confirmCallback();
    }

    return <>
        <Dialog
            open={true}
            onClose={closeDialog}
            disableRestoreFocus={true}
            fullWidth={true}
        >
            <DialogTitle className="justify-between items-center flex-row flex-auto flex">
                <div className="flex flex-row items-center" >
                    {props.title}
                </div>
                <Fab color="default" id="closeButton" onClick={props.closeDialog}>
                    <FontAwesomeIcon icon={faXmark} size="xl" />
                </Fab>
            </DialogTitle>
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
                    onClick={confirmCallback}
                >
                    <FormattedMessage id="Confirm" defaultMessage="Confirm" />
                </Button>
            </DialogActions>
        </Dialog>
    </>
})