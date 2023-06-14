import "./AppTabs.css";
import {useState} from "@hookstate/core";
import {AppTab} from "./AppTab";
import fetchSpaceUserToken, {
    appPermissionScope,
    requestAppPermissions,
    requestUserToken,
    UserTokenData
} from "../service/spaceAuth";
import {httpGet} from "../service/utils";
import Loader from "./Loader";
import {grabOrganizationData} from "../service/orgGrabber";
import SpaceGraph from "./SpaceGraph";
import DiscoveryGraphRenderer from "./DiscoveryGraphRenderer";
import {AppInfo} from "../service/appInfo";
import {Admin} from "./Admin";
import {Button, Snackbar} from "@mui/material";

enum ActiveTab {
    Voyager = 0,
    Discovery = 1,
    Admin = 2
}

interface AppTabsState {
    activeTab: ActiveTab
}

interface AppTabContentsState {
    userTokenData?: UserTokenData;
    appHasPermissions: boolean;
}

interface AppTabsProps {
    appInfo: AppInfo
}

export function AppTabs(props: AppTabsProps) {
    const state = useState(initialState);
    const loadingState = useState(() => loadInitialTabState(props.appInfo));

    if (loadingState.promised) {
        return (<Loader/>);
    }

    const action = (
        <>
            <Button color="info" size="small" onClick={() =>
                requestAppPermissions(
                    appPermissionScope,
                    (success: boolean) => {
                        if (success) {
                            loadingState.appHasPermissions.set(true);
                            grabOrganizationData(loadingState.userTokenData.get())
                        }
                    }
                )
            }>
                Authorize the app
            </Button>
        </>
    );


    return (
        <>
            {
                !loadingState.appHasPermissions.get() &&
                <div className="admin-panel">
                    <Snackbar open={!loadingState.appHasPermissions.get()}
                              autoHideDuration={60000}
                              action={action}
                              message={"Authorize the app to access data for building the network"}
                              anchorOrigin={{vertical: 'top', horizontal: 'center'}}
                    />
                </div>
            }
            {
                loadingState.appHasPermissions.get() &&
                <div className="tab-group">
                    <AppTab
                        name="Voyager view"
                        isActive={state.activeTab.get() === ActiveTab.Voyager}
                        onClick={() => state.activeTab.set(ActiveTab.Voyager)}
                    />

                    <AppTab
                        name="Discovery view"
                        isActive={state.activeTab.get() === ActiveTab.Discovery}
                        onClick={() => state.activeTab.set(ActiveTab.Discovery)}
                    />

                    <AppTab
                        name="Admin"
                        isActive={state.activeTab.get() === ActiveTab.Admin}
                        onClick={() => state.activeTab.set(ActiveTab.Admin)}
                    />
                </div>
            }
            {
                loadingState.appHasPermissions.get() && loadingState.userTokenData.get() &&
                {
                    0: <SpaceGraph userTokenData={loadingState.userTokenData.get()!!}/>,
                    1: <DiscoveryGraphRenderer userTokenData={loadingState.userTokenData.get()!!}/>,
                    2: <Admin userTokenData={loadingState.userTokenData.get()!!}/>
                }[state.activeTab.get()]
            }
        </>
    );
}

function initialState(): AppTabsState {
    return {
        activeTab: ActiveTab.Voyager
    }
}


function loadInitialTabState(appInfo: AppInfo): Promise<AppTabContentsState> {
    if (appInfo.isLocal) {
        return Promise.resolve({
            userTokenData: {} as UserTokenData,
            appHasPermissions: true
        } as AppTabContentsState)
    }
    return new Promise(async (resolve) => {
        const userTokenData = await fetchSpaceUserToken(false, "");
        if (userTokenData === undefined) {
            throw "Could not get userToken with empty permissionScope";
        }

        const appHasPermissionsResponseRaw = await httpGet("/homepage/app-has-permissions", userTokenData.userToken);
        const appHasPermissionsResponse = (await appHasPermissionsResponseRaw.json()) as AppHasPermissionsResponse

        requestUserToken(false, "", (newUserTokenData) => {
            resolve({
                userTokenData: newUserTokenData,
                appHasPermissions: appHasPermissionsResponse.hasPermissions,
            })
        });
    });
}

interface AppHasPermissionsResponse {
    hasPermissions: boolean
}