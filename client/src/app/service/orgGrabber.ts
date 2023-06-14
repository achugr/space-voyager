import {httpPost} from "./utils";
import {UserTokenData} from "./spaceAuth";

export function grabOrganizationData(userTokenData?: UserTokenData) {
    const call = async () => {
        await httpPost("/api/graph", userTokenData!!.userToken, {})
    }
    call().catch(console.error);
}
