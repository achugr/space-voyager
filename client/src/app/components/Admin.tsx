import "./Admin.css";
import {UserTokenData} from "../service/spaceAuth";
import {grabOrganizationData} from "../service/orgGrabber";
import {Alert, Button, Snackbar} from "@mui/material";
import RefreshIcon from '@mui/icons-material/Refresh';
import {useState} from "@hookstate/core";

interface AdminProps {
    userTokenData: UserTokenData
}

export function Admin(props: AdminProps) {

    const grabberStarted = useState(Boolean)

    let grabData = () => {
        grabOrganizationData(props.userTokenData);
        grabberStarted.set(true)
    }

    let unsetGrabDisabled = () => grabberStarted.set(false);

    return (
        <div className="admin-panel">
            <Button disabled={grabberStarted.get()} type={"button"} onClick={grabData}>
                Grab data
                <RefreshIcon/>
            </Button>
            <Snackbar open={grabberStarted.get()}
                      autoHideDuration={6000}
                      onClose={unsetGrabDisabled}
            >
                <Alert onClose={unsetGrabDisabled} severity="success" sx={{width: '100%'}}>
                    Grabber started in the background
                </Alert>
            </Snackbar>
        </div>
    )
}
