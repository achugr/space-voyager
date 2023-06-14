import {httpGet, httpPost} from "./utils";
import {Simulate} from "react-dom/test-utils";
import error = Simulate.error;

export interface AppInfo {
    isLocal: boolean
}

export async function getAppInfo(): Promise<AppInfo> {
    return httpGet("/api/info", "noauth")
        .then((response) => response.json())
        .then(json => json as AppInfo)
}