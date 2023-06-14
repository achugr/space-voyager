import './App.css';
import {AppTabs} from "./components/AppTabs";
import {AppInfo, getAppInfo} from "./service/appInfo";
import {useState} from "@hookstate/core";
import {useEffect} from "react";

interface AppState {
    loaded: boolean
    appInfo?: AppInfo
}

export const App = () => {
    const state = useState({loaded: false, appInfo: undefined} as AppState)

    useEffect(() => {
        getAppInfo().then(appInfo => {
            state.appInfo.set(appInfo)
            state.loaded.set(true)
        })
    }, [state.loaded])

    return <>
        {
            state.loaded.get() &&
            <div className="app">
                <AppTabs appInfo={state.appInfo.get()!}/>
            </div>
        }
    </>
}
